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
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import tv.dotstart.pandemonium.process.Process;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@NotThreadSafe
class ModuleImpl implements MatcherChain.Module {
    private final MatcherChain parent;
    private String name;
    private long size;

    ModuleImpl(@Nullable MatcherChain parent) {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public MatcherChain and() throws IllegalStateException {
        MatcherChain chain = new MatcherChain() {
            private final String name = ModuleImpl.this.name;
            private final long size = ModuleImpl.this.size;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean matches(@Nonnull Process process) {
                return process
                        .getModule(this.name)
                        .filter((m) -> this.size == 0 || this.size == m.getSize())
                        .isPresent();
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
    public MatcherChain.Module name(@Nonnull String name) {
        this.name = name;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public MatcherChain.Module size(@Nonnegative long size) {
        this.size = size;
        return this;
    }
}
