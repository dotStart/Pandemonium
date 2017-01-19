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
import tv.dotstart.pandemonium.effect.EffectConfiguration;
import tv.dotstart.pandemonium.effect.EffectFactory;
import tv.dotstart.pandemonium.fx.control.game.EffectCell;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class EffectCellFactory implements Callback<ListView<EffectConfiguration>, ListCell<EffectConfiguration>> {
    private final MessageSource messageSource;

    @Autowired
    public EffectCellFactory(@Nonnull MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListCell<EffectConfiguration> call(ListView<EffectConfiguration> param) {
        return new ListCell<EffectConfiguration>() {
            @Override
            protected void updateItem(EffectConfiguration item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    this.setGraphic(null);
                    return;
                }

                EffectCell cell = new EffectCell();
                cell.setTitle(EffectCellFactory.this.messageSource.getMessage(EffectFactory.getTitleLocalizationKey(item.getEffectFactory()), new Object[0], Locale.ENGLISH)); // TODO
                cell.setDescription(EffectCellFactory.this.messageSource.getMessage(EffectFactory.getDescriptionLocalizationKey(item.getEffectFactory()), new Object[0], Locale.ENGLISH)); // TODO
                item.activeProperty().bind(cell.activeProperty());

                if (this.getIndex() == 0) {
                    cell.getStyleClass().add("first");
                }

                this.setGraphic(cell);
            }
        };
    }
}
