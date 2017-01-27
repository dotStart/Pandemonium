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
public abstract class MouseSensitivityEffectFactory implements EffectFactory {
    private static final long MOUSE_X_SENSITIVITY_PTR = 0x709E38;
    private static final long MOUSE_Y_SENSITIVITY_PTR = 0x709E3C;

    private final int effectId;
    private final float multiplier;

    MouseSensitivityEffectFactory(int effectId, float multiplier) {
        this.effectId = effectId;
        this.multiplier = multiplier;
    }

    @Nonnull
    @Override
    public Effect build(@Nonnull Process process) {
        return new Effect() {
            private final ProcessMemoryPointer sensitivityPointer = MouseSensitivityEffectFactory.this.createPointer(process);

            private float value;

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply() {
                this.value = this.sensitivityPointer.readFloat();
                this.sensitivityPointer.writeFloat(this.value * MouseSensitivityEffectFactory.this.multiplier);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void revert() {
                this.sensitivityPointer.writeFloat(this.value);
            }
        };
    }

    /**
     * Constructs a memory pointer which refers to the correct sensitivity field.
     */
    @Nonnull
    protected abstract ProcessMemoryPointer createPointer(@Nonnull Process process);

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEffectId() {
        return this.effectId;
    }

    /**
     * Provides a base for effects which alter the mouse X sensitivity.
     */
    public abstract static class X extends MouseSensitivityEffectFactory {

        X(@Nonnegative int effectId, float multiplier) {
            super(effectId, multiplier);
        }

        /**
         * {@inheritDoc}
         */
        @Nonnull
        @Override
        protected ProcessMemoryPointer createPointer(@Nonnull Process process) {
            return process.pointer("dxhr.exe", MOUSE_X_SENSITIVITY_PTR);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCompatibleWith(@Nonnull EffectFactory factory, @Nonnull Effect effect) {
            return !(factory instanceof X);
        }

        public static class High extends X {
            public High() {
                super(7, 4);
            }
        }

        public static class Invert extends X {
            public Invert() {
                super(8, -1);
            }
        }

        public static class Low extends X {
            public Low() {
                super(9, .25f);
            }
        }
    }

    /**
     * Provides a base for effects which alter the mouse Y sensitivity.
     */
    public abstract static class Y extends MouseSensitivityEffectFactory {

        Y(@Nonnegative int effectId, float multiplier) {
            super(effectId, multiplier);
        }

        /**
         * {@inheritDoc}
         */
        @Nonnull
        @Override
        protected ProcessMemoryPointer createPointer(@Nonnull Process process) {
            return process.pointer("dxhr.exe", MOUSE_Y_SENSITIVITY_PTR);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCompatibleWith(@Nonnull EffectFactory factory, @Nonnull Effect effect) {
            return !(factory instanceof Y);
        }

        public static class High extends Y {
            public High() {
                super(10, 4);
            }
        }

        public static class Invert extends Y {
            public Invert() {
                super(11, -1);
            }
        }

        public static class Low extends Y {
            public Low() {
                super(12, .25f);
            }
        }
    }
}
