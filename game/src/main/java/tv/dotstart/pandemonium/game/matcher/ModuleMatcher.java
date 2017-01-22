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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Provides a base interface for matchers which decide whether a module matches the requirements for
 * this game.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@FunctionalInterface
public interface ModuleMatcher {

    /**
     * Chains an arbitrary number of matchers using a binary AND operation.
     */
    @Nonnull
    static ModuleMatcher and(@Nonnull ModuleMatcher... matchers) {
        return (name, size) -> {
            for (ModuleMatcher matcher : matchers) {
                if (!matcher.matches(name, size)) {
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
    static ModuleMatcher or(@Nonnull ModuleMatcher... matchers) {
        return (name, size) -> {
            for (ModuleMatcher matcher : matchers) {
                if (matcher.matches(name, size)) {
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
    static ModuleMatcher xor(@Nonnull ModuleMatcher matcherA, @Nonnull ModuleMatcher matcherB) {
        return (name, size) -> {
            boolean a = matcherA.matches(name, size);
            boolean b = matcherB.matches(name, size);

            return ((a || b) && a != b);
        };
    }

    /**
     * Negates the result of a matcher.
     */
    @Nonnull
    static ModuleMatcher not(@Nonnull ModuleMatcher matcher) {
        return (name, size) -> !matcher.matches(name, size);
    }

    /**
     * Checks whether the supplied module metadata matches the requirements for its parent game
     * definition.
     */
    boolean matches(@Nonnull String name, @Nonnegative long size);
}
