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
package tv.dotstart.pandemonium.process;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.process.exception.ProcessAttachmentException;
import tv.dotstart.pandemonium.process.exception.ProcessStateException;

/**
 * A representation for a process on this operating system.
 *
 * This interface is the main method of gaining information on a process as well as reading/writing
 * from and to the process memory.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface Process {

    /**
     * Closes the memory for reading and writing and restores its internal state.
     *
     * @throws ProcessAttachmentException when detaching from this process fails.
     * @throws ProcessStateException      when the process state prevents detaching.
     */
    void close() throws ProcessAttachmentException, ProcessStateException;

    /**
     * Retrieves the name this process identifies itself as within the operating system
     * (e.g. "dxhr.exe").
     */
    @Nonnull
    String getName();

    /**
     * Retrieves a representation of the executable path where the executable this process
     * originated from may be found.
     */
    @Nonnull
    Path getExecutablePath();

    /**
     * Retrieves the size of the executable this process originated from.
     */
    @Nonnegative
    long getExecutableSize();

    /**
     * Retrieves a set of modules which were located within this process and are thus available to
     * games for reading and writing.
     */
    @Nonnull
    Set<ProcessModule> getModules();

    /**
     * Retrieves a module with a matching native name or an empty optional if no such module is
     * located within this process.
     *
     * Keep in mind that module names make use of their respective native names (e.g. "dxmd.exe"
     * and "steam.dll" on Windows or "dxmd" and "steam.so" on Linux or Mac OS).
     */
    @Nonnull
    Optional<ProcessModule> getModule(@Nonnull String moduleName);

    /**
     * Retrieves a module with a matching native name or an empty optional if no such module is
     * located within this process.
     *
     * This method accepts multiple possible module names and is especially useful if the game is
     * available on multiple platforms (e.g. Windows, Mac OS and Linux) as some lack suffixes to
     * their module names or may identify their modules differently.
     *
     * @param moduleNames a collection of module names.
     * @see #getModule(String) for more information on this method.
     */
    @Nonnull
    Optional<ProcessModule> getModule(@Nonnull Collection<String> moduleNames);

    /**
     * Checks whether a module with a matching name is present within the process.
     *
     * Keep in mind that module names make use of their respective native names (e.g. "dxmd.exe"
     * and "steam.dll" on Windows or "dxmd" and "steam.so" on Linux or Mac OS).
     */
    boolean hasModule(@Nonnull String moduleName);

    /**
     * Checks whether a module with a matching name is present within the process.
     *
     *
     * This method accepts multiple possible module names and is especially useful if the game is
     * available on multiple platforms (e.g. Windows, Mac OS and Linux) as some lack suffixes to
     * their module names or may identify their modules differently.
     *
     * @param moduleNames a collection of module names.
     * @see #hasModule(String) for more information on this method.
     */
    boolean hasModule(@Nonnull Collection<String> moduleNames);

    /**
     * Checks whether this process is a 64-Bit process.
     */
    boolean is64Bit();

    /**
     * Checks whether this process is still alive and has not been closed by the user or the
     * operating system itself.
     */
    boolean isAlive();

    /**
     * Checks whether this process is closed and thus not available for reading and writing from or
     * to its memory.
     */
    default boolean isClosed() {
        return !this.isOpen();
    }

    /**
     * Checks whether this process is open and thus available for reading and writing from or to its
     * memory.
     */
    boolean isOpen();

    /**
     * Opens the process memory for reading and writing.
     *
     * @throws ProcessAttachmentException when attaching to this process fails.
     * @throws ProcessStateException      when the process state prevents attachment.
     */
    void open() throws ProcessAttachmentException, ProcessStateException;

    /**
     * Creates a new pointer to an address in a process module.
     *
     * When using debugging software such as Cheat Engine, this pointer system should be rather
     * familiar as these programs tend to refer to addresses the same way.
     *
     * For instance, a pointer to an address identified as "dxhr.exe"+18B25 would be equal to {@code
     * pointer("dxhr.exe", 0x18B25)}.
     *
     * @param moduleName a platform dependent module name.
     * @param offset     an offset from the module start address.
     * @param offsets    an array of further offsets to apply when resolving deep pointers.
     * @throws ProcessStateException when accessing process memory is prevented by its current
     *                               state.
     */
    @Nonnull
    ProcessMemoryPointer pointer(@Nonnull String moduleName, @Nonnegative long offset, @Nonnull @Nonnegative long... offsets) throws ProcessStateException;

    /**
     * Creates a new pointer to an address in a process module.
     *
     * This method accepts multiple possible module names and is especially useful if the game is
     * available on multiple platforms (e.g. Windows, Mac OS and Linux) as some lack suffixes to
     * their module names or may identify their executables differently.
     *
     * @param moduleNames a collection of known module names.
     * @param offset      an offset from the module start address.
     * @param offsets     an array of further offsets to apply when resolving deep pointers.
     * @throws ProcessStateException when accessing process memory is prevented by its current
     *                               state.
     * @see #pointer(String, long, long...) for more information on this method.
     */
    @Nonnull
    ProcessMemoryPointer pointer(@Nonnull Collection<String> moduleNames, @Nonnegative long offset, @Nonnull @Nonnegative long... offsets) throws ProcessStateException;
}
