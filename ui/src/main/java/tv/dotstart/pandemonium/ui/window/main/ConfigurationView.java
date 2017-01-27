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

import org.controlsfx.control.PopOver;
import org.controlsfx.control.RangeSlider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import tv.dotstart.pandemonium.effect.EffectConfiguration;
import tv.dotstart.pandemonium.fx.FX;
import tv.dotstart.pandemonium.fx.control.IconButton;
import tv.dotstart.pandemonium.fx.factory.EffectCellFactory;
import tv.dotstart.pandemonium.fx.factory.GameCellFactory;
import tv.dotstart.pandemonium.fx.localization.ConfigurationAwareMessageSource;
import tv.dotstart.pandemonium.game.Game;
import tv.dotstart.pandemonium.game.GameConfiguration;
import tv.dotstart.pandemonium.module.ModuleManager;
import tv.dotstart.pandemonium.preset.Preset;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class ConfigurationView implements Initializable {
    private final ObjectProperty<GameConfiguration> gameConfiguration = new SimpleObjectProperty<>();
    private final ObjectProperty<Preset> preset = new SimpleObjectProperty<>();
    private final ObjectProperty<Runnable> onStart = new SimpleObjectProperty<>();

    private final ModuleManager moduleManager;
    private final ConfigurationAwareMessageSource messageSource;
    private final GameCellFactory gameCellFactory;
    private final EffectCellFactory effectCellFactory;

    private final PopOver endorsementPopOver;

    // <editor-fold desc="FXML">
    @FXML
    private ListView<GameConfiguration> gameListView;

    @FXML
    private Label placeholderLabel;
    @FXML
    private VBox gameConfigurationBox;

    @FXML
    private ImageView gameIconImageView;
    @FXML
    private Label gameNameLabel;
    @FXML
    private IconButton gameEndorsementButton;
    @FXML
    private IconButton startButton;

    @FXML
    private TextField seedTextField;
    @FXML
    private Slider effectChanceSlider;
    @FXML
    private Label effectChanceLabel;
    @FXML
    private Slider combinationChanceSlider;
    @FXML
    private Label combinationChanceLabel;
    @FXML
    private RangeSlider delayRangeSlider;
    @FXML
    private Label delayLabel;
    @FXML
    private RangeSlider durationRangeSlider;
    @FXML
    private Label durationLabel;

    @FXML
    private ListView<EffectConfiguration> effectListView;
    // </editor-fold>

    @Autowired
    public ConfigurationView(@Nonnull FX fx, @Nonnull ModuleManager moduleManager, @Nonnull ConfigurationAwareMessageSource messageSource, @Nonnull GameCellFactory gameCellFactory, @Nonnull EffectCellFactory effectCellFactory) {
        this.moduleManager = moduleManager;
        this.messageSource = messageSource;
        this.gameCellFactory = gameCellFactory;
        this.effectCellFactory = effectCellFactory;

        this.endorsementPopOver = fx.createPopOver("Endorsement", this.getClass().getClassLoader()).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.gameConfiguration.bind(this.gameListView.getSelectionModel().selectedItemProperty());

        this.placeholderLabel.visibleProperty().bind(this.gameConfiguration.isNull());
        this.gameConfigurationBox.visibleProperty().bind(this.gameConfiguration.isNotNull());

        this.gameListView.getSelectionModel().selectedItemProperty().addListener(this::updateGameBindings);
        this.gameListView.setItems(this.moduleManager.getGames());
        this.gameListView.setCellFactory(this.gameCellFactory);

        this.gameIconImageView.imageProperty().bind(Bindings.createObjectBinding(this::updateGameIcon, this.gameConfiguration));
        this.gameNameLabel.textProperty().bind(Bindings.createStringBinding(this::updateGameName, this.gameConfiguration));
        this.startButton.disableProperty().bind(Bindings.createBooleanBinding(this::isStartAvailable, this.preset));

        this.effectChanceLabel.textProperty().bind(Bindings.createStringBinding(() -> this.messageSource.getMessage("main.configuration.effectChance.value", (int) this.effectChanceSlider.getValue()), this.effectChanceSlider.valueProperty()));
        this.combinationChanceLabel.textProperty().bind(Bindings.createStringBinding(() -> this.messageSource.getMessage("main.configuration.combinationChance.value", (int) this.combinationChanceSlider.getValue()), this.combinationChanceSlider.valueProperty()));
        this.delayLabel.textProperty().bind(Bindings.createStringBinding(() -> this.messageSource.getMessage("main.configuration.delay.value", (int) this.delayRangeSlider.getLowValue(), (int) this.delayRangeSlider.getHighValue()), this.delayRangeSlider.lowValueProperty(), this.delayRangeSlider.highValueProperty()));
        this.durationLabel.textProperty().bind(Bindings.createStringBinding(() -> this.messageSource.getMessage("main.configuration.duration.value", (int) this.durationRangeSlider.getLowValue(), (int) this.durationRangeSlider.getHighValue()), this.durationRangeSlider.lowValueProperty(), this.durationRangeSlider.highValueProperty()));

        this.effectListView.setCellFactory(this.effectCellFactory);
    }

    /**
     * Applies a decoded preset to this configuration.
     */
    public void applyPreset(@Nonnull Preset preset) {
        GameConfiguration configuration = this.getGameConfiguration();

        if (configuration == null || !preset.getGameId().equals(configuration.getGame().getId())) {
            configuration = this.gameListView.getItems().stream()
                    .filter((c) -> c.getGame().getId().equals(preset.getGameId()))
                    .findAny()
                    .orElse(null);

            if (configuration == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(this.messageSource.getMessage("alert.game.invalid.title"));
                alert.setHeaderText(this.messageSource.getMessage("alert.game.invalid.header"));
                alert.setContentText(this.messageSource.getMessage("alert.game.invalid.description"));
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.show();
                return;
            } else if (configuration.getGame().getMetadata().getRevision() != preset.getRevision()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(this.messageSource.getMessage("alert.game.mismatch.title"));
                alert.setHeaderText(this.messageSource.getMessage("alert.game.mismatch.header"));
                alert.setContentText(this.messageSource.getMessage("alert.game.mismatch.description"));
                alert.initModality(Modality.APPLICATION_MODAL);

                if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                    return;
                }
            }

            this.gameListView.getSelectionModel().select(configuration);
        }

        configuration.setSeed(preset.getSeed());
        configuration.setEffectChance(preset.getEffectChance());
        configuration.setCombinationChance(preset.getCombinationChance());
        configuration.setDelayLow(preset.getDelayLow());
        configuration.setDelayHigh(preset.getDelayHigh());
        configuration.setDurationLow(preset.getDurationLow());
        configuration.setDurationHigh(preset.getDurationHigh());
        configuration.getEffectConfigurations().forEach((c) -> c.setActive(preset.getEffectIds().contains(c.getEffectFactory().getEffectId())));
    }

    // <editor-fold desc="Event Handlers & Bindings">

    /**
     * Checks whether the start button is available to users.
     */
    private boolean isStartAvailable() {
        Preset preset = this.getPreset();
        return preset == null || preset.getEffectIds().size() == 0;
    }

    /**
     * Handles a user's request to display information on the endorsement property of the selected
     * game.
     */
    @FXML
    private void onRequestEndorsementInformation(@Nonnull ActionEvent event) {
        this.endorsementPopOver.show(this.gameEndorsementButton, 1);
    }

    /**
     * Handles user requests to re-generate the configuration seed.
     */
    @FXML
    private void onRequestSeed(@Nonnull ActionEvent event) {
        GameConfiguration configuration = this.getGameConfiguration();

        if (configuration == null) {
            return;
        }

        configuration.generateSeed();
    }

    /**
     * Handles a user's request to start the configuration.
     */
    @FXML
    private void onRequestStart() {
        Runnable runnable = this.getOnStart();

        if (runnable == null) {
            return;
        }

        runnable.run();
    }

    /**
     * Updates all configuration bindings.
     */
    private void updateGameBindings(@Nonnull ObservableValue<? extends GameConfiguration> ob, @Nullable GameConfiguration o, @Nullable GameConfiguration n) {
        if (n == null) {
            this.preset.unbind();
            this.seedTextField.textProperty().unbind();
            this.effectChanceSlider.valueProperty().unbind();
            this.combinationChanceSlider.valueProperty().unbind();

            this.delayRangeSlider.lowValueProperty().unbind();
            this.delayRangeSlider.highValueProperty().unbind();

            this.durationRangeSlider.lowValueProperty().unbind();
            this.durationRangeSlider.highValueProperty().unbind();

            this.effectListView.setItems(null);
            return;
        }

        this.preset.bind(n.presetProperty());
        this.seedTextField.textProperty().bindBidirectional(n.seedProperty());
        this.effectChanceSlider.valueProperty().bindBidirectional(n.effectChanceProperty());
        this.combinationChanceSlider.valueProperty().bindBidirectional(n.combinationChanceProperty());

        this.delayRangeSlider.lowValueProperty().bindBidirectional(n.delayLowProperty());
        this.delayRangeSlider.highValueProperty().bindBidirectional(n.delayHighProperty());

        this.durationRangeSlider.lowValueProperty().bindBidirectional(n.durationLowProperty());
        this.durationRangeSlider.highValueProperty().bindBidirectional(n.durationHighProperty());

        this.effectListView.setItems(n.getEffectConfigurations());
    }

    /**
     * Updates the icon displayed within the configuration UI for the selected game.
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
     * Updates the game title displayed within the configuration UI for the selected game.
     */
    @Nullable
    private String updateGameName() {
        GameConfiguration configuration = this.getGameConfiguration();

        if (configuration == null) {
            return null;
        }

        return this.messageSource.getMessage(Game.getTitleLocalizationKey(configuration.getGame()));
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
    public Preset getPreset() {
        return this.preset.get();
    }

    @Nonnull
    public ReadOnlyObjectProperty<Preset> presetProperty() {
        return this.preset;
    }

    public void setPreset(@Nullable Preset preset) {
        this.preset.set(preset);
    }

    @Nullable
    public Runnable getOnStart() {
        return this.onStart.get();
    }

    @Nonnull
    public ObjectProperty<Runnable> onStartProperty() {
        return this.onStart;
    }

    public void setOnStart(@Nullable Runnable onStart) {
        this.onStart.set(onStart);
    }
    // </editor-fold>
}
