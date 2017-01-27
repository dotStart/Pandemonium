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
package tv.dotstart.pandemonium.ui.window.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import tv.dotstart.pandemonium.fx.control.GameStateLabel;
import tv.dotstart.pandemonium.fx.control.game.EffectView;
import tv.dotstart.pandemonium.fx.localization.ConfigurationAwareMessageSource;
import tv.dotstart.pandemonium.game.Game;
import tv.dotstart.pandemonium.game.GameConfiguration;
import tv.dotstart.pandemonium.ui.game.EffectManager;
import tv.dotstart.pandemonium.ui.game.ProcessWatcher;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class ActivityView implements Initializable {
    private final ObjectProperty<GameConfiguration> gameConfiguration = new SimpleObjectProperty<>();
    private final ObjectProperty<Runnable> onStop = new SimpleObjectProperty<>();

    private final ConfigurationAwareMessageSource messageSource;
    private final ProcessWatcher processWatcher;
    private final EffectManager effectManager;

    private final GameStateLabel stateLabel;

    // <editor-fold desc="FXML">
    @FXML
    private ImageView gameIconImageView;
    @FXML
    private Label gameNameLabel;

    @FXML
    private EffectView effectView;

    @FXML
    private HBox footerLeft;
    @FXML
    private HBox footerRight;
    // </editor-fold>

    @Autowired
    public ActivityView(@Nonnull ConfigurationAwareMessageSource messageSource, @Nonnull ProcessWatcher processWatcher, @Nonnull EffectManager effectManager) {
        this.messageSource = messageSource;
        this.processWatcher = processWatcher;
        this.effectManager = effectManager;

        this.stateLabel = new GameStateLabel(this.messageSource);

        processWatcher.gameConfigurationProperty().bind(this.gameConfigurationProperty());
        this.stateLabel.stateProperty().bind(effectManager.stateProperty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.gameIconImageView.imageProperty().bind(Bindings.createObjectBinding(this::updateGameIcon, this.gameConfiguration));
        this.gameNameLabel.textProperty().bind(Bindings.createObjectBinding(this::updateGameName, this.gameConfiguration));
        this.effectView.setItems(this.effectManager.getEffectList());
        this.footerLeft.getChildren().add(this.stateLabel);
    }

    // <editor-fold desc="Event Handlers & Bindings">

    /**
     * Updates the game icon.
     */
    @Nullable
    private Image updateGameIcon() {
        GameConfiguration configuration = this.getGameConfiguration();

        if (configuration == null) {
            return null;
        }

        return Game.getIcon(configuration.getGame());
    }

    /**
     * Updates the game name.
     */
    @Nullable
    private String updateGameName() {
        GameConfiguration configuration = this.getGameConfiguration();

        if (configuration == null) {
            return null;
        }

        return this.messageSource.getMessage(Game.getTitleLocalizationKey(configuration.getGame()));
    }

    /**
     * Handles a user's request to stop an active configuration.
     */
    @FXML
    private void onRequestStop() {
        Runnable onStop = this.getOnStop();

        if (onStop != null) {
            onStop.run();
        }
    }
    // </editor-fold>

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
    public Runnable getOnStop() {
        return this.onStop.get();
    }

    @Nonnull
    public ObjectProperty<Runnable> onStopProperty() {
        return this.onStop;
    }

    public void setOnStop(@Nullable Runnable onStop) {
        this.onStop.set(onStop);
    }
    // </editor-fold>
}
