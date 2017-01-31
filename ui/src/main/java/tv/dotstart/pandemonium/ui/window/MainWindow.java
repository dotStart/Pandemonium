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
import org.controlsfx.glyphfont.FontAwesome;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import tv.dotstart.pandemonium.fx.FX;
import tv.dotstart.pandemonium.fx.annotation.ApplicationWindow;
import tv.dotstart.pandemonium.fx.control.IconButton;
import tv.dotstart.pandemonium.fx.glyph.EmbeddedFontAwesome;
import tv.dotstart.pandemonium.fx.localization.ConfigurationAwareMessageSource;
import tv.dotstart.pandemonium.game.GameConfiguration;
import tv.dotstart.pandemonium.preset.Preset;
import tv.dotstart.pandemonium.ui.service.UpdateService;
import tv.dotstart.pandemonium.ui.window.main.ActivityView;
import tv.dotstart.pandemonium.ui.window.main.ConfigurationView;

/**
 * Provides a base controller which manages the shared space of both the configuration and activity
 * view.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@ApplicationWindow
public class MainWindow implements Initializable {
    private static final Logger logger = LogManager.getFormatterLogger(MainWindow.class);

    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final BooleanProperty generatedPreset = new SimpleBooleanProperty();

    private final FX fx;
    private final HostServices hostServices;
    private final ConfigurationAwareMessageSource messageSource;
    private final UpdateService updateService;

    // <editor-fold desc="FXML">
    @FXML
    private TextField presetTextField;
    @FXML
    private IconButton presetLoadIconButton;
    @FXML
    private IconButton presetCopyIconButton;
    @FXML
    private IconButton updateButton;
    @FXML
    private Button settingsButton;

    @FXML
    private StackPane activityView;
    @FXML
    private ActivityView activityViewController;

    @FXML
    private HBox configurationView;
    @FXML
    private ConfigurationView configurationViewController;
    // </editor-fold>

    private boolean skipButtonUpdate;

    @Autowired
    public MainWindow(@Nonnull FX fx, @Nonnull HostServices hostServices, @Nonnull ConfigurationAwareMessageSource messageSource, @Nonnull UpdateService updateService) {
        this.fx = fx;
        this.hostServices = hostServices;
        this.messageSource = messageSource;
        this.updateService = updateService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.presetTextField.textProperty().addListener(this::onWindowPresetChange);
        this.presetTextField.editableProperty().bind(this.activeProperty.not());

        this.presetLoadIconButton.visibleProperty().bind(this.generatedPreset.not().and(this.activeProperty.not()));
        this.presetLoadIconButton.managedProperty().bind(this.presetLoadIconButton.visibleProperty());
        this.presetCopyIconButton.visibleProperty().bind(this.presetLoadIconButton.visibleProperty().not());
        this.presetCopyIconButton.managedProperty().bind(this.presetCopyIconButton.visibleProperty());

        this.updateButton.visibleProperty().bind(this.updateService.updateUrlProperty().isNotEmpty());
        this.updateButton.managedProperty().bind(this.updateButton.visibleProperty());
        this.settingsButton.setGraphic(EmbeddedFontAwesome.getInstance().create(FontAwesome.Glyph.COGS));

        this.activityView.visibleProperty().bind(this.activeProperty);
        this.configurationView.visibleProperty().bind(this.activeProperty.not());

        this.configurationViewController.presetProperty().addListener(this::onConfigurationPresetChange);
        this.configurationViewController.setOnStart(this::onConfigurationStart);

        this.activityViewController.gameConfigurationProperty().bind(Bindings.createObjectBinding(this::updateGameConfiguration, this.activeProperty, this.configurationViewController.gameConfigurationProperty()));
        this.activityViewController.setOnStop(this::onConfigurationStop);
    }

    // <editor-fold desc="Event Handlers & Bindings">

    /**
     * Handles the user's request to start the configuration in question.
     */
    private void onConfigurationStart() {
        this.activeProperty.set(true);
    }

    /**
     * handles the user's request to stop the configuration in question.
     */
    private void onConfigurationStop() {
        this.activeProperty.set(false);
    }

    /**
     * Requests a copy of the current preset.
     */
    @FXML
    private void onRequestPresetCopy(@Nonnull ActionEvent event) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(this.presetTextField.getText());
        clipboard.setContent(content);
    }

    /**
     * Attempts to load this preset.
     */
    @FXML
    private void onRequestPresetLoad(@Nonnull ActionEvent event) {
        Preset preset;

        try {
            preset = Preset.load(this.presetTextField.getText());
        } catch (IllegalArgumentException | IllegalStateException | IndexOutOfBoundsException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            String errorType;
            if (ex instanceof IllegalArgumentException) {
                errorType = "version";
            } else {
                errorType = "malformed";
            }

            alert.setTitle(this.messageSource.getMessage("alert.preset." + errorType + ".title"));
            alert.setHeaderText(this.messageSource.getMessage("alert.preset." + errorType + ".header"));
            alert.setContentText(this.messageSource.getMessage("alert.preset." + errorType + ".description"));
            alert.initModality(Modality.APPLICATION_MODAL);

            logger.warn("Failed to load preset: " + ex.getMessage(), ex);
            alert.show();
            return;
        }

        this.configurationViewController.applyPreset(preset);
    }

    /**
     * Handles user requests to open the settings window.
     */
    @FXML
    private void onRequestSettings(@Nonnull ActionEvent event) {
        this.fx.createStage(SettingsWindow.class)
                .setModality(Modality.APPLICATION_MODAL)
                .setTitle(this.messageSource.getMessage("settings.title"))
                .buildAndShow();
    }

    /**
     * Handles user requests to download an updated version.
     */
    @FXML
    private void onRequestUpdate(@Nonnull ActionEvent event) {
        // TODO: Maybe rather refer to an update page?
        this.hostServices.showDocument("https://dotStart.github.io/Pandemonium/");
    }

    /**
     * Handles changes to the preset text field.
     */
    private void onWindowPresetChange(@Nonnull ObservableValue<? extends String> ob, @Nullable String o, @Nullable String n) {
        if (this.skipButtonUpdate) {
            return;
        }

        this.generatedPreset.set(false);
    }

    /**
     * Handles changes to the preset provided by the configuration view.
     */
    private void onConfigurationPresetChange(@Nonnull ObservableValue<? extends Preset> ob, @Nullable Preset o, @Nullable Preset n) {
        if (n != null) {
            this.skipButtonUpdate = true;
            this.presetTextField.setText(n.toString());
            this.skipButtonUpdate = false;
            this.generatedPreset.set(true);
        }
    }

    /**
     * Updates the activity game configuration.
     */
    @Nullable
    private GameConfiguration updateGameConfiguration() {
        if (!this.activeProperty.get()) {
            return null;
        }

        return this.configurationViewController.getGameConfiguration();
    }
    // </editor-fold>
}
