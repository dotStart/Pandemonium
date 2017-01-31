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
package tv.dotstart.pandemonium.ui.window;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tv.dotstart.pandemonium.configuration.ApplicationConfiguration;
import tv.dotstart.pandemonium.fx.FX;
import tv.dotstart.pandemonium.fx.annotation.ApplicationWindow;
import tv.dotstart.pandemonium.fx.localization.ConfigurationAwareMessageSource;
import tv.dotstart.pandemonium.ui.service.UpdateService;
import tv.dotstart.pandemonium.ui.window.splash.ApplicationUpdateTask;
import tv.dotstart.pandemonium.ui.window.splash.EndorsementUpdateTask;
import tv.dotstart.pandemonium.ui.window.splash.InitializationTask;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@ApplicationWindow
public class SplashScreen implements Initializable {
    private static Logger logger = LogManager.getFormatterLogger(SplashScreen.class);

    private final Deque<InitializationTask> tasks = new ArrayDeque<>();
    private final ObjectProperty<InitializationTask> activeTask = new SimpleObjectProperty<>();
    private final TaskThread taskThread = new TaskThread();

    private final FX fx;
    private final ConfigurationAwareMessageSource messageSource;

    @FXML
    private VBox root;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressBar progressBar;

    @Autowired
    public SplashScreen(@Nonnull ApplicationConfiguration configuration, @Nonnull FX fx, @Nonnull ConfigurationAwareMessageSource messageSource, @Nonnull UpdateService updateService) {
        this.fx = fx;
        this.messageSource = messageSource;

        if (configuration.isApplicationCheckUpdates()) {
            this.tasks.addLast(new ApplicationUpdateTask(updateService));
        }

        if (configuration.isApplicationCheckEndorsementUpdates()) {
            this.tasks.add(new EndorsementUpdateTask(updateService));
        }

        // TODO: Add support for addon updates
        /* if (configuration.isApplicationCheckAddonUpdates()) {
            this.tasks.add(new AddonUpdateTask());
        } */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // append a style class if we are actually residing in a transparent window in order to add
        // the respective drop shadows to this window
        if (Platform.isSupported(ConditionalFeature.TRANSPARENT_WINDOW)) {
            this.root.getStyleClass().add("transparent");
        }

        this.statusLabel.textProperty().bind(Bindings.createStringBinding(this::updateStatusLabelText, this.activeTask));

        this.taskThread.start();
    }

    /**
     * Updates the status label.
     */
    @Nullable
    private String updateStatusLabelText() {
        InitializationTask task = this.activeTask.get();

        if (task == null) {
            return null;
        }

        return this.messageSource.getMessage("splash.task." + task.getLocalizationKey());
    }

    /**
     * Exits the splash screen and accesses the new application.
     */
    private void startApplication() {
        logger.info("Initialization has been finalized - Starting application");

        ((Stage) this.root.getScene().getWindow()).close();
        this.fx.createStage(MainWindow.class)
                .setTitle("Pandemonium")
                .addIcon(new Image(SplashScreen.class.getResource("/icon/application256.png").toExternalForm()))
                .buildAndShow();
    }

    /**
     * Provides a thread which will iterate all tasks until the queue is emptied out.
     */
    private class TaskThread extends Thread {

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            // sleep for 2 seconds to give JavaFX some time to catch up
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignore) {
            }

            // perform tasks
            int size = SplashScreen.this.tasks.size();

            while (!SplashScreen.this.tasks.isEmpty()) {
                InitializationTask task = SplashScreen.this.tasks.pollFirst();
                int remaining = SplashScreen.this.tasks.size();

                Platform.runLater(() -> {
                    SplashScreen.this.activeTask.set(task);
                    SplashScreen.this.progressBar.setProgress((size - remaining) / (double) size);
                });

                try {
                    task.run();
                } catch (Exception ex) {
                    if (task.isCritical()) {
                        throw new RuntimeException("Failed to initialize application: " + ex.getMessage(), ex);
                    }

                    logger.fatal("Failed to execute task implementation " + task.getClass() + ": " + ex.getMessage(), ex);
                }
            }

            // open up main window
            Platform.runLater(SplashScreen.this::startApplication);
        }
    }
}
