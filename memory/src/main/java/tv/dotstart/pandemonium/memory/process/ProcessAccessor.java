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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Provides access to the operating system's processes.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface ProcessAccessor {

    /**
     * Retrieves a set of processes which are currently active on this system.
     */
    @Nonnull
    Set<Process> getActiveProcesses();

    /**
     * Attempts to find an active process in the operating system with the specified name or returns
     * an empty optional if no such process exists.
     */
    @Nonnull
    Optional<Process> findProcess(@Nonnull String processName);

    /**
     * Attempts to find an active process based on a list of process names or returns an empty
     * optional if no such process exists.
     */
    @Nonnull
    default Optional<Process> findProcess(@Nonnull Collection<String> processNames) {
        return processNames.stream()
                .map(this::findProcess)
                .flatMap((o) -> o.isPresent() ? Stream.of(o.get()) : Stream.empty())
                .findAny();
    }
}
