/*
 * Copyright 2017 Johannes Donath <me@dotstart.tv>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.dotstart.pandemonium.win32.process;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.NoSuchMessageException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.process.Process;
import tv.dotstart.pandemonium.process.ProcessMemoryPointer;
import tv.dotstart.pandemonium.process.exception.ProcessAttachmentException;
import tv.dotstart.pandemonium.process.exception.ProcessException;
import tv.dotstart.pandemonium.process.exception.ProcessPermissionException;
import tv.dotstart.pandemonium.process.exception.ProcessStateException;
import tv.dotstart.pandemonium.process.exception.ProcessTerminatedException;
import tv.dotstart.pandemonium.process.exception.memory.ProcessMemoryStateException;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class Win32Process implements Process {
    private static final Logger logger = LogManager.getFormatterLogger(Win32Process.class);

    private final int pid;
    private final String name;
    private final Path path;
    private final long size;
    private final boolean is64;
    private final Map<String, Win32ProcessModule> moduleMap;
    private final WinNT.HANDLE infoHandle;

    WinNT.HANDLE accessHandle;

    public Win32Process(@Nonnegative int pid, @Nonnull String name) throws ProcessException {
        this.pid = pid;
        this.name = name;

        // attach to process using read permissions
        logger.info("Attaching to process with pid %d (\"%s\") in read-only mode", pid, name);
        this.infoHandle = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ, true, this.pid);
        int errorNo = Kernel32.INSTANCE.GetLastError();

        switch (errorNo) {
            case Kernel32.ERROR_SUCCESS:
                break;
            case Kernel32.ERROR_INVALID_PARAMETER:
                throw new ProcessAttachmentException("Failed to attach to process: Invalid Parameter");
            case Kernel32.ERROR_ACCESS_DENIED:
                throw new ProcessPermissionException("Failed to attach to process: Access Denied");
            default:
                throw new ProcessAttachmentException("Failed to attach to process: Unknown error (code " + errorNo + ")");
        }

        // evaluate whether the operating system identifies itself to be a 64-bit system and if so
        // check whether the process is running within the 32-bit subsystem

        IntByReference wow64Process = new IntByReference();
        Kernel32.INSTANCE.IsWow64Process(Kernel32.INSTANCE.GetCurrentProcess(), wow64Process);

        if (wow64Process.getValue() == 0) { // AMD64 or IA64
            logger.info("Detected a 64-Bit Operating System - Evaluating process architecture");

            wow64Process.setValue(0);
            Kernel32.INSTANCE.IsWow64Process(this.infoHandle, wow64Process);

            if (wow64Process.getValue() != 0) {
                logger.info("Process is running in WoW64 - Using 32-Bit pointers");
                this.is64 = false;
            } else {
                logger.info("Process is running in native mode - Using 64-Bit pointers");
                this.is64 = true;
            }
        } else {
            logger.info("Detected 32-Bit Operating System - Assuming all processes to run in 32-Bit mode");
            this.is64 = false;
        }

        // query module list with info handle
        WinDef.HMODULE[] modules = new WinDef.HMODULE[1024];
        int moduleStructSize = Native.getNativeSize(WinDef.HMODULE.class);
        int moduleArraySize = moduleStructSize * modules.length;

        IntByReference requiredSize = new IntByReference();
        Psapi.INSTANCE.EnumProcessModules(this.infoHandle, modules, moduleArraySize, requiredSize);

        // make sure we got a list of modules and that it fits our array since truncated lists may
        // cause issues further down the road
        if (requiredSize.getValue() == 0 || requiredSize.getValue() < moduleStructSize) {
            throw new ProcessAttachmentException("Failed to retrieve process modules: No modules received");
        } else if (requiredSize.getValue() > moduleArraySize) {
            throw new ProcessAttachmentException("Failed to retrieve process modules: Module list exceeded allocated memory");
        }

        int moduleCount = requiredSize.getValue() / moduleStructSize;
        Map<String, Win32ProcessModule> moduleMap = new HashMap<>();
        logger.info("Found %d modules:", moduleCount);

        Path executablePath = null;
        long executableSize = 0;

        for (int i = 0; i < moduleCount; ++i) {
            WinDef.HMODULE module = modules[i];

            // query the module filename
            // this seems to be a fully qualified path to the respective exe or dll so we'll just
            // convert it into a path while we're at it
            char[] filename = new char[Kernel32.MAX_PATH];
            int length = Psapi.INSTANCE.GetModuleFileNameExW(this.infoHandle, module, filename, filename.length);
            Path modulePath = Paths.get(new String(filename, 0, length));

            // construct a new module representation and store it in our local module map
            try {
                logger.info("  #%02d - %s (located at %s) starting at 0x%016X", i + 1, modulePath.getFileName(), modulePath, Pointer.nativeValue(module.getPointer()));
                Win32ProcessModule processModule = new Win32ProcessModule(this, modulePath, module.getPointer());

                if (modulePath.getFileName().toString().equalsIgnoreCase(name)) {
                    executablePath = modulePath;
                    executableSize = processModule.getSize();
                }

                moduleMap.put(processModule.getName(), processModule);
            } catch (IOException ex) {
                throw new ProcessAttachmentException("Failed to access process module \"" + modulePath + "\": " + ex.getMessage(), ex);
            }
        }

        this.moduleMap = Collections.unmodifiableMap(moduleMap);

        // validate and store executable module information
        if (executablePath == null) {
            throw new ProcessAttachmentException("Failed to locate executable module");
        }

        this.path = executablePath;
        this.size = executableSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws ProcessAttachmentException, ProcessStateException {
        if (this.accessHandle == null) {
            throw new ProcessStateException("Process is already closed");
        }

        logger.info("Closing process \"%s\"", this.name);
        Kernel32.INSTANCE.CloseHandle(this.accessHandle);

        int errorNo = Kernel32.INSTANCE.GetLastError();
        if (errorNo != Kernel32.ERROR_SUCCESS) {
            throw new ProcessAttachmentException("Failed to detach from process: Received error " + errorNo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Path getExecutablePath() {
        return this.path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getExecutableSize() {
        return this.size;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Set<Win32ProcessModule> getModules() {
        return Collections.unmodifiableSet(new HashSet<>(this.moduleMap.values()));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<Win32ProcessModule> getModule(@Nonnull String moduleName) {
        return Optional.ofNullable(this.moduleMap.get(moduleName));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<Win32ProcessModule> getModule(@Nonnull Collection<String> moduleNames) {
        return moduleNames.stream()
                .flatMap((n) -> this.getModule(n).map(Stream::of).orElseGet(Stream::empty))
                .findAny();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasModule(@Nonnull String moduleName) {
        return this.moduleMap.containsKey(moduleName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasModule(@Nonnull Collection<String> moduleNames) {
        return moduleNames.stream()
                .anyMatch(this::hasModule);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean is64Bit() {
        return this.is64;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlive() {
        IntByReference exitCode = new IntByReference();
        Kernel32.INSTANCE.GetExitCodeProcess(this.infoHandle, exitCode);

        return exitCode.getValue() == Kernel32.STILL_ACTIVE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpen() {
        return this.accessHandle != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() throws ProcessAttachmentException, ProcessStateException {
        if (this.accessHandle != null) {
            throw new ProcessStateException("Process is already open");
        }

        // validate whether our handle is still valid in order to figure out whether the pid is even
        // valid for us to rely on in order to access the process
        if (!this.isAlive()) {
            throw new ProcessTerminatedException("Process with pid " + this.pid + " has terminated");
        }

        this.accessHandle = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_VM_WRITE | Kernel32.PROCESS_VM_OPERATION, true, this.pid);
        int errorNo = Kernel32.INSTANCE.GetLastError();

        switch (errorNo) {
            case Kernel32.ERROR_SUCCESS:
                break;
            case Kernel32.ERROR_INVALID_PARAMETER:
                throw new ProcessAttachmentException("Failed to attach to process: Invalid Parameter");
            case Kernel32.ERROR_ACCESS_DENIED:
                throw new ProcessPermissionException("Failed to attach to process: Access Denied");
            default:
                throw new ProcessAttachmentException("Failed to attach to process: Unknown error (code " + errorNo + ")");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer pointer(@Nonnull String moduleName, @Nonnegative long offset, @Nonnull @Nonnegative long... offsets) throws ProcessMemoryStateException {
        return this.getModule(moduleName)
                .orElseThrow(() -> new NoSuchMessageException("No such module"))
                .pointer(offset, offsets);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer pointer(@Nonnull Collection<String> moduleNames, @Nonnegative long offset, @Nonnull @Nonnegative long... offsets) throws ProcessMemoryStateException {
        return this.getModule(moduleNames)
                .orElseThrow(() -> new NoSuchMessageException("No such module"))
                .pointer(offset, offsets);
    }

    /**
     * Validates the process state.
     */
    void validate() {
        if (!this.isAlive()) {
            this.accessHandle = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {
        // ensure our access handle is disposed of correctly in case of a garbage collection so we
        // don't leave any resources open by accident
        if (this.accessHandle != null) {
            logger.warn("Closing remaining handle for process with pid %d through garbage collection", this.pid);

            Kernel32.INSTANCE.CloseHandle(this.accessHandle);
            this.accessHandle = null;
        }

        // ensure the information handle is closed as well since it cannot be closed through the
        // API
        Kernel32.INSTANCE.CloseHandle(this.infoHandle);
    }
}
