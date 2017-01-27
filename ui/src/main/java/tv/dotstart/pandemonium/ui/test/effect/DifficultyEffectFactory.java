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

import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.effect.Effect;
import tv.dotstart.pandemonium.effect.EffectFactory;
import tv.dotstart.pandemonium.process.Process;
import tv.dotstart.pandemonium.process.ProcessMemoryPointer;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class DifficultyEffectFactory implements EffectFactory {
    private static final long DIFFICULTY_PTR = 0x1855950;

    private final int effectId;
    private final byte level;

    DifficultyEffectFactory(int effectId, byte level) {
        this.effectId = effectId;
        this.level = level;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Effect build(@Nonnull Process process) {
        return new Effect() {
            private final ProcessMemoryPointer difficultyPointer = process.pointer("dxhr.exe", DIFFICULTY_PTR);

            private byte level;

            /**
             * {@inheritDoc}
             */
            @Override
            public void revert() {
                this.level = this.difficultyPointer.readByte();
                this.difficultyPointer.writeByte(DifficultyEffectFactory.this.level);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply() {
                this.difficultyPointer.writeByte(this.level);
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
        return !(factory instanceof DifficultyEffectFactory);
    }

    public static class Easy extends DifficultyEffectFactory {
        public Easy() {
            super(2, (byte) 0);
        }
    }

    public static class Hard extends DifficultyEffectFactory {
        public Hard() {
            super(3, (byte) 2);
        }
    }

    public static class Medium extends DifficultyEffectFactory {
        public Medium() {
            super(4, (byte) 1);
        }
    }
}
