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
     * Retrieves a set of known executable names which are considered to belong to this game.
     *
     * This method is required to always return at least one executable name and may additionally
     * include names for different platforms. When at least one of the executable names matches,
     * the process will be evaluated for compatibility against this definition.
     *
     * If required, definitions may also declare a set of required module names using {@link
     * #getModuleNames()} (which is especially useful when dealing with engine implementations which
     * make use of DLLs in order to contain their specific game logic).
     */
    @Nonnull
    Set<String> getExecutableNames();

    /**
     * Retrieves a set of known executable sizes which are valid for use with this definition.
     *
     * If this method returns a set of at least one value, matching executables will be evaluated
     * for their size against the values within the returned set.
     *
     * If none of the sizes returned by this method match, the process will be considered
     * incompatible and marked as such within the application UI.
     */
    @Nonnull
    default Set<Long> getExecutableSizes() {
        return Collections.emptySet();
    }

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
     * Returns a set of attached definition metadata which may be used to provide further
     * information to the user within the UI or decide upon compatibility with entered preset
     * strings.
     */
    @Nonnull
    Metadata getMetadata();

    /**
     * Retrieves a set of known module names which are considered to belong to this game.
     *
     * When a non-empty set is returned from this method, the modules provided by a matching
     * executable will be checked against this list. If none of the modules within this list are
     * present, the executable won't be considered an instance of this game definition.
     *
     * This is the recommended method of matching engines which contain all of their game logic
     * within DLLs.
     */
    @Nonnull
    default Set<String> getModuleNames() {
        return Collections.emptySet();
    }

    /**
     * Retrieves a set of known module sizes which are valid for use with this definition.
     *
     * If this method returns a set of at least one value, matching modules will be evaluated for
     * their size against the values within the returned set.
     *
     * If none of the sizes match, the process in question is considered incompatible and will be
     * marked as such in the application UI.
     */
    @Nonnull
    default Set<Long> getModuleSizes() {
        return Collections.emptySet();
    }

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

    // TODO: Memory based compatibility
}
