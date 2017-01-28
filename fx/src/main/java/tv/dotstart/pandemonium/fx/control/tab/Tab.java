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
package tv.dotstart.pandemonium.fx.control.tab;

import javax.annotation.Nonnull;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.StackPane;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class Tab extends StackPane {
    private final StringProperty title = new SimpleStringProperty();

    public String getTitle() {
        return this.title.get();
    }

    @Nonnull
    public StringProperty titleProperty() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }
}
