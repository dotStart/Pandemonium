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

import org.controlsfx.tools.Platform;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Provides a base interface for matchers which decide whether a certain process matches the
 * requirements for a game.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@FunctionalInterface
public interface ExecutableMatcher {

    /**
     * Chains an arbitrary number of matchers using a binary AND operation.
     */
    @Nonnull
    static ExecutableMatcher and(@Nonnull ExecutableMatcher... matchers) {
        return (name, platform, size) -> {
            for (ExecutableMatcher matcher : matchers) {
                if (!matcher.matches(name, platform, size)) {
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
    static ExecutableMatcher or(@Nonnull ExecutableMatcher... matchers) {
        return (name, platform, size) -> {
            for (ExecutableMatcher matcher : matchers) {
                if (matcher.matches(name, platform, size)) {
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
    static ExecutableMatcher xor(@Nonnull ExecutableMatcher matcherA, @Nonnull ExecutableMatcher matcherB) {
        return (name, platform, size) -> {
            boolean a = matcherA.matches(name, platform, size);
            boolean b = matcherB.matches(name, platform, size);

            return ((a || b) && a != b);
        };
    }

    /**
     * Negates the result of a matcher.
     */
    @Nonnull
    static ExecutableMatcher not(@Nonnull ExecutableMatcher matcher) {
        return (name, platform, size) -> !matcher.matches(name, platform, size);
    }

    /**
     * Checks whether the supplied executable metadata matches the expected bounds.
     */
    boolean matches(@Nonnull String name, @Nonnull Platform platform, @Nonnegative long size);
}
