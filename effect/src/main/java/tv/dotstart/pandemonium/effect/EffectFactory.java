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

import org.controlsfx.tools.Platform;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Provides a factory for an effect which may be applied to the program instance or memory in order
 * to change the game behavior persistently or temporarily.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface EffectFactory {

    /**
     * Builds a new effect instance for the specified process.
     */
    @Nonnull
    Effect build(@Nonnull Process process);

    /**
     * Retrieves the base localization key which is used to refer to the effects produced by this
     * factory within the application UI.
     */
    @Nonnull
    static String getBaseLocalizationKey(@Nonnull EffectFactory factory) {
        return getBaseLocalizationKey(factory.getClass());
    }

    /**
     * Retrieves the base localization key which is used to refer to the effects produced by this
     * factory within the application UI.
     */
    @Nonnull
    static String getBaseLocalizationKey(@Nonnull Class<? extends EffectFactory> factory) {
        return "effect." + factory.getName();
    }

    /**
     * Retrieves the description localization key.
     */
    @Nonnull
    static String getDescriptionLocalizationKey(@Nonnull EffectFactory factory) {
        return getDescriptionLocalizationKey(factory.getClass());
    }

    /**
     * Retrieves a game unique identifier which refers to this effect in presets.
     *
     * This identifier is required to be unique within the namespace of the module and should never
     * be reassigned even if an effect is removed and is thus no longer available in the
     * definition.
     *
     * This guarantees correct resolving of effects when effects are added or removed since users
     * are able to load presets even when their revisions differ (e.g. all located effects will be
     * activated while non-existent effects are skipped).
     */
    @Nonnegative
    int getEffectId();

    /**
     * Retrieves the description localization key.
     */
    @Nonnull
    static String getDescriptionLocalizationKey(@Nonnull Class<? extends EffectFactory> factory) {
        return getBaseLocalizationKey(factory) + ".description";
    }

    /**
     * Retrieves a title localization key.
     */
    @Nonnull
    static String getTitleLocalizationKey(@Nonnull EffectFactory factory) {
        return getTitleLocalizationKey(factory.getClass());
    }

    /**
     * Retrieves a title localization key.
     */
    @Nonnull
    static String getTitleLocalizationKey(@Nonnull Class<? extends EffectFactory> factory) {
        return getBaseLocalizationKey(factory) + ".title";
    }

    /**
     * Checks whether this effect is compatible with the supplied platform.
     *
     * It is generally recommended to only include effects which are available for all platforms
     * the
     * game is available on as to keep races predictable and fair for all participants.
     *
     * In addition, the application will warn users when they attempt to load a preset which
     * includes effects that aren't supported on the current platform to not produce unexpected
     * results.
     */
    default boolean isCompatibleWith(@Nonnull Platform platform) {
        return true;
    }

    /**
     * Checks whether this effect is compatible with the supplied effect.
     *
     * When this check returns false, this factory will not be invoked and another effect may be
     * generated (depending on the current game preset).
     */
    default boolean isCompatibleWith(@Nonnull EffectFactory factory, @Nonnull Effect effect) {
        return true;
    }

    /**
     * Checks whether the effects produced by this factory are persistent (e.g. are applied once
     * and
     * have no need for an inversion).
     *
     * This is most commonly used when increasing or decreasing values in order to increase or
     * decrease the game difficulty.
     */
    default boolean isPersistent() {
        return false;
    }

    /**
     * Checks whether this effect may stack (e.g. may be applied multiple times at once).
     *
     * When this method yields true, another instance of this effect may be appended to the list of
     * active effects.
     */
    default boolean mayStack() {
        return false;
    }
}
