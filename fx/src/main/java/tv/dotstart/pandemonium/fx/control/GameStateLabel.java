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
package tv.dotstart.pandemonium.fx.control;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import tv.dotstart.pandemonium.fx.localization.ConfigurationAwareMessageSource;

/**
 * Provides a simple label which changes its text and color as the game pauses, resets or
 * terminates.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class GameStateLabel extends Label {
    private static final String MESSAGE_BASE_KEY = "main.activity.state";
    private static final PseudoClass TERMINATED_PSEUDO_CLASS = PseudoClass.getPseudoClass("terminated");
    private static final PseudoClass TITLE_SCREEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("title-screen");
    private static final PseudoClass PAUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("paused");
    private static final PseudoClass PLAYING_PSEUDO_CLASS = PseudoClass.getPseudoClass("playing");

    private final ObjectProperty<State> state = new SimpleObjectProperty<State>() {
        @Override
        protected void invalidated() {
            super.invalidated();

            State state = this.get();
            GameStateLabel.this.pseudoClassStateChanged(TERMINATED_PSEUDO_CLASS, state == State.TERMINATED);
            GameStateLabel.this.pseudoClassStateChanged(TITLE_SCREEN_PSEUDO_CLASS, state == State.TITLE_SCREEN);
            GameStateLabel.this.pseudoClassStateChanged(PAUSED_PSEUDO_CLASS, state == State.PAUSED);
            GameStateLabel.this.pseudoClassStateChanged(PLAYING_PSEUDO_CLASS, state == State.PLAYING);
        }
    };

    private final ConfigurationAwareMessageSource messageSource;

    public GameStateLabel(@Nonnull ConfigurationAwareMessageSource messageSource) {
        this.messageSource = messageSource;

        this.getStyleClass().add("game-state");
        this.textProperty().bind(Bindings.createStringBinding(this::updateText, this.state));
    }

    @Nullable
    private String updateText() {
        State state = this.getState();

        if (state == null) {
            return null;
        }

        return this.messageSource.getMessage(MESSAGE_BASE_KEY + "." + state.name().toLowerCase());
    }

    // <editor-fold desc="Getters & Setters">
    public State getState() {
        return this.state.get();
    }

    @Nonnull
    public ObjectProperty<State> stateProperty() {
        return this.state;
    }

    public void setState(State state) {
        this.state.set(state);
    }
    // </editor-fold>

    /**
     * Provides a list of valid game states.
     */
    public enum State {
        TERMINATED,
        TITLE_SCREEN,
        PAUSED,
        PLAYING
    }
}
