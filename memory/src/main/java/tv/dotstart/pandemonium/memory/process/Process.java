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
package tv.dotstart.pandemonium.memory.process;

import java.util.Set;

import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.memory.exception.process.ProcessStateException;
import tv.dotstart.pandemonium.memory.module.ProcessModule;

/**
 * Represents a game process which a definition may attach to in order to read or write its memory
 * or otherwise alter its state.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface Process {

    /**
     * Retrieves the module which contains the executable resources.
     *
     * @throws ProcessStateException when the process state prevents access to modules.
     */
    @Nonnull
    ProcessModule getExecutableModule() throws ProcessStateException;

    /**
     * Retrieves a reference to this process's memory.
     *
     * @throws ProcessStateException when the process state prevents access to memory.
     */
    @Nonnull
    ProcessMemory getMemory() throws ProcessStateException;

    /**
     * Retrieves a process module.
     *
     * @throws ProcessStateException when the process state prevents access to modules.
     */
    @Nonnull
    ProcessModule getModule(@Nonnull String name) throws ProcessStateException;

    /**
     * Retrieves a set of process modules within this process.
     *
     * @throws ProcessStateException when the process state prevents access to modules.
     */
    @Nonnull
    Set<ProcessModule> getModules() throws ProcessStateException;

    /**
     * Retrieves the platform dependent name of this process.
     *
     * For instance, this could be "dxmd" on Linux and Mac OS while it would return "dxmd.exe" on
     * Windows based systems.
     *
     * Generally definitions are usually not required to even access this information since the
     * process watcher implementation will automatically search for known process names anyways.
     */
    @Nonnull
    String getName();

    /**
     * Checks whether a module exists within this process.
     *
     * @throws ProcessStateException when the process state prevents access to modules.
     */
    boolean hasModule(@Nonnull String moduleName) throws ProcessStateException;

    /**
     * Opens the process for reading/writing.
     *
     * @throws ProcessStateException when the process state prevents access.
     */
    void open() throws ProcessStateException;

    /**
     * Closes the process.
     *
     * @throws ProcessStateException when the process state prevents access.
     */
    void close() throws ProcessStateException;
}
