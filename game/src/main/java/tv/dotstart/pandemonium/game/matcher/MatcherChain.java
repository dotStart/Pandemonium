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

import com.sun.jna.Native;
import com.sun.jna.Structure;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.process.Process;

/**
 * Represents a chain of matchers which decide whether a process is considered compatible with a
 * game definition.
 *
 * TODO: Improve integration with executable name list against matcher chain
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface MatcherChain {
    MatcherChain TRUE = new MatcherChain() {
    };
    MatcherChain FALSE = new MatcherChain() {
        @Override
        public boolean matches(@Nonnull Process process) {
            return false;
        }

        @Override
        public boolean isCompatible(@Nonnull Process process) {
            return false;
        }
    };

    // <editor-fold desc="Chaining">

    /**
     * Inverts the matcher provided by a supplied matcher chain.
     */
    @Nonnull
    static MatcherChain not(@Nonnull MatcherChain chain) {
        return new MatcherChain() {
            @Override
            public boolean matches(@Nonnull Process process) {
                return !chain.matches(process);
            }

            @Override
            public boolean isCompatible(@Nonnull Process process) {
                return !chain.isCompatible(process);
            }
        };
    }

    /**
     * Combines an arbitrary amount of matcher chains using a binary AND.
     */
    @Nonnull
    static MatcherChain and(@Nonnull MatcherChain... chains) {
        return new MatcherChain() {
            @Override
            public boolean matches(@Nonnull Process process) {
                for (MatcherChain chain : chains) {
                    if (!chain.matches(process)) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public boolean isCompatible(@Nonnull Process process) {
                for (MatcherChain chain : chains) {
                    if (!chain.isCompatible(process)) {
                        return false;
                    }
                }

                return true;
            }
        };
    }

    /**
     * Combines an arbitrary amount of matcher chains using a binary OR.
     */
    @Nonnull
    static MatcherChain or(@Nonnull MatcherChain... chains) {
        return new MatcherChain() {
            @Override
            public boolean matches(@Nonnull Process process) {
                for (MatcherChain chain : chains) {
                    if (chain.matches(process)) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public boolean isCompatible(@Nonnull Process process) {
                for (MatcherChain chain : chains) {
                    if (chain.isCompatible(process)) {
                        return true;
                    }
                }

                return false;
            }
        };
    }

    /**
     * Combines two matcher chains using a binary xor.
     */
    @Nonnull
    static MatcherChain xor(@Nonnull MatcherChain chainA, @Nonnull MatcherChain chainB) {
        return new MatcherChain() {
            @Override
            public boolean matches(@Nonnull Process process) {
                boolean matchA = chainA.matches(process);
                boolean matchB = chainB.matches(process);

                return (matchA || matchB) && (matchA != matchB);
            }

            @Override
            public boolean isCompatible(@Nonnull Process process) {
                boolean matchA = chainA.isCompatible(process);
                boolean matchB = chainB.isCompatible(process);

                return (matchA || matchB) && (matchA != matchB);
            }
        };
    }

    /**
     * Combines an arbitrary amount of matcher chains using a binary NAND.
     */
    @Nonnull
    static MatcherChain nand(@Nonnull MatcherChain... chain) {
        return not(and(chain));
    }

    /**
     * Combines an arbitrary amount of matcher chains using a binary NOR.
     */
    @Nonnull
    static MatcherChain nor(@Nonnull MatcherChain... chain) {
        return not(or(chain));
    }

    /**
     * Combines this chain with another matcher chain using a binary AND.
     */
    @Nonnull
    default MatcherChain and(@Nonnull MatcherChain chain) {
        return and(this, chain);
    }

    /**
     * Combines this chain with another matcher chain using a binary OR.
     */
    @Nonnull
    default MatcherChain or(@Nonnull MatcherChain chain) {
        return or(this, chain);
    }

    /**
     * Combines this chain with another matcher chain using a binary XOR.
     */
    @Nonnull
    default MatcherChain xor(@Nonnull MatcherChain chain) {
        return xor(this, chain);
    }

    /**
     * Combines this chain with another matcher chain using a binary NOR.
     */
    @Nonnull
    default MatcherChain nor(@Nonnull MatcherChain chain) {
        return nor(this, chain);
    }

    /**
     * Combines this chain with another matcher chain using a binary NAND.
     */
    @Nonnull
    default MatcherChain nand(@Nonnull MatcherChain chain) {
        return nand(this, chain);
    }
    // </editor-fold>

    // <editor-fold desc="Properties">

    /**
     * Registers an executable matcher with this chain.
     */
    @Nonnull
    default Executable executable() throws IllegalArgumentException {
        return new ExecutableImpl(this);
    }

    /**
     * Registers a memory matcher with this chain.
     */
    @Nonnull
    default Memory memory() {
        return new MemoryImpl(this);
    }

    /**
     * Registers a module matcher with this chain.
     */
    @Nonnull
    default Module module() {
        return new ModuleImpl(this);
    }
    // </editor-fold>

    // <editor-fold desc="Creation">

    /**
     * Creates a new executable matcher.
     */
    @Nonnull
    static Executable createExecutable() {
        return new ExecutableImpl(null);
    }

    /**
     * Creates a new memory matcher.
     */
    @Nonnull
    static Memory createMemory() {
        return new MemoryImpl(null);
    }

    /**
     * Creates a new module matcher.
     */
    @Nonnull
    static Module createModule() {
        return new ModuleImpl(null);
    }
    // </editor-fold>

    /**
     * Checks whether the supplied process matches the parameters stored within this matcher chain
     * instance.
     *
     * This method verifies both process information and module sizes at the same time since this
     * information is present even when the process is not yet attached.
     */
    default boolean matches(@Nonnull Process process) {
        return true;
    }

    /**
     * Checks whether the supplied attached process is compatible with the parameters within this
     * chain.
     *
     * This method is used to validate memory compatibility (such as addresses being set to a
     * specific value or available for reading).
     */
    default boolean isCompatible(@Nonnull Process process) {
        return true;
    }

    /**
     * Manages the configuration of an executable matcher.
     */
    interface Executable {

        /**
         * Appends this executable configuration to the chain and returns the newly created chain.
         *
         * @throws IllegalStateException when no name has been appended to the executable.
         */
        @Nonnull
        MatcherChain and() throws IllegalStateException;

        /**
         * Sets the name of this executable.
         */
        @Nonnull
        Executable name(@Nonnull String name);

        /**
         * Sets the size of this executable (in bytes).
         */
        @Nonnull
        Executable size(@Nonnegative long size);
    }

    /**
     * Manages the configuration of a memory matcher.
     */
    interface Memory {

        /**
         * Appends this memory configuration to the chain and returns the newly created chain.
         *
         * @throws IllegalStateException when no definition has been appended to the configuration.
         */
        @Nonnull
        MatcherChain and() throws IllegalStateException;

        /**
         * Adds a matcher to the memory configuration which verifies the accessibility (read/write)
         * of an address by reading it.
         */
        @Nonnull
        Memory accessible(@Nonnull String moduleName, @Nonnegative long offset, @Nonnegative int length);

        /**
         * @see #accessible(String, long, int)
         */
        @Nonnull
        default Memory byteAccessible(@Nonnull String moduleName, @Nonnegative long offset) {
            return this.accessible(moduleName, offset, 1);
        }

        /**
         * @see #accessible(String, long, int)
         */
        @Nonnull
        default Memory byteArrayAccissible(@Nonnull String moduleName, @Nonnegative long offset, @Nonnegative int length) {
            return this.accessible(moduleName, offset, length);
        }

        /**
         * @see #accessible(String, long, int)
         */
        @Nonnull
        default Memory byteBufferAccessible(@Nonnull String moduleName, @Nonnegative long offset, @Nonnegative int length) {
            return this.byteArrayAccissible(moduleName, offset, length);
        }

        /**
         * @see #accessible(String, long, int)
         */
        @Nonnull
        default Memory integerAccessible(@Nonnull String moduleName, @Nonnegative long offset) {
            return this.accessible(moduleName, offset, 4);
        }

        /**
         * @see #accessible(String, long, int)
         */
        @Nonnull
        default Memory longAccessible(@Nonnull String moduleName, @Nonnegative long offset) {
            return this.accessible(moduleName, offset, 8);
        }

        /**
         * @see #accessible(String, long, int)
         */
        @Nonnull
        default Memory shortAccessible(@Nonnull String moduleName, @Nonnegative long offset) {
            return this.accessible(moduleName, offset, 2);
        }

        /**
         * @see #accessible(String, long, int)
         */
        @Nonnull
        default Memory stringAccessible(@Nonnull String moduleName, @Nonnegative long offset, @Nonnegative int maxLength) {
            return this.accessible(moduleName, offset, maxLength);
        }

        /**
         * @see #accessible(String, long, int)
         */
        @Nonnull
        default Memory structureAccessible(@Nonnull String moduleName, @Nonnegative long offset, @Nonnull Class<? extends Structure> type) {
            return this.accessible(moduleName, offset, Native.getNativeSize(type));
        }
    }

    /**
     * Manges the configuration of a module matcher.
     */
    interface Module {

        /**
         * Appends this module configuration to the chain and returns the newly created chain.
         *
         * @throws IllegalStateException when no name has been appended to the configuration.
         */
        @Nonnull
        MatcherChain and() throws IllegalStateException;

        /**
         * Sets a name for this module configuration.
         */
        @Nonnull
        Module name(@Nonnull String name);

        /**
         * Sets a size for this module configuration (in bytes).
         */
        @Nonnull
        Module size(@Nonnegative long size);
    }
}
