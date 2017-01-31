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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Provides a cell in order to represent games within list views or other selections.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class GameCell extends HBox implements Initializable {
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();

    // <editor-fold desc="FXML">
    @FXML
    private ImageView iconImageView;
    @FXML
    private Label titleLabel;
    // </editor-fold>

    public GameCell() {
        FXMLLoader loader = new FXMLLoader();

        loader.setCharset(StandardCharsets.UTF_8);
        loader.setController(this);
        loader.setRoot(this);

        try (InputStream inputStream = this.getClass().getResourceAsStream("GameCell.fxml")) {
            loader.load(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load GameCell control: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.iconImageView.imageProperty().bind(this.image);
        this.titleLabel.textProperty().bind(this.title);
    }

    // <editor-fold desc="Getters & Setters">

    @Nullable
    public Image getImage() {
        return this.image.get();
    }

    @Nonnull
    public ObjectProperty<Image> imageProperty() {
        return this.image;
    }

    public void setImage(@Nullable Image image) {
        this.image.set(image);
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
    // </editor-fold>
}
