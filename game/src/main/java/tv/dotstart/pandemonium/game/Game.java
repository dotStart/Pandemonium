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

import org.controlsfx.tools.Platform;

import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import javafx.scene.image.Image;
import tv.dotstart.pandemonium.effect.EffectFactory;
import tv.dotstart.pandemonium.game.matcher.ExecutableMatcher;
import tv.dotstart.pandemonium.game.matcher.ModuleMatcher;

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
     * Retrieves a set of known executable matchers.
     *
     * This method is required to at least one matcher. If no matchers are provided, no process
     * will
     * be located for attachment and thus no action will be taken.
     *
     * The provided matchers will be applied one-by-one until at least one matcher returns true or
     * the set is exceeded.
     */
    @Nonnull
    Set<ExecutableMatcher> getExecutableMatchers();

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
     * Retrieves a set of module matchers.
     *
     * If one or more matchers are returned by this method, its contained matchers are evaluated
     * until at least a single one returns true or the provided matchers are exceeded. If an empty
     * set is returned, which is the default behavior unless a custom implementation is provided,
     * all previously matched executables will be considered compatible unless a later check
     * mismatches.
     *
     * Unless at least one matcher within the returned set returns true on a passed process's
     * metadata, the process in question is considered incompatible and marked as such within the
     * application UI. If at least one matcher returns true, the process is considered compatible
     * and attached to.
     */
    @Nonnull
    default Set<ModuleMatcher> getModuleMatchers() {
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

    /**
     * Checks whether a game matches the supplied executable metadata.
     */
    static boolean matchesExecutable(@Nonnull Game game, @Nonnull String name, @Nonnull Platform platform, @Nonnegative long size) {
        for (ExecutableMatcher matcher : game.getExecutableMatchers()) {
            if (matcher.matches(name, platform, size)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a game matches the supplied module metadata.
     */
    static boolean matchesModule(@Nonnull Game game, @Nonnull String moduleName, @Nonnull Platform platform, @Nonnegative long size) {
        Set<ModuleMatcher> matchers = game.getModuleMatchers();

        if (matchers.size() == 0) {
            return true;
        }

        for (ModuleMatcher matcher : matchers) {
            if (matcher.matches(moduleName, platform, size)) {
                return true;
            }
        }

        return false;
    }

    // TODO: Memory based compatibility
}
