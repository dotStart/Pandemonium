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
package tv.dotstart.pandemonium.game.matcher;

import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.memory.process.ProcessMemory;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@FunctionalInterface
public interface MemoryMatcher {

    /**
     * Chains an arbitrary number of matchers using a binary AND operation.
     */
    @Nonnull
    static MemoryMatcher and(@Nonnull MemoryMatcher... matchers) {
        return (name, memory) -> {
            for (MemoryMatcher matcher : matchers) {
                if (!matcher.matches(name, memory)) {
                    return false;
                }
            }

            return true;
        };
    }

    /**
     * Chains an arbitrary number of matchers using a binary OR operation.
     */
    @Nonnull
    static MemoryMatcher or(@Nonnull MemoryMatcher... matchers) {
        return (name, memory) -> {
            for (MemoryMatcher matcher : matchers) {
                if (matcher.matches(name, memory)) {
                    return true;
                }
            }

            return false;
        };
    }

    /**
     * Combines two matchers using a binary XOR operation.
     */
    @Nonnull
    static MemoryMatcher xor(@Nonnull MemoryMatcher matcherA, @Nonnull MemoryMatcher matcherB) {
        return (name, memory) -> {
            boolean a = matcherA.matches(name, memory);
            boolean b = matcherB.matches(name, memory);

            return ((a || b) && a != b);
        };
    }

    /**
     * Negates the result of a matcher.
     */
    @Nonnull
    static MemoryMatcher not(@Nonnull MemoryMatcher matcher) {
        return (name, memory) -> !matcher.matches(name, memory);
    }

    /**
     * Checks whether the supplied executable memory matches the expected bounds.
     */
    boolean matches(@Nonnull String name, @Nonnull ProcessMemory memory);
}
