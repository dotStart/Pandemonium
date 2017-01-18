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

import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class IconButton extends Button {
    private final StringProperty fontName = new SimpleStringProperty();
    private final StringProperty glyphName = new SimpleStringProperty();

    public IconButton() {
        this.graphicProperty().bind(Bindings.createObjectBinding(this::updateGraphic, this.fontName, this.glyphName));
    }

    /**
     * Updates the button graphic.
     */
    @Nullable
    private Node updateGraphic() {
        String fontName = this.getFontName();
        String glyphName = this.getGlyphName();

        if (fontName == null || glyphName == null) {
            return null;
        }

        GlyphFont font = GlyphFontRegistry.font(this.fontName.get());

        if (font == null) {
            return null;
        }

        return font.create(this.glyphName.get());
    }

    // <editor-fold desc="Getters & Setters">

    public String getFontName() {
        return this.fontName.get();
    }

    public StringProperty fontNameProperty() {
        return this.fontName;
    }

    public void setFontName(String fontName) {
        this.fontName.set(fontName);
    }

    public String getGlyphName() {
        return this.glyphName.get();
    }

    public StringProperty glyphNameProperty() {
        return this.glyphName;
    }

    public void setGlyphName(String glyphName) {
        this.glyphName.set(glyphName);
    }
    // </editor-fold>
}
