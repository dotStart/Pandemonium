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
public class BulletEffectFactory implements EffectFactory {
    private static final long BULLET_LOADED_PTR = 0x01858238;
    private static final long[] BULLET_LOADED_OFFSETS = new long[]{0x70, 0x14};
    private static final long BULLET_INVENTORY_PTR = 0x01858238;
    private static final long[] BULLET_INVENTORY_OFFSETS = new long[]{0x70, 0x2E};

    private final int effectId;
    private final byte loadedAmount;
    private final byte inventoryAmount;

    BulletEffectFactory(@Nonnegative int effectId, @Nonnegative byte loadedAmount, @Nonnegative byte inventoryAmount) {
        this.effectId = effectId;
        this.loadedAmount = loadedAmount;
        this.inventoryAmount = inventoryAmount;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Effect build(@Nonnull Process process) {
        return new Effect() {
            private final ProcessMemoryPointer bulletLoadedPointer = process.pointer("dxhr.exe", BULLET_LOADED_PTR, BULLET_LOADED_OFFSETS);
            private final ProcessMemoryPointer bulletInventoryPointer = process.pointer("dxhr.exe", BULLET_INVENTORY_PTR, BULLET_INVENTORY_OFFSETS);

            private byte loadedAmount;
            private byte inventoryAmount;

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply() {
                this.loadedAmount = this.bulletLoadedPointer.readByte();
                this.inventoryAmount = this.bulletInventoryPointer.readByte();

                this.bulletLoadedPointer.writeByte(BulletEffectFactory.this.loadedAmount);
                this.bulletInventoryPointer.writeByte(BulletEffectFactory.this.inventoryAmount);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void revert() {
                this.bulletLoadedPointer.writeByte(this.loadedAmount);
                this.bulletInventoryPointer.writeByte(this.inventoryAmount);
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
    public boolean isCompatibleWith(@Nonnull Process process) {
        return process.pointer("dxhr.exe", BULLET_LOADED_PTR, BULLET_LOADED_OFFSETS).isReadable(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWith(@Nonnull EffectFactory factory, @Nonnull Effect effect) {
        return !(factory instanceof BulletEffectFactory);
    }

    public static class Empty extends BulletEffectFactory {
        public Empty() {
            super(0, (byte) 1, (byte) 0);
        }
    }

    public static class Full extends BulletEffectFactory {
        public Full() {
            super(1, Byte.MAX_VALUE, Byte.MAX_VALUE);
        }
    }
}
