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

import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.controlsfx.glyphfont.INamedCharacter;

import java.util.Arrays;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class WindowIcons extends GlyphFont {
    private static final WindowIcons INSTANCE;

    static {
        INSTANCE = new WindowIcons();
        GlyphFontRegistry.register(INSTANCE);
    }

    private WindowIcons() {
        super("Window Icons", 14, WindowIcons.class.getResource("WindowIcons.ttf").toExternalForm());
        this.registerAll(Arrays.asList(Glyph.values()));
    }

    /**
     * Retrieves the singleton instance of this font.
     */
    @Nonnull
    public static WindowIcons getInstance() {
        return INSTANCE;
    }

    public enum Glyph implements INamedCharacter {
        CLOSE('C'),
        FULLSCREEN('F'),
        ICONIFY('I'),
        RESTORE('R'),
        RESIZE_GRIP('r');

        private final char name;

        Glyph(char name) {
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public char getChar() {
            return this.name;
        }
    }
}
