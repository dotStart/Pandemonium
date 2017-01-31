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

import org.controlsfx.control.ToggleSwitch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class EffectCell extends HBox implements Initializable {
    private final BooleanProperty active = new SimpleBooleanProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();

    // <editor-fold desc="FXML">
    @FXML
    private ToggleSwitch activeToggleSwitch;
    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;
    // </editor-fold>

    public EffectCell() {
        FXMLLoader loader = new FXMLLoader();

        loader.setCharset(StandardCharsets.UTF_8);
        loader.setController(this);
        loader.setRoot(this);

        try (InputStream inputStream = this.getClass().getResourceAsStream("EffectCell.fxml")) {
            loader.load(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load EffectCell control: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.activeToggleSwitch.selectedProperty().bindBidirectional(this.active);
        this.titleLabel.textProperty().bind(this.title);
        this.descriptionLabel.textProperty().bind(this.description);
    }

    // <editor-fold desc="Getters & Setters">
    public boolean isActive() {
        return this.active.get();
    }

    public BooleanProperty activeProperty() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public String getTitle() {
        return this.title.get();
    }

    public StringProperty titleProperty() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getDescription() {
        return this.description.get();
    }

    public StringProperty descriptionProperty() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }
    // </editor-fold>
}
