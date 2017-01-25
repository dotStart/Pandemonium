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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.process.exception.ProcessStateException;

/**
 * Represents a module within a game process such as the executable itself or
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface ProcessModule {

    /**
     * Retrieves the platform specific name of this module.
     *
     * Keep in mind that module names make use of their respective native names (e.g. "dxmd.exe"
     * and "steam.dll" on Windows or "dxmd" and "steam.so" on Linux or Mac OS).
     */
    @Nonnull
    String getName();

    /**
     * Retrieves the path to this module.
     */
    @Nonnull
    Path getPath();

    /**
     * Retrieves the module size in bytes.
     */
    @Nonnegative
    long getSize();

    /**
     * Creates a pointer relative to this module.
     *
     * @param offset  an offset from the module start address.
     * @param offsets an array of further offsets to apply when resolving deep pointers.
     * @throws ProcessStateException when the process state prevents access to memory.
     */
    @Nonnull
    ProcessMemoryPointer pointer(@Nonnegative long offset, @Nonnull @Nonnegative long... offsets) throws ProcessStateException;
}
