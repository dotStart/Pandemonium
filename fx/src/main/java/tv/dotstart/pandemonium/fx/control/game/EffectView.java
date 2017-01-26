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

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Provides a view to display scheduled effects along with their progress at play time.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class EffectView extends ListView<ScheduledEffect> {

    public EffectView() {
        super();

        this.getStyleClass().add("effect-view");

        this.setCellFactory(param -> new ListCell<ScheduledEffect>() {
            @Override
            protected void updateItem(ScheduledEffect item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setGraphic(null);
                    return;
                }

                this.setGraphic(item);
            }
        });
    }
}
