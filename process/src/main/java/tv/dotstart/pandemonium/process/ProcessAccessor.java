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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Provides platform independent access to this operating system's processes and their respective
 * memory.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface ProcessAccessor {

    /**
     * Retrieves a set of active processes on this operating system.
     */
    @Nonnull
    Set<? extends Process> getActiveProcesses();

    /**
     * Returns a running process on this operating system with the specified name or an empty
     * optional if no such process is active on this system.
     *
     * Keep in mind that process names make use of their respective native names (e.g. "dxmd.exe" on
     * Windows or "dxmd" on Linux or Mac OS).
     *
     * @param name a process name.
     */
    @Nonnull
    Optional<? extends Process> getProcess(@Nonnull String name);

    /**
     * Returns a running process on this operating system with the specified name or an empty
     * optional if no such process is active on this system.
     *
     * This method accepts multiple possible process names and is especially useful if the game is
     * available on multiple platforms (e.g. Windows, Mac OS and Linux) as some lack suffixes to
     * their process names or may identify their processes differently.
     */
    @Nonnull
    Optional<? extends Process> getProcess(@Nonnull Collection<String> names);
}
