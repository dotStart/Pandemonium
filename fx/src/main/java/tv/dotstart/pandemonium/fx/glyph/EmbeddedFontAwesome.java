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
package tv.dotstart.pandemonium.fx.glyph;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.fx.control.TitleBar;

/**
 * Provides an embedded version of the FontAwesome glyph font.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class EmbeddedFontAwesome extends FontAwesome {
    private static final EmbeddedFontAwesome INSTANCE;

    static {
        INSTANCE = new EmbeddedFontAwesome();
        GlyphFontRegistry.register(INSTANCE);
    }

    private EmbeddedFontAwesome() {
        super(TitleBar.class.getResource("/META-INF/resources/webjars/fontawesome/4.7.0/fonts/FontAwesome.otf").toExternalForm());
    }

    /**
     * Retrieves a reference to the glyph font.
     */
    @Nonnull
    public static EmbeddedFontAwesome getInstance() {
        return INSTANCE;
    }
}
