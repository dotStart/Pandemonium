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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import tv.dotstart.pandemonium.fx.glyph.WindowIcons;

/**
 * Provides the functionality of a default window manager title bar.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@DefaultProperty("buttons")
public class TitleBar extends HBox implements Initializable {
    private final ObjectProperty<Image> icon = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();

    private final ObservableList<Node> buttons = FXCollections.observableArrayList();

    private final BooleanProperty move = new SimpleBooleanProperty(true);
    private final BooleanProperty iconify = new SimpleBooleanProperty(true);
    private final BooleanProperty resize = new SimpleBooleanProperty(true);
    private final BooleanProperty close = new SimpleBooleanProperty(true);

    private final BooleanProperty stageFullscreen = new SimpleBooleanProperty();

    private double dragInitX;
    private double dragInitY;

    // <editor-fold desc="Event Handlers">
    private final ChangeListener<? super Window> windowChangeListener = this::onWindowChange;
    // </editor-fold>

    // <editor-fold desc="FXML">
    @FXML
    private ImageView iconImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private HBox buttonBox;

    @FXML
    private Button iconifyButton;
    @FXML
    private Button fullscreenButton;
    @FXML
    private Button restoreButton;
    @FXML
    private Button closeButton;
    // </editor-fold>

    public TitleBar() {
        FXMLLoader loader = new FXMLLoader();

        loader.setCharset(StandardCharsets.UTF_8);
        loader.setController(this);
        loader.setRoot(this);

        try (InputStream inputStream = this.getClass().getResourceAsStream("TitleBar.fxml")) {
            loader.load(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load TitleBar control: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.iconifyButton.setGraphic(WindowIcons.getInstance().create(WindowIcons.Glyph.ICONIFY));
        this.fullscreenButton.setGraphic(WindowIcons.getInstance().create(WindowIcons.Glyph.FULLSCREEN));
        this.restoreButton.setGraphic(WindowIcons.getInstance().create(WindowIcons.Glyph.RESTORE));
        this.closeButton.setGraphic(WindowIcons.getInstance().create(WindowIcons.Glyph.CLOSE));

        this.iconImageView.imageProperty().bind(this.icon);
        this.titleLabel.textProperty().bind(this.title);
        Bindings.bindContent(this.buttonBox.getChildren(), this.buttons);

        this.iconifyButton.visibleProperty().bind(this.iconify);
        this.fullscreenButton.visibleProperty().bind(this.resize.and(this.stageFullscreen.not()));
        this.restoreButton.visibleProperty().bind(this.resize.and(this.fullscreenButton.visibleProperty().not()));
        this.closeButton.visibleProperty().bind(this.close);

        this.iconifyButton.managedProperty().bind(this.iconifyButton.visibleProperty());
        this.fullscreenButton.managedProperty().bind(this.fullscreenButton.visibleProperty());
        this.restoreButton.managedProperty().bind(this.restoreButton.visibleProperty());
        this.closeButton.managedProperty().bind(this.closeButton.visibleProperty());

        this.sceneProperty().addListener(this::onSceneChange);
        this.onSceneChange(this.sceneProperty(), null, this.getScene());
    }

    // <editor-fold desc="Event Handlers">

    /**
     * Handles changes to the component's parent scene.
     */
    private void onSceneChange(@Nonnull ObservableValue<? extends Scene> ob, @Nullable Scene o, @Nullable Scene n) {
        if (o != null) {
            o.windowProperty().removeListener(this.windowChangeListener);
        }

        if (n != null) {
            n.windowProperty().addListener(this.windowChangeListener);
            this.onWindowChange(n.windowProperty(), null, n.getWindow());
        }
    }

    /**
     * Handles changes to the scene's parent window instance.
     */
    private void onWindowChange(@Nonnull ObservableValue<? extends Window> ob, @Nullable Window o, @Nullable Window n) {
        if (o instanceof Stage) {
            Stage s = (Stage) o;

            this.title.unbindBidirectional(s.titleProperty());
            this.stageFullscreen.unbind();
        }

        if (n instanceof Stage) {
            Stage s = (Stage) n;

            this.title.bindBidirectional(s.titleProperty());
            this.stageFullscreen.bind(s.maximizedProperty());
        }
    }

    /**
     * Handles mouse dragging on the title bar area.
     */
    @FXML
    private void onMouseDragged(@Nonnull MouseEvent event) {
        if (event.getButton() != MouseButton.MIDDLE) {
            this.getScene().getWindow().setX(event.getScreenX() - this.dragInitX);
            this.getScene().getWindow().setY(event.getScreenY() - this.dragInitY);
        }
    }

    /**
     * Handles mouse pressing on the title bar area.
     */
    @FXML
    private void onMousePressed(@Nonnull MouseEvent event) {
        if (event.getButton() != MouseButton.MIDDLE) {
            this.dragInitX = event.getSceneX();
            this.dragInitY = event.getSceneY();
        } else {
            this.getScene().getWindow().centerOnScreen();

            this.dragInitX = this.getScene().getWindow().getX();
            this.dragInitY = this.getScene().getWindow().getY();
        }
    }

    /**
     * Handles a user's request to close a stage.
     */
    @FXML
    private void onRequestClose(@Nonnull ActionEvent event) {
        Window window = this.getScene().getWindow();

        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    /**
     * Handles a user's request to switch to fullscreen.
     */
    @FXML
    private void onRequestFullscreen(@Nonnull ActionEvent event) {
        Window window = this.getScene().getWindow();

        if (window instanceof Stage) {
            ((Stage) window).setMaximized(true);
        }
    }

    /**
     * Handles a user's request to iconify a stage.
     */
    @FXML
    private void onRequestIconify(@Nonnull ActionEvent event) {
        Window window = this.getScene().getWindow();

        if (window instanceof Stage) {
            ((Stage) window).setIconified(true);
        }
    }

    /**
     * Handles a user's request to restore the previous size.
     */
    @FXML
    private void onRequestRestore(@Nonnull ActionEvent event) {
        Window window = this.getScene().getWindow();

        if (window instanceof Stage) {
            ((Stage) window).setMaximized(false);
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
