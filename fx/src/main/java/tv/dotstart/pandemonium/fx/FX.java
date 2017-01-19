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
package tv.dotstart.pandemonium.fx;

import org.controlsfx.control.PopOver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import tv.dotstart.pandemonium.fx.annotation.ApplicationWindow;

/**
 * Provides a helper implementation which is used to simplify the creation of application windows.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class FX {
    private static final String RESOURCE_PATTERN = "fxml/%s.fxml";

    private final ApplicationContext ctx;
    private final MessageSource messageSource;

    @Autowired
    public FX(@Nonnull ApplicationContext ctx, @Nonnull MessageSource messageSource) {
        this.ctx = ctx;
        this.messageSource = messageSource;
    }

    /**
     * Augments the scene in order to provide it with default values based on its root or other
     * circumstances.
     */
    @Nonnull
    private Scene augmentScene(@Nonnull Scene scene) {
        if (Platform.isSupported(ConditionalFeature.TRANSPARENT_WINDOW) && scene.getRoot() instanceof tv.dotstart.pandemonium.fx.control.Window) {
            scene.setFill(Color.TRANSPARENT);
        }

        return scene;
    }

    /**
     * Creates a new FXML loader instance which is augmented to utilize Spring injections to
     * construct its controller instances.
     */
    @Nonnull
    public FXMLLoader createLoader(@Nullable ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setCharset(StandardCharsets.UTF_8);
        loader.setControllerFactory(this.ctx::getBean);
        loader.setClassLoader(classLoader);
        loader.setResources(new MessageSourceResourceBundle(this.messageSource, Locale.ENGLISH)); // TODO: Locale Configuration
        return loader;
    }

    /**
     * Creates a new popover builder using a JavaFX controller definition.
     *
     * @throws IllegalArgumentException when the supplied resource name does not actually exist
     *                                  within the loader or the passed controller type is not
     *                                  annotated using {@link ApplicationWindow}.
     * @throws IllegalStateException    when the supplied resource fails loading.
     */
    @Nonnull
    public PopOverBuilder createPopOver(@Nonnull Class<?> controllerType) throws IllegalArgumentException, IllegalStateException {
        return new PopOverBuilder(this.loadResource(controllerType));
    }

    /**
     * Creates a new popover factory using an FXML document as its root.
     *
     * @throws IllegalArgumentException when the supplied resource name does not actually exist
     *                                  within the loader.
     * @throws IllegalStateException    when the supplied resource fails loading.
     */
    @Nonnull
    public PopOverBuilder createPopOver(@Nonnull String resourceName, @Nullable ClassLoader loader) throws IllegalArgumentException, IllegalStateException {
        return new PopOverBuilder(this.loadResource(resourceName, loader));
    }

    /**
     * Creates a new scene using a JavaFX controller definition.
     *
     * @throws IllegalArgumentException when the supplied resource name does not actually exist
     *                                  within the loader or the passed controller type is not
     *                                  annotated using {@link ApplicationWindow}.
     * @throws IllegalStateException    when the supplied resource fails loading.
     */
    @Nonnull
    public Scene createScene(@Nonnull Class<?> controllerType) throws IllegalArgumentException, IllegalStateException {
        return this.augmentScene(new Scene(this.loadResource(controllerType)));
    }

    /**
     * Creates a new scene using an FXML document.
     *
     * @throws IllegalArgumentException when the supplied resource name does not actually exist
     *                                  within the loader.
     * @throws IllegalStateException    when the supplied resource fails loading.
     */
    @Nonnull
    public Scene createScene(@Nonnull String resourceName, @Nullable ClassLoader loader) throws IllegalArgumentException, IllegalStateException {
        return this.augmentScene(new Scene(this.loadResource(resourceName, loader)));
    }

    /**
     * Creates a new stage builder using the supplied JavaFX controller.
     *
     * @throws IllegalArgumentException when the supplied resource name does not actually exist
     *                                  within the loader or the passed controller type is not
     *                                  annotated using {@link ApplicationWindow}.
     * @throws IllegalStateException    when the supplied resource fails loading.
     */
    @Nonnull
    public StageBuilder createStage(@Nonnull Class<?> controllerType) throws IllegalArgumentException, IllegalStateException {
        return new StageBuilder(this.createScene(controllerType));
    }

    /**
     * Creates a new stage builder using the supplied scene document.
     *
     * @throws IllegalArgumentException when the supplied resource name does not actually exist
     *                                  within the loader.
     * @throws IllegalStateException    when the supplied resource fails loading.
     */
    @Nonnull
    public StageBuilder createStage(@Nonnull String resourceName, @Nullable ClassLoader loader) throws IllegalArgumentException, IllegalStateException {
        return new StageBuilder(this.createScene(resourceName, loader));
    }

    /**
     * Retrieves the loader relative path of an FXML resource.
     */
    @Nonnull
    public String getResourcePath(@Nonnull String resourceName) {
        return String.format(RESOURCE_PATTERN, resourceName);
    }

    /**
     * Loads a certain FXML for a passed controller as declared through the respective window
     * annotation.
     *
     * All passed controller types are required to be annotated with {@link ApplicationWindow} in
     * order to indicate their respective FXML resource.
     *
     * @throws IllegalArgumentException when the supplied resource name does not actually exist
     *                                  within the loader or the passed controller type is not
     *                                  annotated using {@link ApplicationWindow}.
     * @throws IllegalStateException    when the supplied resource fails loading.
     */
    @Nonnull
    public <N extends Node> N loadResource(@Nonnull Class<?> controllerType) throws IllegalArgumentException, IllegalStateException {
        if (!controllerType.isAnnotationPresent(ApplicationWindow.class)) {
            throw new IllegalArgumentException("Supplied type is not marked using @ApplicationWindow");
        }

        ApplicationWindow window = AnnotationUtils.findAnnotation(controllerType, ApplicationWindow.class);
        String resourceName = window.resourceName();

        if (resourceName.isEmpty()) {
            resourceName = controllerType.getSimpleName();
        }

        return this.loadResource(resourceName, controllerType.getClassLoader());
    }

    /**
     * Loads a certain FXML resource from its standard path within the specified loader.
     *
     * @throws IllegalArgumentException when the supplied resource name does not actually exist
     *                                  within the loader.
     * @throws IllegalStateException    when the supplied resource fails loading.
     */
    @Nonnull
    public <N extends Node> N loadResource(@Nonnull String resourceName, @Nullable ClassLoader classLoader) throws IllegalArgumentException, IllegalStateException {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        String resourcePath = this.getResourcePath(resourceName);

        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Supplied resource \"" + resourceName + "\" (path \"" + resourcePath + "\") does not exist in current loader");
            }

            final FXMLLoader loader = this.createLoader(classLoader);
            return loader.load(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load FXML resource: " + ex.getMessage(), ex);
        }
    }

    /**
     * Provides a factory for popover instances.
     */
    public class PopOverBuilder {
        private final Node root;

        private String title = null;
        private PopOver.ArrowLocation arrowLocation = PopOver.ArrowLocation.TOP_CENTER;
        private boolean autoHide = true;

        PopOverBuilder(@Nonnull Node root) {
            this.root = root;
        }

        /**
         * Builds a new popover instance.
         */
        @Nonnull
        public PopOver build() {
            PopOver popOver = new PopOver(this.root);

            if (this.title != null) {
                popOver.setTitle(this.title);
            }

            if (this.arrowLocation != null) {
                popOver.setArrowLocation(this.arrowLocation);
            }

            if (!this.autoHide) {
                popOver.setAutoHide(false);
            }

            return popOver;
        }

        /**
         * Builds a new popover instance and shows it at the specified location within the window.
         */
        @Nonnegative
        public PopOver buildAndShow(@Nonnull Window window, @Nonnegative double anchorX, double anchorY) {
            PopOver popOver = this.build();
            popOver.show(window, anchorX, anchorY);
            return popOver;
        }

        /**
         * Builds a new popover instance and shows it at the specified node location.
         */
        @Nonnegative
        public PopOver buildAndShow(@Nonnull Node owner, @Nonnegative double anchorX, double anchorY) {
            PopOver popOver = this.build();
            popOver.show(owner, anchorX, anchorY);
            return popOver;
        }

        // <editor-fold desc="Getters & Setters">
        @Nullable
        public String getTitle() {
            return this.title;
        }

        public void setTitle(@Nullable String title) {
            this.title = title;
        }

        @Nullable
        public PopOver.ArrowLocation getArrowLocation() {
            return this.arrowLocation;
        }

        public void setArrowLocation(@Nullable PopOver.ArrowLocation arrowLocation) {
            this.arrowLocation = arrowLocation;
        }

        public boolean isAutoHide() {
            return this.autoHide;
        }

        public void setAutoHide(boolean autoHide) {
            this.autoHide = autoHide;
        }
        // </editor-fold>
    }

    /**
     * Provides a factory for stage instances.
     */
    public class StageBuilder {
        private final Scene scene;

        private boolean valid = true;
        private Window owner = null;

        private String title = null;
        private boolean alwaysOnTop = false;
        private Set<Image> icons = new HashSet<>();

        private Modality modality = null;
        private StageStyle style = null;

        StageBuilder(@Nonnull Scene scene) {
            this.scene = scene;

            if (Platform.isSupported(ConditionalFeature.TRANSPARENT_WINDOW) && scene.getRoot() instanceof tv.dotstart.pandemonium.fx.control.Window) {
                this.style = StageStyle.TRANSPARENT;
            }
        }

        /**
         * Builds a stage based on the builder specifications.
         *
         * @throws IllegalStateException when an attempt to reuse the builder is detected.
         */
        @Nonnull
        public Stage build() throws IllegalStateException {
            if (!this.valid) {
                throw new IllegalStateException("Cannot reuse builder");
            }

            Stage stage = new Stage();
            stage.setScene(this.scene);

            if (this.owner != null) {
                stage.initOwner(this.owner);
            }

            if (this.title != null) {
                stage.setTitle(this.title);
            }

            stage.setAlwaysOnTop(this.alwaysOnTop);
            stage.getIcons().addAll(this.icons);

            if (this.modality != null) {
                stage.initModality(this.modality);
            }

            if (this.style != null) {
                stage.initStyle(this.style);
            }

            this.valid = false;
            return stage;
        }

        /**
         * Builds the stage and makes it visible.
         */
        @Nonnull
        public Stage buildAndShow() throws IllegalStateException {
            Stage stage = this.build();
            stage.show();
            return stage;
        }

        // <editor-fold desc="Icons">

        /**
         * Adds an icon to the created stage.
         */
        @Nonnull
        public StageBuilder addIcon(@Nonnull Image icon) {
            this.icons.add(icon);
            return this;
        }

        /**
         * Removes an icon from the created stage.
         */
        @Nonnull
        public StageBuilder removeIcon(@Nonnull Image icon) {
            this.icons.remove(icon);
            return this;
        }
        // </editor-fold>

        // <editor-fold desc="Getters & Setters">
        @Nullable
        public Window getOwner() {
            return this.owner;
        }

        @Nullable
        public String getTitle() {
            return this.title;
        }

        public boolean isAlwaysOnTop() {
            return this.alwaysOnTop;
        }

        @Nullable
        public StageStyle getStyle() {
            return this.style;
        }

        @Nullable
        public Modality getModality() {
            return this.modality;
        }

        @Nonnull
        public StageBuilder setOwner(@Nullable Window owner) {
            this.owner = owner;
            return this;
        }

        @Nonnull
        public StageBuilder setTitle(@Nullable String title) {
            this.title = title;
            return this;
        }

        @Nonnull
        public StageBuilder setAlwaysOnTop(boolean alwaysOnTop) {
            this.alwaysOnTop = alwaysOnTop;
            return this;
        }

        @Nonnull
        public StageBuilder setStyle(@Nullable StageStyle style) {
            this.style = style;
            return this;
        }

        @Nonnull
        public StageBuilder setModality(@Nullable Modality modality) {
            this.modality = modality;
            return this;
        }
        // </editor-fold>

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;

            StageBuilder that = (StageBuilder) o;
            return this.valid == that.valid &&
                    this.alwaysOnTop == that.alwaysOnTop &&
                    Objects.equals(this.scene, that.scene) &&
                    Objects.equals(this.owner, that.owner) &&
                    Objects.equals(this.title, that.title) &&
                    Objects.equals(this.icons, that.icons) &&
                    this.modality == that.modality &&
                    this.style == that.style;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.scene, this.valid, this.owner, this.title, this.alwaysOnTop, this.icons, this.modality, this.style);
        }
    }
}
