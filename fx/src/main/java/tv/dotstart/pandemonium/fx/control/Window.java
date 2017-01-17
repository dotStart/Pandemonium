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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.DefaultProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import tv.dotstart.pandemonium.fx.glyph.WindowIcons;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@DefaultProperty("content")
public class Window extends VBox implements Initializable {
    private final ObjectProperty<Image> icon = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();

    private final ObservableList<Node> buttons = FXCollections.observableArrayList();
    private final ObservableList<Node> content = FXCollections.observableArrayList();

    private final BooleanProperty move = new SimpleBooleanProperty(true);
    private final BooleanProperty iconify = new SimpleBooleanProperty(true);
    private final BooleanProperty resize = new SimpleBooleanProperty(true);
    private final BooleanProperty close = new SimpleBooleanProperty(true);

    private double width;
    private double height;
    private double dragInitX;
    private double dragInitY;

    // <editor-fold text="FXML">
    @FXML
    private TitleBar titleBar;
    @FXML
    private VBox contentBox;
    @FXML
    private Label resizeGrip;
    // </editor-fold>

    public Window() {
        FXMLLoader loader = new FXMLLoader();

        loader.setCharset(StandardCharsets.UTF_8);
        loader.setController(this);
        loader.setRoot(this);

        try (InputStream inputStream = this.getClass().getResourceAsStream("Window.fxml")) {
            loader.load(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load Window control: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.titleBar.iconProperty().bind(this.icon);
        this.titleBar.titleProperty().bindBidirectional(this.title);
        this.titleBar.moveProperty().bindBidirectional(this.move);
        this.titleBar.iconifyProperty().bind(this.iconify);
        this.titleBar.resizeProperty().bind(this.resize);
        this.titleBar.closeProperty().bind(this.close);

        Bindings.bindContent(this.titleBar.getButtons(), this.buttons);
        Bindings.bindContent(this.contentBox.getChildren(), this.content);

        this.resizeGrip.setGraphic(WindowIcons.getInstance().create(WindowIcons.Glyph.RESIZE_GRIP));
    }

    // <editor-fold desc="Event Handler">

    /**
     * Handles mouse dragging on the resize grip.
     */
    @FXML
    private void onMouseDragged(@Nonnull MouseEvent event) {
        this.getScene().getWindow().setWidth(this.width + (event.getScreenX() - this.dragInitX));
        this.getScene().getWindow().setHeight(this.height + (event.getScreenY() - this.dragInitY));
    }

    /**
     * handles mouse presses on the resize grip.
     */
    @FXML
    private void onMousePressed(@Nonnull MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            this.width = this.getScene().getWindow().getWidth();
            this.height = this.getScene().getWindow().getHeight();

            this.dragInitX = event.getScreenX();
            this.dragInitY = event.getScreenY();
        }
    }
    // </editor-fold>

    // <editor-fold desc="Getters & Setters">

    @Nullable
    public Image getIcon() {
        return this.icon.get();
    }

    @Nonnull
    public ObjectProperty<Image> iconProperty() {
        return this.icon;
    }

    public void setIcon(@Nullable Image icon) {
        this.icon.set(icon);
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
    public ObservableList<Node> getButtons() {
        return this.buttons;
    }

    @Nonnull
    public ObservableList<Node> getContent() {
        return this.content;
    }

    public boolean isMove() {
        return this.move.get();
    }

    @Nonnull
    public BooleanProperty moveProperty() {
        return this.move;
    }

    public void setMove(boolean move) {
        this.move.set(move);
    }

    public boolean isIconify() {
        return this.iconify.get();
    }

    @Nonnull
    public BooleanProperty iconifyProperty() {
        return this.iconify;
    }

    public void setIconify(boolean iconify) {
        this.iconify.set(iconify);
    }

    public boolean isResize() {
        return this.resize.get();
    }

    @Nonnull
    public BooleanProperty resizeProperty() {
        return this.resize;
    }

    public void setResize(boolean resize) {
        this.resize.set(resize);
    }

    public boolean isClose() {
        return this.close.get();
    }

    @Nonnull
    public BooleanProperty closeProperty() {
        return this.close;
    }

    public void setClose(boolean close) {
        this.close.set(close);
    }
    // </editor-fold>
}
