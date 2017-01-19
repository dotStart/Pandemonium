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
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

import javax.annotation.Nonnull;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import tv.dotstart.pandemonium.fx.control.game.GameCell;
import tv.dotstart.pandemonium.game.Game;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class GameCellFactory implements Callback<ListView<Game>, ListCell<Game>> {
    private final MessageSource messageSource;

    @Autowired
    public GameCellFactory(@Nonnull MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListCell<Game> call(ListView<Game> param) {
        return new ListCell<Game>() {
            @Override
            protected void updateItem(Game item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    this.setGraphic(null);
                    return;
                }

                GameCell cell = new GameCell();
                cell.setImage(Game.getIcon(item));
                cell.setTitle(GameCellFactory.this.messageSource.getMessage(Game.getTitleLocalizationKey(item), new Object[0], Locale.ENGLISH)); // TODO: Language Configuration

                this.setGraphic(cell);
            }
        };
    }
}
