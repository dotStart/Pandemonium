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
package tv.dotstart.pandemonium.fx.control.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import tv.dotstart.pandemonium.effect.Effect;
import tv.dotstart.pandemonium.effect.EffectFactory;

/**
 * Represents an effect which has been scheduled for application to a game instance.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class ScheduledEffect extends StackPane implements Initializable {
    private static final Logger logger = LogManager.getFormatterLogger(ScheduledEffect.class);
    private static final PseudoClass PSEUDO_CLASS_STOPPED = PseudoClass.getPseudoClass("stopped");
    private static final PseudoClass PSEUDO_CLASS_WAITING = PseudoClass.getPseudoClass("waiting");
    private static final PseudoClass PSEUDO_CLASS_APPLIED = PseudoClass.getPseudoClass("applied");
    private static final PseudoClass PSEUDO_CLASS_REVERTED = PseudoClass.getPseudoClass("reverted");

    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final ObjectProperty<State> state = new SimpleObjectProperty<State>() {
        @Override
        protected void invalidated() {
            super.invalidated();

            ScheduledEffect.this.pseudoClassStateChanged(PSEUDO_CLASS_STOPPED, this.get() == State.STOPPED);
            ScheduledEffect.this.pseudoClassStateChanged(PSEUDO_CLASS_WAITING, this.get() == State.WAITING);
            ScheduledEffect.this.pseudoClassStateChanged(PSEUDO_CLASS_APPLIED, this.get() == State.APPLIED);
            ScheduledEffect.this.pseudoClassStateChanged(PSEUDO_CLASS_REVERTED, this.get() == State.REVERTED);
        }
    };
    private final ObjectProperty<Runnable> onApply = new SimpleObjectProperty<>();
    private final ObjectProperty<Runnable> onRevert = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();

    private final EffectFactory factory;
    private final Effect effect;
    private final Duration delay;
    private final Duration duration;
    private final Timeline timeline;

    // <editor-fold desc="FXML">
    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Pane progressBar;
    // </editor-fold>

    public ScheduledEffect(@Nonnull EffectFactory factory, @Nonnull Effect effect, @Nonnull Duration delay, @Nonnull Duration duration) {
        this.factory = factory;
        this.effect = effect;
        this.delay = delay;
        this.duration = duration;

        // construct a timeline for this schedule
        this.timeline = new Timeline(
                new KeyFrame(delay, this::onDelayFinished),
                new KeyFrame(delay.add(duration), this::onDurationFinished)
        );

        this.progress.bind(Bindings.createDoubleBinding(this::updateProgress, this.timeline.currentTimeProperty()));

        // load the control contents
        FXMLLoader loader = new FXMLLoader();

        loader.setCharset(StandardCharsets.UTF_8);
        loader.setController(this);
        loader.setRoot(this);

        try (InputStream inputStream = this.getClass().getResourceAsStream("ScheduledEffect.fxml")) {
            loader.load(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load ScheduledEffect control: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.titleLabel.textProperty().bind(this.title);
        this.descriptionLabel.textProperty().bind(this.description);
        this.progressBar.maxWidthProperty().bind(Bindings.createDoubleBinding(this::updateProgressBarWidth, this.widthProperty(), this.progress));
    }

    /**
     * "Plays" the scheduled effect resulting in its effect being applied when the specified
     */
    public void play() {
        if (this.state.get() == State.WAITING || this.state.get() == State.APPLIED) {
            logger.info("Resuming playback of effect from paused state for effect provided by %s", this.factory.getClass().getName());
            this.timeline.play();
            return;
        }

        logger.info("Scheduled effect provided by %s", this.factory.getClass().getName());
        this.state.set(State.WAITING);
        this.timeline.playFromStart();
    }

    /**
     * Pauses the scheduled effect (e.g. delays the next action until the effect is unpaused).
     */
    public void pause() {
        this.timeline.pause();
    }

    /**
     * Aborts the effect (if it has already been played) and stops its schedule.
     */
    public void abort() {
        logger.info("Aborting effect provided by %s", this.factory.getClass().getName());
        this.timeline.stop();

        if (this.state.get() == State.APPLIED) {
            this.onDurationFinished(null);
        }

        this.state.set(State.STOPPED);
    }

    // <editor-fold desc="Event Handlers & Bindings">

    /**
     * Updates the effect progress.
     */
    @Nonnegative
    private double updateProgress() {
        double currentTime = this.timeline.getCurrentTime().toSeconds();
        double delay = this.delay.toSeconds();
        double duration = this.duration.toSeconds();

        if (delay >= currentTime) {
            if (delay == 0) {
                return 0;
            }

            return currentTime / delay;
        }

        if (duration == 0) {
            return 0;
        }

        currentTime -= delay;
        return Math.max(0d, Math.min(1d, currentTime / duration));
    }

    private double updateProgressBarWidth() {
        return this.getWidth() * this.getProgress();
    }

    /**
     * Handles a finalized delay.
     */
    private void onDelayFinished(ActionEvent event) {
        logger.info("%d second delay has passed - Applying effect provided by %s", (int) this.delay.toSeconds(), this.factory.getClass().getName());

        this.effect.apply();
        this.state.set(State.APPLIED);

        if (event != null) {
            Runnable runnable = this.getOnApply();

            if (runnable != null) {
                runnable.run();
            }
        }
    }

    /**
     * Handles a finalized duration.
     */
    private void onDurationFinished(ActionEvent event) {
        logger.info("%d second duration has passed - Reverting effect provided by %s", (int) this.duration.toSeconds(), this.factory.getClass().getName());

        if (!this.factory.isPersistent()) {
            this.effect.revert();
        } else {
            logger.info("Effect indicates that it is persistent and thus won't be reverted");
        }

        this.state.set(State.REVERTED);

        if (event != null) {
            Runnable runnable = this.getOnRevert();

            if (runnable != null) {
                runnable.run();
            }
        }
    }

    // </editor-fold>

    // <editor-fold desc="Getters & Setters">
    @Nonnull
    public EffectFactory getFactory() {
        return this.factory;
    }

    public double getProgress() {
        return this.progress.get();
    }

    @Nonnull
    public ReadOnlyDoubleProperty progressProperty() {
        return this.progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    @Nonnull
    public State getState() {
        return this.state.get();
    }

    @Nonnull
    public ReadOnlyObjectProperty<State> stateProperty() {
        return this.state;
    }

    @Nullable
    public Runnable getOnApply() {
        return this.onApply.get();
    }

    @Nonnull
    public ObjectProperty<Runnable> onApplyProperty() {
        return this.onApply;
    }

    public void setOnApply(@Nullable Runnable onApply) {
        this.onApply.set(onApply);
    }

    @Nullable
    public Runnable getOnRevert() {
        return this.onRevert.get();
    }

    @Nonnull
    public ObjectProperty<Runnable> onRevertProperty() {
        return this.onRevert;
    }

    public void setOnRevert(@Nullable Runnable onRevert) {
        this.onRevert.set(onRevert);
    }

    @Nonnull
    public Effect getGameEffect() {
        return this.effect;
    }

    @Nonnull
    public Duration getDelay() {
        return this.delay;
    }

    @Nonnull
    public Duration getDuration() {
        return this.duration;
    }

    @Nullable
    public String getTitle() {
        return this.title.get();
    }

    @Nonnull
    public StringProperty titleProperty() {
        return this.title;
    }

    public void setTitle(@Nullable String title) {
        this.title.set(title);
    }

    @Nonnull
    public String getDescription() {
        return this.description.get();
    }

    @Nonnull
    public StringProperty descriptionProperty() {
        return this.description;
    }

    public void setDescription(@Nullable String description) {
        this.description.set(description);
    }
    // </editor-fold>

    /**
     * Provides a list of valid schedule states.
     */
    public enum State {
        STOPPED,
        WAITING,
        APPLIED,
        REVERTED
    }
}
