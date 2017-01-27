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
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.util.Duration;
import tv.dotstart.pandemonium.game.Game;
import tv.dotstart.pandemonium.game.GameConfiguration;
import tv.dotstart.pandemonium.process.Process;
import tv.dotstart.pandemonium.process.ProcessAccessor;
import tv.dotstart.pandemonium.process.exception.ProcessAttachmentException;
import tv.dotstart.pandemonium.process.exception.ProcessPermissionException;
import tv.dotstart.pandemonium.process.exception.ProcessStateException;

/**
 * Provides a watcher which is capable of finding and attaching to processes using the backing
 * native implementation.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class ProcessWatcher {
    private static final Logger logger = LogManager.getFormatterLogger(ProcessWatcher.class);

    private final ObjectProperty<GameConfiguration> gameConfiguration = new SimpleObjectProperty<>();
    private final ObjectProperty<Process> process = new SimpleObjectProperty<>();

    private final ProcessAccessor processAccessor;
    private final EffectManager effectManager;

    private final Timeline scanTimeline = new Timeline(
            new KeyFrame(Duration.seconds(0), this::scan),
            new KeyFrame(Duration.seconds(5))
    );

    @Autowired
    public ProcessWatcher(@Nonnull ProcessAccessor processAccessor, @Nonnull EffectManager effectManager) {
        this.processAccessor = processAccessor;
        this.effectManager = effectManager;

        this.scanTimeline.setCycleCount(Animation.INDEFINITE);
        this.gameConfiguration.addListener(this::onConfigurationActivate);

        this.effectManager.gameConfigurationProperty().bind(this.gameConfigurationProperty());
        this.effectManager.processProperty().bind(this.process);
    }

    /**
     * Scans the active processes for a compatible game instance.
     */
    private void scan(@Nonnull ActionEvent event) {
        GameConfiguration configuration = this.getGameConfiguration();
        Process process = this.getProcess();

        try {
            // handle graceful shutdowns and lacking configurations
            if (configuration == null) {
                this.shutdown();
                return;
            }

            // handle process validation
            if (process != null) {
                // validate whether the process is still alive and if not request a graceful
                // shutdown
                if (!process.isAlive()) {
                    this.shutdown();
                }

                return;
            }

            // validate executable and module compatibility before actually attaching to the process
            Game game = configuration.getGame();
            process = this.processAccessor.getProcess(game.getExecutableNames())
                    .filter((p) -> game.getMatcherChain().matches(p))
                    .orElse(null);

            if (process == null) {
                return;
            }

            logger.info("Located matching process with name \"%s\"", process.getName());

            // open the process and verify memory compatibility
            logger.info("Opening process for reading and writing");
            process.open();

            logger.info("Confirming memory compatibility");

            if (!game.getMatcherChain().isCompatible(process)) {
                logger.warn("Process memory does not match expected bounds");
                logger.warn("Closing process");

                process.close();
                return;
            }

            // inform other components of our changes
            logger.info("Process seems to be compatible - Carrying on");
            this.process.setValue(process);
        } catch (Throwable ex) {
            this.scanTimeline.stop();

            if (ex instanceof ProcessPermissionException) {
                logger.warn("Lacking permissions to attach to process: " + ex.getMessage(), ex);
            }

            javafx.application.Platform.runLater(() -> {
                throw new RuntimeException("Failed to attach to process: " + ex.getMessage(), ex);
            });
        }
    }

    public void shutdown() {
        Process process = this.getProcess();

        if (process == null) {
            return;
        }

        logger.info("Closing process");
        this.process.set(null);

        try {
            process.close();
        } catch (ProcessAttachmentException | ProcessStateException ex) {
            throw new RuntimeException("Failed to detach from process: " + ex.getMessage(), ex);
        }
    }

    /**
     * Handles the activation of a configuration.
     */
    private void onConfigurationActivate(@Nonnull ObservableValue<? extends GameConfiguration> ob, @Nullable GameConfiguration o, @Nullable GameConfiguration n) {
        if (n == null) {
            logger.info("Configuration removed - Disabling watcher");

            this.scanTimeline.stop();
            this.shutdown();
            return;
        }

        logger.info("Configuration stored - Enabling watcher");
        this.scanTimeline.playFromStart();
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
    public ReadOnlyObjectProperty<Process> processProperty() {
        return this.process;
    }
    // </editor-fold>
}
