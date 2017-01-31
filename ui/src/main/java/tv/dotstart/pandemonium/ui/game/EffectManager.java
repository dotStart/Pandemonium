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
package tv.dotstart.pandemonium.ui.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import tv.dotstart.pandemonium.configuration.ApplicationConfiguration;
import tv.dotstart.pandemonium.effect.Effect;
import tv.dotstart.pandemonium.effect.EffectConfiguration;
import tv.dotstart.pandemonium.effect.EffectFactory;
import tv.dotstart.pandemonium.event.RemoveEffectEvent;
import tv.dotstart.pandemonium.event.ScheduleEffectEvent;
import tv.dotstart.pandemonium.fx.control.GameStateLabel;
import tv.dotstart.pandemonium.fx.control.game.ScheduledEffect;
import tv.dotstart.pandemonium.fx.localization.ConfigurationAwareMessageSource;
import tv.dotstart.pandemonium.game.GameConfiguration;
import tv.dotstart.pandemonium.game.GameStateMapper;
import tv.dotstart.pandemonium.process.Process;

/**
 * Manages the lifetime of effects within the application.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Lazy
@Component
public class EffectManager {
    private static final Logger logger = LogManager.getFormatterLogger(EffectManager.class);
    private static final int MAX_SPAWN_ATTEMPTS = 5;

    private final ObjectProperty<GameConfiguration> gameConfiguration = new SimpleObjectProperty<>();
    private final ObjectProperty<Process> process = new SimpleObjectProperty<>();
    private final ObjectProperty<GameStateLabel.State> state = new SimpleObjectProperty<>();
    private final ObservableList<ScheduledEffect> effectList = FXCollections.observableArrayList();
    private final List<MediaPlayer> mediaPlayers = new ArrayList<>();
    private final Timeline spawnTimeline = new Timeline(
            new KeyFrame(Duration.seconds(20), this::spawnEffect)
    );
    private final Timeline stateTimeline = new Timeline(
            new KeyFrame(Duration.millis(500), this::checkState)
    );

    private final ApplicationContext context;
    private final ApplicationConfiguration applicationConfiguration;
    private final ConfigurationAwareMessageSource messageSource;

    private Random random;
    private GameStateMapper stateMapper;

    @Autowired
    public EffectManager(@Nonnull ApplicationContext context, @Nonnull ApplicationConfiguration applicationConfiguration, @Nonnull ConfigurationAwareMessageSource messageSource) {
        this.context = context;
        this.applicationConfiguration = applicationConfiguration;
        this.messageSource = messageSource;

        this.spawnTimeline.setCycleCount(Animation.INDEFINITE);
        this.stateTimeline.setCycleCount(Animation.INDEFINITE);

        this.process.addListener(this::onProcessInvalidated);
        this.effectList.addListener((ListChangeListener<ScheduledEffect>) c -> {
            while (c.next()) {
                List<? extends ScheduledEffect> added = c.getAddedSubList();

                if (!added.isEmpty()) {
                    added.forEach((e) -> this.context.publishEvent(new ScheduleEffectEvent(e)));
                }

                List<? extends ScheduledEffect> removed = c.getRemoved();

                if (!removed.isEmpty()) {
                    removed.forEach((e) -> this.context.publishEvent(new RemoveEffectEvent(e)));
                }
            }
        });
    }

    /**
     * Aborts all effects and restores the process to its default values.
     */
    private void abort() {
        logger.info("Process is about to be closed - Aborting all effects and stopping timeline");
        this.stateTimeline.stop();

        // if we have active effects we'll attempt to revert them since we are hopefully detaching
        // from the process
        // FIXME: Check for process termination
        if (this.effectList.size() != 0) {
            this.effectList.forEach(ScheduledEffect::abort);
            this.effectList.clear();
        }

        this.setState(GameStateLabel.State.TERMINATED);
    }

    /**
     * Checks whether the game is currently active and starts/stops the spawn timeline as needed.
     */
    private void checkState(@Nonnull ActionEvent event) {
        GameConfiguration configuration = this.getGameConfiguration();
        Process process = this.getProcess();

        if (configuration == null || process == null) {
            return;
        }

        try {
            // check whether the game has been reset since our last call and if so reset the RNG to its
            // initial state to guarantee we get the same behavior as we did during the first launch
            if (this.stateMapper.hasReset()) {
                String seed = configuration.getSeed();

                if (seed == null) {
                    logger.error("Ignoring reset - Seed unavailable");
                    logger.error("This is a bug!");
                } else {
                    logger.info("State mapper indicated reset - Re-initializing spawner seed");

                    this.random = new Random(configuration.getSeed().hashCode());
                    this.spawnTimeline.playFromStart();
                }
            }

            if (!this.stateMapper.inGame()) {
                // our state mapper identifies the game to be on its title screen or in its main
                // menu and thus we will have to stop our spawn timeline and revert all effects
                if (this.spawnTimeline.getStatus() != Animation.Status.STOPPED) {
                    logger.info("State mapper indicates main-menu/title screen state - Stopping effect spawn cycle and removing all active effects");
                    this.spawnTimeline.stop();

                    this.effectList.forEach(ScheduledEffect::abort);
                    this.effectList.clear();

                    this.setState(GameStateLabel.State.TITLE_SCREEN);
                }
            } else if (this.stateMapper.isPaused()) {
                // our state mapper identifies the game to be paused (e.g. the player is currently
                // in a menu or on a loading screen), we'll stop the timeline temporarily and revert
                // as soon as the state changes
                if (this.spawnTimeline.getStatus() == Animation.Status.RUNNING) {
                    logger.info("State mapper indicates pause - Pausing effect spawn cycle and active effects");

                    this.spawnTimeline.pause();
                    this.effectList.forEach(ScheduledEffect::pause);

                    this.setState(GameStateLabel.State.PAUSED);
                }
            } else if (this.spawnTimeline.getStatus() != Animation.Status.RUNNING) {
                // the state mapper indicates the game to be active but our local state does not
                // reflect this - Start timeline from scratch or resume where we left off
                switch (this.spawnTimeline.getStatus()) {
                    case PAUSED:
                        logger.info("State mapper indicates active gameplay - Resuming operation");

                        this.spawnTimeline.play();
                        this.effectList.forEach(ScheduledEffect::play);
                        break;
                    case STOPPED:
                        logger.info("State mapper indicates gameplay start - Initializing operation");

                        this.spawnTimeline.playFromStart();
                        break;
                }

                this.setState(GameStateLabel.State.PLAYING);
            }
        } catch (Throwable ex) {
            this.stateTimeline.stop();

            Platform.runLater(() -> {
                throw new RuntimeException("Failed to update process state: " + ex.getMessage(), ex);
            });
        }
    }

    /**
     * Evaluates a chance (ranging from 0 to 100).
     */
    private boolean evaluateChance(double chance) {
        Random random = this.random;
        return random != null && random.nextInt(100) < chance;

    }

    /**
     * Plays an audio clip.
     */
    private void playAudioClip(@Nullable String clipPath) {
        if (clipPath == null) {
            logger.warn("No audio clip specified");
            return;
        }

        Path path = Paths.get(clipPath);

        if (Files.notExists(path)) {
            logger.warn("Audio clip file \"%s\" does no longer exist", path);
            return;
        }

        if (!Files.isReadable(path)) {
            logger.warn("Audio clip file \"%s\" is not readable", path);
        }

        this.playAudioClip(path);
    }

    /**
     * Plays an audio clip.
     */
    private void playAudioClip(@Nonnull Path clipPath) {
        try {
            String uri = clipPath.toUri().toURL().toExternalForm();

            logger.info("Playing schedule audio clip: %s", uri);
            Media clip = new Media(uri);

            final MediaPlayer player = new MediaPlayer(clip);
            player.setOnEndOfMedia(() -> this.mediaPlayers.remove(player));
            player.setVolume(this.applicationConfiguration.getAudioVolume() / 100d);
            player.play();

            this.mediaPlayers.add(player);
        } catch (MalformedURLException ex) {
            logger.error("Audio clip path \"" + clipPath.toString() + "\" is invalid: " + ex.getMessage(), ex);
        } catch (MediaException ex) {
            switch (ex.getType()) {
                case MEDIA_CORRUPTED:
                    logger.error("Failed to play clip: Media is corrupted: %s", ex.getMessage());
                    break;
                case MEDIA_INACCESSIBLE:
                    logger.error("Failed to play clip: Media is inaccessible: %s", ex.getMessage());
                    break;
                case MEDIA_UNAVAILABLE:
                    logger.error("Failed to play clip: Media is unavailable: %s", ex.getMessage());
                    break;
                case MEDIA_UNSUPPORTED:
                    logger.error("Failed to play clip: Media is of an unsupported type: %s", ex.getMessage());
                    break;
                default:
                    logger.error("Failed to play clip: Unknown error: " + ex.getMessage());
                    break;
            }
        }
    }

    /**
     * Evaluates the chances of spawning an effect.
     */
    private void spawnEffect(@Nonnull ActionEvent event) {
        GameConfiguration configuration = this.getGameConfiguration();
        Process process = this.getProcess();
        Random random = this.random;

        if (configuration == null || process == null || random == null) {
            return;
        }

        // remove garbage
        if (!this.effectList.isEmpty()) {
            logger.info("Removing reverted effects");
            this.effectList.removeIf((e) -> e.getState() == ScheduledEffect.State.REVERTED);

            logger.info("%d active effects remain", this.effectList.size());
        }

        // evaluate effect spawn
        logger.info("Evaluating effect spawn");

        if (!this.evaluateChance(configuration.getEffectChance())) {
            logger.info("Skipping spawn - Chance condition not met");
            return;
        }

        if (!this.effectList.isEmpty() && !this.evaluateChance(configuration.getCombinationChance())) {
            logger.info("Skipping spawn - Combination chance condition not met");
            return;
        }

        try {
            int attempt = 0;

            // try to locate an effect which either stacks with present effects or is not yet
            // present within the active effect list and abort if the maximum amount of tries is
            // exceeded to not end up in an infinite loop by accident
            while (attempt < MAX_SPAWN_ATTEMPTS) {
                double delay = configuration.getDelayLow() + (random.nextDouble() * (configuration.getDelayHigh() - configuration.getDelayLow()));
                double duration = configuration.getDurationLow() + (random.nextDouble() * (configuration.getDurationHigh() - configuration.getDurationLow()));

                EffectFactory[] factories = configuration.getEffectConfigurations().stream()
                        .filter(EffectConfiguration::isActive)
                        .map(EffectConfiguration::getEffectFactory)
                        .toArray(EffectFactory[]::new);
                EffectFactory factory = factories[random.nextInt(factories.length)];

                // check whether the chosen effect is actually compatible with this platform before
                // creating a new instance
                if (!factory.isCompatibleWith(org.controlsfx.tools.Platform.getCurrent())) {
                    logger.info("Spawn attempt #%02d - Effect provided by factory %s is not compatible with current platform", attempt++, factory.getClass());
                    continue;
                }

                // check whether the chosen effect is currently compatible with the process and its
                // state before creating an instance in order to prevent memory read/write
                // exceptions
                if (!factory.isCompatibleWith(process)) {
                    logger.info("Spawn attempt #%02d - Effect provided by factory %s is not compatible with process at this time", attempt++, factory.getClass());
                    continue;
                }

                Effect effect = factory.build(process);

                // check whether stacking is available for the specified effect and if not skip this
                // attempt in favor of a different effect
                if (!factory.mayStack() && this.effectList.stream().anyMatch((e) -> e.getFactory() == factory)) {
                    logger.info("Spawn attempt #%02d - Effect provided by factory %s does not stack", attempt++, factory.getClass());
                    continue;
                }

                // check whether two effects are incompatible with each other and if so skip this
                // attempt in favor of a different effect
                if (this.effectList.stream().anyMatch((e) -> !e.getFactory().isCompatibleWith(factory, effect) || !factory.isCompatibleWith(e.getFactory(), e.getGameEffect()))) {
                    logger.info("Spawn attempt #%02d - Effect provided by factory %s is incompatible with one or more active effects", attempt++, factory.getClass());
                    continue;
                }

                // update the title and description using the local message source as this
                // information is unavailable in our effect component
                logger.info("Spawn Attempt #%02d - Spawning effect provided by factory %s with delay %d seconds and duration %s seconds", attempt, factory.getClass().getName(), (int) delay, (int) duration);
                ScheduledEffect scheduledEffect = new ScheduledEffect(factory, effect, Duration.seconds(delay), Duration.seconds(duration));

                scheduledEffect.setTitle(this.messageSource.getMessage(EffectFactory.getTitleLocalizationKey(factory)));
                scheduledEffect.setDescription(this.messageSource.getMessage(EffectFactory.getDescriptionLocalizationKey(factory)));

                this.effectList.add(scheduledEffect);
                scheduledEffect.play();

                if (this.applicationConfiguration.isAudioPlaySchedule()) {
                    logger.info("Playing schedule audio clip");
                    this.playAudioClip(this.applicationConfiguration.getAudioClipSchedule());
                }

                scheduledEffect.setOnApply(() -> {
                    if (this.applicationConfiguration.isAudioPlayApply()) {
                        logger.info("Playing apply audio clip");
                        this.playAudioClip(this.applicationConfiguration.getAudioClipApply());
                    }
                });

                scheduledEffect.setOnRevert(() -> {
                    if (this.applicationConfiguration.isAudioPlayRevert()) {
                        logger.info("Playing revert audio clip");
                        this.playAudioClip(this.applicationConfiguration.getAudioClipRevert());
                    }
                });

                break;
            }

            if (attempt == MAX_SPAWN_ATTEMPTS) {
                logger.warn("Failed to spawn after %02d attempts - Giving up", MAX_SPAWN_ATTEMPTS);
            }
        } catch (Throwable ex) {
            logger.warn("Encountered an unexpected error while processing spawn queue: " + ex.getMessage(), ex);

            Platform.runLater(() -> {
                throw ex;
            });
        }
    }

    /**
     * Handles the invalidation of the process variable.
     */
    private void onProcessInvalidated(@Nonnull Observable observable) {
        GameConfiguration configuration = this.getGameConfiguration();
        Process process = this.getProcess();

        // if the process or configuration was removed, we'll make sure to close all of our handles
        // and revert all effects (if this is still possible for us to pull off at this time)
        if (process == null || configuration == null) {
            this.abort();
            this.stateMapper = null;
            return;
        }

        // check whether we got a valid seed and reset if there is none
        // this case should actually never occur since we do not modify the configuration without
        // detaching from the process
        String seed = configuration.getSeed();

        if (seed == null) {
            logger.warn("Seed is null - Cannot continue");
            this.abort();
            return;
        }

        // initialize a state mapper for the specified process
        logger.info("Initializing state mapper");
        this.stateMapper = configuration.getGame().createStateMapper(process);

        // initialize the RNG in order to kickstart the spawn process
        logger.info("Initializing random number generator using seed %d", seed.hashCode());
        this.random = new Random(seed.hashCode());

        // start the scan timeline in order to decide when to start spawning effects
        logger.info("Initializing spawn timeline");
        this.stateTimeline.playFromStart();
    }

    // <editor-fold desc="Getters & Setters">
    @Nullable
    public GameConfiguration getGameConfiguration() {
        return this.gameConfiguration.get();
    }

    @Nonnull
    public ObjectProperty<GameConfiguration> gameConfigurationProperty() {
        return this.gameConfiguration;
    }

    public void setGameConfiguration(@Nullable GameConfiguration gameConfiguration) {
        this.gameConfiguration.set(gameConfiguration);
    }

    @Nullable
    public Process getProcess() {
        return this.process.get();
    }

    @Nonnull
    public ObjectProperty<Process> processProperty() {
        return this.process;
    }

    public void setProcess(@Nullable Process process) {
        this.process.set(process);
    }

    public GameStateLabel.State getState() {
        return this.state.get();
    }

    @Nonnull
    public ObjectProperty<GameStateLabel.State> stateProperty() {
        return this.state;
    }

    public void setState(GameStateLabel.State state) {
        this.state.set(state);
    }

    @Nonnull
    public ObservableList<ScheduledEffect> getEffectList() {
        return FXCollections.unmodifiableObservableList(this.effectList);
    }
    // </editor-fold>
}
