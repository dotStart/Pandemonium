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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Callback;
import tv.dotstart.pandemonium.configuration.ApplicationConfiguration;
import tv.dotstart.pandemonium.fx.annotation.ApplicationWindow;
import tv.dotstart.pandemonium.fx.localization.ConfigurationAwareMessageSource;
import tv.dotstart.pandemonium.game.Game;

/**
 * Provides a user interface for altering game settings.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@ApplicationWindow
public class SettingsWindow implements Initializable {
    private final ApplicationConfiguration applicationConfiguration;
    private final ConfigurationAwareMessageSource messageSource;
    private final HostServices hostServices;
    private final Collection<Game> games;

    private boolean localeWarningDisplayed;
    private boolean webWarningDisplayed;
    private int eeCounter;

    // <editor-fold desc="FXML">
    @FXML
    private tv.dotstart.pandemonium.fx.control.Window root;

    @FXML
    private ComboBox<Locale> globalLocaleComboBox;
    @FXML
    private ToggleSwitch globalUpdateApplicationToggleSwitch;
    @FXML
    private ToggleSwitch globalUpdateAddonToggleSwitch;
    @FXML
    private ToggleSwitch globalUpdateEndorsementToggleSwitch;
    @FXML
    private ToggleSwitch eeToggleSwitch;

    @FXML
    private ToggleSwitch audioScheduleToggleSwitch;
    @FXML
    private ToggleSwitch audioApplyToggleSwitch;
    @FXML
    private ToggleSwitch audioRevertToggleSwitch;
    @FXML
    private Slider audioVolumeSlider;
    @FXML
    private Label audioVolumeValueLabel;

    @FXML
    private TextField audioScheduleFileTextField;
    @FXML
    private Button audioScheduleFileBrowseButton;
    @FXML
    private Button audioScheduleFileClearButton;
    @FXML
    private TextField audioApplyFileTextField;
    @FXML
    private Button audioApplyFileBrowseButton;
    @FXML
    private Button audioApplyFileClearButton;
    @FXML
    private TextField audioRevertFileTextField;
    @FXML
    private Button audioRevertFileBrowseButton;
    @FXML
    private Button audioRevertFileClearButton;

    @FXML
    private ListView<Game> addonList;
    @FXML
    private VBox addonDetailBox;
    @FXML
    private Label addonNameLabel;
    @FXML
    private Label addonVersionLabel;
    @FXML
    private Label addonAuthorLabel;
    @FXML
    private Label addonProjectUrlNameLabel;
    @FXML
    private Hyperlink addonProjectUrlLabel;
    @FXML
    private Label addonReportingUrlNameLabel;
    @FXML
    private Hyperlink addonReportingUrlLabel;

    @FXML
    private ToggleSwitch webEnabledToggleSwitch;
    @FXML
    private TextField webAddressTextField;
    @FXML
    private TextField webPortTextField;
    @FXML
    private TextField webAddressEffectTextField;

    @FXML
    private Label aboutVersionLabel;
    @FXML
    private Label aboutDevelopersLabel;
    @FXML
    private Label aboutContributorsNameLabel;
    @FXML
    private Label aboutContributorsLabel;
    // </editor-fold>

    @Autowired
    public SettingsWindow(@Nonnull ApplicationConfiguration applicationConfiguration, @Nonnull ConfigurationAwareMessageSource messageSource, @Nonnull HostServices hostServices, @Nonnull Collection<Game> games) {
        this.applicationConfiguration = applicationConfiguration;
        this.messageSource = messageSource;
        this.hostServices = hostServices;
        this.games = games;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Global
        this.globalLocaleComboBox.valueProperty().bindBidirectional(this.applicationConfiguration.applicationLocaleProperty());
        this.globalUpdateApplicationToggleSwitch.selectedProperty().bindBidirectional(this.applicationConfiguration.applicationCheckUpdatesProperty());
        this.globalUpdateAddonToggleSwitch.selectedProperty().bindBidirectional(this.applicationConfiguration.applicationCheckAddonUpdatesProperty());
        this.globalUpdateEndorsementToggleSwitch.selectedProperty().bindBidirectional(this.applicationConfiguration.applicationCheckEndorsementUpdatesProperty());

        final Callback<ListView<Locale>, ListCell<Locale>> localeCellFactory = param -> new ListCell<Locale>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setText(null);
                    return;
                }

                this.setText(item.getDisplayLanguage(item));
            }
        };

        this.globalLocaleComboBox.setButtonCell(localeCellFactory.call(null));
        this.globalLocaleComboBox.setCellFactory(localeCellFactory);
        this.globalLocaleComboBox.setItems(FXCollections.observableArrayList(ApplicationConfiguration.getSupportedLocales()));

        this.globalLocaleComboBox.valueProperty().addListener(observable -> {
            if (this.localeWarningDisplayed) {
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(this.messageSource.getMessage("alert.locale.restart.title"));
            alert.setHeaderText(this.messageSource.getMessage("alert.locale.restart.header"));
            alert.setContentText(this.messageSource.getMessage("alert.locale.restart.description"));

            alert.initModality(Modality.APPLICATION_MODAL);
            alert.show();

            this.localeWarningDisplayed = true;
        });

        this.eeToggleSwitch.selectedProperty().addListener(observable -> {
            if (++this.eeCounter == 32) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Stop doing that");
                alert.setHeaderText("It feels funny");
                alert.setContentText("So please don't, alright?");

                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(
                        ButtonType.OK,
                        new ButtonType("Sod Off", ButtonBar.ButtonData.CANCEL_CLOSE)
                );

                alert.initModality(Modality.APPLICATION_MODAL);
                if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.OK) {
                    this.eeToggleSwitch.setDisable(true);
                }
            }
        });

        // Audio
        this.audioScheduleToggleSwitch.selectedProperty().bindBidirectional(this.applicationConfiguration.audioPlayScheduleProperty());
        this.audioApplyToggleSwitch.selectedProperty().bindBidirectional(this.applicationConfiguration.audioPlayApplyProperty());
        this.audioRevertToggleSwitch.selectedProperty().bindBidirectional(this.applicationConfiguration.audioPlayRevertProperty());
        this.audioVolumeSlider.valueProperty().bindBidirectional(this.applicationConfiguration.audioVolumeProperty());
        this.audioVolumeValueLabel.textProperty().bind(Bindings.createStringBinding(this::updateAudioVolumeLabelText, this.audioVolumeSlider.valueProperty()));

        this.audioScheduleFileTextField.textProperty().bindBidirectional(this.applicationConfiguration.audioClipScheduleProperty());
        this.audioApplyFileTextField.textProperty().bindBidirectional(this.applicationConfiguration.audioClipApplyProperty());
        this.audioRevertFileTextField.textProperty().bindBidirectional(this.applicationConfiguration.audioClipRevertProperty());

        this.audioScheduleFileTextField.disableProperty().bind(this.audioScheduleToggleSwitch.selectedProperty().not());
        this.audioScheduleFileBrowseButton.disableProperty().bind(this.audioScheduleToggleSwitch.selectedProperty().not());
        this.audioApplyFileTextField.disableProperty().bind(this.audioApplyToggleSwitch.selectedProperty().not());
        this.audioApplyFileBrowseButton.disableProperty().bind(this.audioApplyToggleSwitch.selectedProperty().not());
        this.audioRevertFileTextField.disableProperty().bind(this.audioRevertToggleSwitch.selectedProperty().not());
        this.audioRevertFileBrowseButton.disableProperty().bind(this.audioRevertToggleSwitch.selectedProperty().not());

        this.audioScheduleFileClearButton.disableProperty().bind(this.audioScheduleFileTextField.textProperty().isEmpty().or(this.audioScheduleToggleSwitch.selectedProperty().not()));
        this.audioApplyFileClearButton.disableProperty().bind(this.audioApplyFileTextField.textProperty().isEmpty().or(this.audioApplyToggleSwitch.selectedProperty().not()));
        this.audioRevertFileClearButton.disableProperty().bind(this.audioRevertFileTextField.textProperty().isEmpty().or(this.audioRevertToggleSwitch.selectedProperty().not()));

        // Addon List
        this.addonList.setItems(FXCollections.observableArrayList(this.games));
        this.addonDetailBox.visibleProperty().bind(this.addonList.getSelectionModel().selectedItemProperty().isNotNull());

        this.addonProjectUrlNameLabel.visibleProperty().bind(this.addonProjectUrlLabel.textProperty().isNotEmpty());
        this.addonProjectUrlNameLabel.managedProperty().bind(this.addonProjectUrlNameLabel.visibleProperty());
        this.addonProjectUrlLabel.visibleProperty().bind(this.addonProjectUrlLabel.textProperty().isNotEmpty());
        this.addonProjectUrlLabel.managedProperty().bind(this.addonProjectUrlLabel.visibleProperty());
        this.addonReportingUrlNameLabel.visibleProperty().bind(this.addonReportingUrlLabel.textProperty().isNotEmpty());
        this.addonReportingUrlNameLabel.managedProperty().bind(this.addonReportingUrlNameLabel.visibleProperty());
        this.addonReportingUrlLabel.visibleProperty().bind(this.addonReportingUrlLabel.textProperty().isNotEmpty());
        this.addonReportingUrlLabel.managedProperty().bind(this.addonReportingUrlLabel.visibleProperty());

        this.addonNameLabel.textProperty().bind(Bindings.createStringBinding(this::updateAddonNameLabel, this.addonList.getSelectionModel().selectedItemProperty()));
        this.addonVersionLabel.textProperty().bind(Bindings.createStringBinding(this::updateAddonVersionLabel, this.addonList.getSelectionModel().selectedItemProperty()));
        this.addonAuthorLabel.textProperty().bind(Bindings.createStringBinding(this::updateAddonAuthorLabel, this.addonList.getSelectionModel().selectedItemProperty()));
        this.addonProjectUrlLabel.textProperty().bind(Bindings.createStringBinding(this::updateAddonProjectUrlLabel, this.addonList.getSelectionModel().selectedItemProperty()));
        this.addonReportingUrlLabel.textProperty().bind(Bindings.createStringBinding(this::updateAddonReportingUrlLabel, this.addonList.getSelectionModel().selectedItemProperty()));

        this.addonList.setCellFactory(param -> new ListCell<Game>() {
            @Override
            protected void updateItem(Game item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setText(null);
                    return;
                }

                this.setText(SettingsWindow.this.messageSource.getMessage(Game.getTitleLocalizationKey(item)));
            }
        });

        // Web
        this.webEnabledToggleSwitch.selectedProperty().bindBidirectional(this.applicationConfiguration.webEnabledProperty());
        this.webAddressTextField.textProperty().bindBidirectional(this.applicationConfiguration.webAddressProperty());
        this.webPortTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.applicationConfiguration.setWebPort(Integer.parseUnsignedInt(newValue));
            } catch (NumberFormatException ex) {
                this.webPortTextField.setText(oldValue);
            }
        });

        this.webAddressTextField.disableProperty().bind(this.applicationConfiguration.webEnabledProperty().not());
        this.webPortTextField.disableProperty().bind(this.applicationConfiguration.webEnabledProperty().not());
        this.webAddressEffectTextField.textProperty().bind(Bindings.createStringBinding(this::updateWebAddressEffectTextField, this.webAddressTextField.textProperty(), this.webPortTextField.textProperty()));

        this.webEnabledToggleSwitch.selectedProperty().addListener(observable -> {
            if (this.webWarningDisplayed) {
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(this.messageSource.getMessage("alert.web.restart.title"));
            alert.setHeaderText(this.messageSource.getMessage("alert.web.restart.header"));
            alert.setContentText(this.messageSource.getMessage("alert.web.restart.description"));

            alert.initModality(Modality.APPLICATION_MODAL);
            alert.show();

            this.webWarningDisplayed = true;
        });

        // About
        Package p = this.getClass().getPackage();
        this.aboutVersionLabel.setText(p.getImplementationVersion());
        this.aboutContributorsNameLabel.visibleProperty().bind(this.aboutContributorsLabel.textProperty().isNotEmpty());
        this.aboutContributorsNameLabel.managedProperty().bind(this.aboutContributorsNameLabel.visibleProperty());
        this.aboutContributorsLabel.visibleProperty().bind(this.aboutContributorsLabel.textProperty().isNotEmpty());
        this.aboutContributorsLabel.managedProperty().bind(this.aboutContributorsLabel.visibleProperty());

        try (InputStream inputStream = this.getClass().getResourceAsStream("/developers.json")) {
            ObjectMapper mapper = new ObjectMapper();
            AttributionMetadata metadata = mapper.readValue(inputStream, AttributionMetadata.class);

            this.aboutDevelopersLabel.setText(
                    metadata.getDevelopers().stream()
                            .map(AttributionMetadata.Developer::toString)
                            .collect(Collectors.joining(", "))
            );
            this.aboutContributorsLabel.setText(
                    metadata.getContributors().stream()
                            .map(AttributionMetadata.Developer::toString)
                            .collect(Collectors.joining(", "))
            );
        } catch (IOException ex) {
            LogManager.getFormatterLogger(SettingsWindow.class).error("Failed to load developer information: " + ex.getMessage(), ex);
        }
    }

    /**
     * Opens a file browser which stores its result in the target property.
     */
    private void showBrowseClipDialog(@Nonnull StringProperty property) {
        Path value = (property.get() == null || property.get().isEmpty() ? null : Paths.get(property.get()));

        FileChooser chooser = new FileChooser();
        chooser.setTitle(this.messageSource.getMessage("settings.browse.clip.title"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(this.messageSource.getMessage("settings.browse.clip.type"), "*.aac", "*.aif", "*.aiff", "*.mp3", "*.wav"));

        if (value != null) {
            chooser.setInitialDirectory(value.getParent().toFile());
            chooser.setInitialFileName(value.getFileName().toString());
        } else {
            chooser.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        }

        Window window = this.root.getScene().getWindow();
        File file = chooser.showOpenDialog(window);

        if (file != null) {
            property.set(file.getAbsolutePath());
        }
    }

    // <editor-fold desc="Event Handlers">
    @FXML
    private void onAudioScheduleClipBrowse(@Nonnull ActionEvent event) {
        this.showBrowseClipDialog(this.audioScheduleFileTextField.textProperty());
    }

    @FXML
    private void onAudioScheduleClipClear(@Nonnull ActionEvent event) {
        this.audioScheduleFileTextField.setText(null);
    }

    @FXML
    private void onAudioApplyClipBrowse(@Nonnull ActionEvent event) {
        this.showBrowseClipDialog(this.audioApplyFileTextField.textProperty());
    }

    @FXML
    private void onAudioApplyClipClear(@Nonnull ActionEvent event) {
        this.audioApplyFileTextField.setText(null);
    }

    @FXML
    private void onAudioRevertClipBrowse(@Nonnull ActionEvent event) {
        this.showBrowseClipDialog(this.audioRevertFileTextField.textProperty());
    }

    @FXML
    private void onAudioRevertClipClear(@Nonnull ActionEvent event) {
        this.audioRevertFileTextField.setText(null);
    }

    @FXML
    private void onAddonOpenProjectUrl(@Nonnull ActionEvent event) {
        Game game = this.addonList.getSelectionModel().getSelectedItem();

        if (game == null) {
            return;
        }

        this.hostServices.showDocument(game.getMetadata().getProjectUrl());
    }

    @FXML
    private void onAddonOpenReportingUrl(@Nonnull ActionEvent event) {
        Game game = this.addonList.getSelectionModel().getSelectedItem();

        if (game == null) {
            return;
        }

        this.hostServices.showDocument(game.getMetadata().getReportingUrl());
    }

    @FXML
    private void onCopyrightView(@Nonnull ActionEvent event) {
        try {
            Desktop.getDesktop().open(Paths.get("..", "THIRD-PARTY.txt").toFile());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to display copyright information: " + ex.getMessage(), ex);
        }
    }

    @FXML
    private void onWebGuide() {
        this.hostServices.showDocument("https://github.com/dotStart/Pandemonium/wiki/Stream-Guide");
    }
    // </editor-fold>

    // <editor-fold desc="Bindings">
    @Nullable
    private String updateAudioVolumeLabelText() {
        return String.format("%d%%", (int) this.audioVolumeSlider.getValue());
    }

    @Nullable
    private String updateWebAddressEffectTextField() {
        String address = this.webAddressTextField.getText();
        String port = this.webPortTextField.getText();

        if ("0.0.0.0".equals(address) || "127.0.0.1".equals(address)) {
            address = "localhost";
        }

        return "http://" + address + (!"80".equals(port) ? ":" + port : "") + "/effect";
    }

    @Nullable
    private String updateAddonNameLabel() {
        Game game = this.addonList.getSelectionModel().getSelectedItem();

        if (game == null) {
            return null;
        }

        return this.messageSource.getMessage(Game.getTitleLocalizationKey(game));
    }

    @Nullable
    private String updateAddonVersionLabel() {
        Game game = this.addonList.getSelectionModel().getSelectedItem();

        if (game == null) {
            return null;
        }

        return game.getMetadata().getVersion();
    }

    @Nullable
    private String updateAddonAuthorLabel() {
        Game game = this.addonList.getSelectionModel().getSelectedItem();

        if (game == null) {
            return null;
        }

        return game.getMetadata().getAuthors().stream().collect(Collectors.joining(", "));
    }

    @Nullable
    private String updateAddonProjectUrlLabel() {
        Game game = this.addonList.getSelectionModel().getSelectedItem();

        if (game == null) {
            return null;
        }

        return game.getMetadata().getProjectUrl();
    }

    @Nullable
    private String updateAddonReportingUrlLabel() {
        Game game = this.addonList.getSelectionModel().getSelectedItem();

        if (game == null) {
            return null;
        }

        return game.getMetadata().getReportingUrl();
    }
    // </editor-fold>

    /**
     * Provides a representation for a list of active developers and contributors.
     */
    public static class AttributionMetadata {
        private final List<Developer> developers;
        private final List<Developer> contributors;

        @JsonCreator
        public AttributionMetadata(@Nonnull @JsonProperty(value = "developers", required = true) List<Developer> developers, @Nonnull @JsonProperty(value = "contributors", required = true) List<Developer> contributors) {
            this.developers = developers;
            this.contributors = contributors;

            final Comparator<Developer> comparator = (d0, d1) -> {
                if (d0.alias == null && d1.alias == null) {
                    int d = d0.lastName.compareTo(d1.lastName);

                    if (d != 0) {
                        return d;
                    }

                    return d0.firstName.compareTo(d1.firstName);
                }

                if (d0.alias == null) {
                    return 1;
                }

                if (d1.alias == null) {
                    return -1;
                }

                return d0.alias.compareTo(d1.alias);
            };

            this.developers.sort(comparator);
            this.contributors.sort(comparator);
        }

        @Nonnull
        public List<Developer> getDevelopers() {
            return this.developers;
        }

        @Nonnull
        public List<Developer> getContributors() {
            return this.contributors;
        }

        /**
         * Represents a developer or contributor.
         */
        public static class Developer {
            private final String alias;
            private final String firstName;
            private final String lastName;

            @JsonCreator
            public Developer(@Nullable @JsonProperty("alias") String alias, @Nullable @JsonProperty("firstName") String firstName, @Nullable @JsonProperty("lastName") String lastName) {
                this.alias = alias;
                this.firstName = firstName;
                this.lastName = lastName;
            }

            @Nullable
            public String getAlias() {
                return this.alias;
            }

            @Nullable
            public String getFirstName() {
                return this.firstName;
            }

            @Nullable
            public String getLastName() {
                return this.lastName;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                if (this.firstName == null || this.lastName == null) {
                    return this.alias;
                }

                if (this.alias == null) {
                    return this.firstName + " " + this.lastName;
                }

                return this.firstName + " \"" + this.alias + "\" " + this.lastName;
            }
        }
    }
}
