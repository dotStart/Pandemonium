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
package tv.dotstart.pandemonium.memory.module;

import javax.annotation.Nonnull;

/**
 * Provides access to the information attached to a process module (such as an executable or DLL).
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface ProcessModule {

    /**
     * Retrieves the memory area occupied by this module.
     */
    @Nonnull
    ProcessModuleMemory getMemory();

    /**
     * Retrieves the name of this module.
     *
     * Module names are also available when creating pointers in order to access addresses relative
     * to a specific module in the application.
     *
     * This behavior is especially useful when dealing with engines that make use of DLLs in order
     * to provide their specific game logic.
     */
    @Nonnull
    String getName();
}
