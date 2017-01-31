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
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.process.ProcessAccessor;
import tv.dotstart.pandemonium.process.exception.ProcessException;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class Win32ProcessAccessor implements ProcessAccessor {
    private static final Logger logger = LogManager.getFormatterLogger(Win32ProcessAccessor.class);

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Set<Win32Process> getActiveProcesses() {
        return this.getMatchingProcess((n) -> true);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    public Set<Win32Process> getMatchingProcess(@Nonnull Predicate<String> matcher) {
        Set<Win32Process> processes = new HashSet<>();
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));

        try {
            Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();

            while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry)) {
                try {
                    String name = Native.toString(processEntry.szExeFile);

                    if (!matcher.test(name)) {
                        continue;
                    }

                    processes.add(new Win32Process(processEntry.th32ProcessID.intValue(), name));
                } catch (IllegalStateException | ProcessException ex) {
                    logger.warn("Failed to access process: " + ex.getMessage(), ex);
                    logger.warn("Process will not be visible to the application and its addons");
                }
            }

            return processes;
        } finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<Win32Process> getProcess(@Nonnull String name) {
        return this.getMatchingProcess(name::equalsIgnoreCase).stream().findAny();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<Win32Process> getProcess(@Nonnull Collection<String> names) {
        return names.stream()
                .flatMap((n) -> this.getProcess(n).map(Stream::of).orElseGet(Stream::empty))
                .findAny();
    }
}
