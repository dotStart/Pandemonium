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
package tv.dotstart.pandemonium.fx.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import tv.dotstart.pandemonium.fx.control.game.GameCell;
import tv.dotstart.pandemonium.fx.localization.ConfigurationAwareMessageSource;
import tv.dotstart.pandemonium.game.Game;
import tv.dotstart.pandemonium.game.GameConfiguration;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class GameCellFactory implements Callback<ListView<GameConfiguration>, ListCell<GameConfiguration>> {
    private final ConfigurationAwareMessageSource messageSource;

    @Autowired
    public GameCellFactory(@Nonnull ConfigurationAwareMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListCell<GameConfiguration> call(ListView<GameConfiguration> param) {
        return new ListCell<GameConfiguration>() {
            @Override
            protected void updateItem(GameConfiguration item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    this.setGraphic(null);
                    return;
                }

                GameCell cell = new GameCell();
                cell.setImage(Game.getIcon(item.getGame()));
                cell.setTitle(GameCellFactory.this.messageSource.getMessage(Game.getTitleLocalizationKey(item.getGame())));

                this.setGraphic(cell);
            }
        };
    }
}
