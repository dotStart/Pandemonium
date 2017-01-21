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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.memory.MemoryAccessor;
import tv.dotstart.pandemonium.memory.MemoryPointer;
import tv.dotstart.pandemonium.memory.exception.MemoryAddressException;
import tv.dotstart.pandemonium.memory.exception.MemoryAddressOutOfBoundsException;
import tv.dotstart.pandemonium.memory.exception.MemoryStateException;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface ProcessMemory extends MemoryAccessor {

    /**
     * Creates a pointer to an address relative to the module base address.
     *
     * @throws IllegalArgumentException          when the supplied module does not exist.
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer createPointer(@Nonnull String moduleName, @Nonnegative long offset, @Nonnull long... offsets) throws MemoryAddressException, MemoryStateException;
}
