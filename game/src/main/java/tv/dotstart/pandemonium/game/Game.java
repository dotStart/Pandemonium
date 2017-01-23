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
import tv.dotstart.pandemonium.game.matcher.MemoryMatcher;
import tv.dotstart.pandemonium.game.matcher.ModuleMatcher;
import tv.dotstart.pandemonium.memory.process.ProcessMemory;

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
     * When the returned set provides one or more matchers, the matchers are evaluated one by one
     * until a matcher returns true or the set is exceeded. If the set contains elements but is
     * exceeded without a single matcher returning true, the process in question will be considered
     * incompatible and marked as such in the UI.
     */
    @Nonnull
    default Set<ExecutableMatcher> getExecutableMatchers() {
        return Collections.emptySet();
    }

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
     * Returns a set of memory matchers.
     *
     * If one or more matchers are returned by this method, its contained matchers are evaluated
     * until at least a single one returns true or the provided matchers are exceeded. If an empty
     * set is returned, which is the default behavior unless a custom implementation is provided,
     * all previously matched executables will be considered compatible unless a later check
     * mismatches.
     *
     * Unless at least one matcher within the returned set returns true on a passed process's
     * memory, the process in question is considered incompatible and marked and such within the
     * application UI. If at least one matcher returns true, the process is considered compatible
     * and processing is enabled.
     */
    @Nonnull
    default Set<MemoryMatcher> getMemoryMatchers() {
        return Collections.emptySet();
    }

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
        Set<ExecutableMatcher> matchers = game.getExecutableMatchers();

        if (matchers.isEmpty()) {
            return true;
        }

        for (ExecutableMatcher matcher : matchers) {
            if (matcher.matches(name, platform, size)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a game matches the supplied process memory.
     */
    static boolean matchesMemory(@Nonnull Game game, @Nonnull String name, @Nonnull ProcessMemory memory) {
        Set<MemoryMatcher> matchers = game.getMemoryMatchers();

        if (matchers.isEmpty()) {
            return true;
        }

        for (MemoryMatcher matcher : matchers) {
            if (matcher.matches(name, memory)) {
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

        if (matchers.isEmpty()) {
            return true;
        }

        for (ModuleMatcher matcher : matchers) {
            if (matcher.matches(moduleName, platform, size)) {
                return true;
            }
        }

        return false;
    }
}
