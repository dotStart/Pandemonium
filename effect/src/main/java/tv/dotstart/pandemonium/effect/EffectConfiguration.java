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
package tv.dotstart.pandemonium.effect;

import java.util.Objects;

import javax.annotation.Nonnull;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Provides an object which is used to alter the configuration of an effect during the configuration
 * phase.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class EffectConfiguration {
    private final BooleanProperty active = new SimpleBooleanProperty();
    private final EffectFactory effectFactory;

    public EffectConfiguration(@Nonnull EffectFactory effectFactory) {
        this.effectFactory = effectFactory;
    }

    public boolean isActive() {
        return this.active.get();
    }

    @Nonnull
    public BooleanProperty activeProperty() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    @Nonnull
    public EffectFactory getEffectFactory() {
        return this.effectFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        EffectConfiguration that = (EffectConfiguration) o;
        return Objects.equals(this.active, that.active) &&
                Objects.equals(this.effectFactory, that.effectFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.active, this.effectFactory);
    }
}
