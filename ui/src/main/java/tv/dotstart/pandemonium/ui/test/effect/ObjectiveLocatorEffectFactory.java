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
public class ObjectiveLocatorEffectFactory implements EffectFactory {
    private static final long LOCATOR_PTR = 0x1855919;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Effect build(@Nonnull Process process) {
        return new Effect() {
            private final ProcessMemoryPointer locatorPointer = process.pointer("dxhr.exe", LOCATOR_PTR);

            private boolean enabled;

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply() {
                this.enabled = this.locatorPointer.readByte() == 1;
                this.locatorPointer.writeByte((byte) (this.enabled ? 0 : 1));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void revert() {
                this.locatorPointer.writeByte((byte) (this.enabled ? 1 : 0));
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEffectId() {
        return 13;
    }
}
