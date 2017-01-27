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
package tv.dotstart.pandemonium.ui.test.effect;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.effect.Effect;
import tv.dotstart.pandemonium.effect.EffectFactory;
import tv.dotstart.pandemonium.process.Process;
import tv.dotstart.pandemonium.process.ProcessMemoryPointer;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public abstract class FieldOfViewEffectFactory implements EffectFactory {
    private static final long FOV_PTR = 0x1855954;

    private final int effectId;
    private final int fov;

    protected FieldOfViewEffectFactory(@Nonnegative int effectId, @Nonnegative int fov) {
        this.effectId = effectId;
        this.fov = fov;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Effect build(@Nonnull Process process) {
        return new Effect() {
            private final ProcessMemoryPointer fovPointer = process.pointer("dxhr.exe", FOV_PTR);

            private int fov;

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply() {
                this.fov = this.fovPointer.readInteger();
                this.fovPointer.writeInteger(FieldOfViewEffectFactory.this.fov);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void revert() {
                this.fovPointer.writeInteger(this.fov);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEffectId() {
        return this.effectId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWith(@Nonnull EffectFactory factory, @Nonnull Effect effect) {
        return !(factory instanceof FieldOfViewEffectFactory);
    }

    public static class High extends FieldOfViewEffectFactory {
        public High() {
            super(5, 179);
        }
    }

    public static class Low extends FieldOfViewEffectFactory {
        public Low() {
            super(6, 20);
        }
    }
}
