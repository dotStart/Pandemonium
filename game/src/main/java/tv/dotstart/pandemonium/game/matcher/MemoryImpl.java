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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import tv.dotstart.pandemonium.process.Process;
import tv.dotstart.pandemonium.process.exception.ProcessStateException;
import tv.dotstart.pandemonium.process.exception.memory.ProcessMemoryReadException;
import tv.dotstart.pandemonium.process.exception.memory.ProcessMemoryStateException;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@NotThreadSafe
class MemoryImpl implements MatcherChain.Memory {
    private final MatcherChain parent;
    private final Set<Address> addresses = new HashSet<>();

    MemoryImpl(@Nullable MatcherChain parent) {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public MatcherChain and() throws IllegalStateException {
        if (this.addresses.size() == 0) {
            throw new IllegalStateException("No accessibility configuration in memory");
        }

        MatcherChain chain = new MatcherChain() {
            private final Address[] addresses = MemoryImpl.this.addresses.toArray(new Address[MemoryImpl.this.addresses.size()]);

            @Override
            public boolean isCompatible(@Nonnull Process process) {
                try {
                    for (Address address : this.addresses) {
                        for (int i = 0; i < address.length; ++i) {
                            process.pointer(address.moduleName, address.offset).readByte(i);
                        }
                    }

                    return true;
                } catch (ProcessMemoryStateException | ProcessMemoryReadException ex) {
                    return false;
                }
            }
        };

        if (this.parent == null) {
            return chain;
        }

        return this.parent.and(chain);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public MatcherChain.Memory accessible(@Nonnull String moduleName, @Nonnegative long offset, @Nonnegative int length) {
        this.addresses.add(new Address(moduleName, offset, length));
        return this;
    }

    /**
     * Represents an address which is to be checked for accessibility within a memory configuration.
     */
    private static class Address {
        private final String moduleName;
        private final long offset;
        private final int length;

        Address(@Nonnull String moduleName, @Nonnegative long offset, @Nonnegative int length) {
            this.moduleName = moduleName;
            this.offset = offset;
            this.length = length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;

            Address that = (Address) o;
            return this.offset == that.offset &&
                    this.length == that.length &&
                    Objects.equals(this.moduleName, that.moduleName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.moduleName, this.offset, this.length);
        }
    }
}
