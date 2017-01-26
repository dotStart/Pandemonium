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
package tv.dotstart.pandemonium.game;

import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import javafx.scene.image.Image;
import tv.dotstart.pandemonium.effect.EffectFactory;
import tv.dotstart.pandemonium.game.matcher.MatcherChain;

/**
 * Provides a game and the effects available to them as well as the information necessary to locate
 * the respective game process and validate its compatibility with this definition.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface Game {

    /**
     * Retrieves the base localization key used when referring to this game within the application
     * UI.
     */
    @Nonnull
    static String getBaseLocalizationKey(@Nonnull Game game) {
        return getBaseLocalizationKey(game.getClass());
    }

    /**
     * Retrieves the base localization key used when referring to this game within the application
     * UI.
     */
    @Nonnull
    static String getBaseLocalizationKey(@Nonnull Class<? extends Game> game) {
        return "game." + game.getName();
    }

    /**
     * Retrieves a set of effect factories which are provided by this game definition.
     */
    @Nonnull
    Set<EffectFactory> getEffectFactories();

    /**
     * Retrieves a set of known executable names.
     *
     * At least one name must be contained within the returned set in order to achieve attachment
     * when making use of this configuration. Any matching executable will be evaluated against
     * further executable and module matchers in order to decide upon compatibility.
     */
    @Nonnull
    Set<String> getExecutableNames();

    /**
     * Retrieves an icon URL which is used to reference this game definition within the application
     * UI or an empty optional, if no icon is provided for this definition.
     *
     * The URL returned by this method may also relate to a module resource (e.g. within the jar of
     * a module).
     */
    @Nonnull
    default Optional<URL> getIcon() {
        return Optional.empty();
    }

    /**
     * Retrieves the icon for a supplied game or the default placeholder icon.
     */
    @Nonnull
    static Image getIcon(@Nonnull Game game) {
        return new Image(game.getIcon().orElseGet(() -> Game.class.getResource("placeholder.png")).toExternalForm());
    }

    /**
     * Retrieves a globally unique identifier for this game definition.
     *
     * This identifier is mainly used to refer to this game definition within presets and is
     * unlikely to collide with other modules or even official implementations even when randomly
     * generated.
     */
    @Nonnull
    UUID getId();

    /**
     * Retrieves a chain of matchers which indicate whether a process is compatible with this game
     * definition or not.
     *
     * Matcher chains evaluate whether process names, sizes, module names sizes and memory
     * properties are within expected bounds and may be chained together using standard binary
     * operations in order to provide more complex matching schemes.
     */
    @Nonnull
    default MatcherChain getMatcherChain() {
        return MatcherChain.TRUE;
    }

    /**
     * Returns a set of attached definition metadata which may be used to provide further
     * information to the user within the UI or decide upon compatibility with entered preset
     * strings.
     */
    @Nonnull
    Metadata getMetadata();

    /**
     * Retrieves the title localization key.
     */
    @Nonnull
    static String getTitleLocalizationKey(@Nonnull Game game) {
        return getTitleLocalizationKey(game.getClass());
    }

    /**
     * Retrieves the title localization key.
     */
    @Nonnull
    static String getTitleLocalizationKey(@Nonnull Class<? extends Game> game) {
        return getBaseLocalizationKey(game) + ".title";
    }

    /**
     * Checks whether the game is currently paused.
     *
     * When paused effect application will be paused until the game reports otherwise to prevent
     * players from sitting through an effect on a pause screen as well as to prevent effects from
     * being applied or reverted during load screens (which may cause some problems in some specific
     * games since memory areas may become unavailable).
     *
     * Note: Implementing types are responsible for taking loading screens into account when effects
     * are reset during loading phases.
     */
    default boolean isPaused(@Nonnull Process process) {
        return false;
    }
}
