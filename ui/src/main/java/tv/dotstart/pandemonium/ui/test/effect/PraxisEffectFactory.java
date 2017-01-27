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
public class PraxisEffectFactory implements EffectFactory {
    private static final long PRAXIS_PTR = 0x015DE1A8;
    private static final long[] PRAXIS_OFFSETS = new long[]{0x14, 0x140};

    private final int effectId;
    private final byte praxis;

    PraxisEffectFactory(int effectId, byte praxis) {
        this.effectId = effectId;
        this.praxis = praxis;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Effect build(@Nonnull Process process) {
        return new Effect() {
            private final ProcessMemoryPointer praxisPointer = process.pointer("dxhr.exe", PRAXIS_PTR, PRAXIS_OFFSETS);

            private byte praxis;

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply() {
                this.praxis = this.praxisPointer.readByte();
                this.praxisPointer.writeByte(PraxisEffectFactory.this.praxis);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void revert() {
                this.praxisPointer.writeByte(this.praxis);
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
        return !(factory instanceof PraxisEffectFactory);
    }

    public static class High extends PraxisEffectFactory {
        public High() {
            super(14, Byte.MAX_VALUE);
        }
    }

    public static class Low extends PraxisEffectFactory {
        public Low() {
            super(15, (byte) 0);
        }
    }
}
