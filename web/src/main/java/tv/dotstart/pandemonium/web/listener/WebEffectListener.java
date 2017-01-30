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
package tv.dotstart.pandemonium.web.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.effect.EffectFactory;
import tv.dotstart.pandemonium.event.RemoveEffectEvent;
import tv.dotstart.pandemonium.event.ScheduleEffectEvent;
import tv.dotstart.pandemonium.fx.control.game.ScheduledEffect;
import tv.dotstart.pandemonium.fx.localization.ConfigurationAwareMessageSource;
import tv.dotstart.pandemonium.web.entity.EffectEntity;
import tv.dotstart.pandemonium.web.entity.EffectProgressEntity;
import tv.dotstart.pandemonium.web.entity.EffectRemovalEntity;

/**
 * Provides an event listener which forwards all incoming information on effects to clients which
 * are currently connected to the websocket server.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class WebEffectListener {
    private final ConfigurationAwareMessageSource messageSource;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebEffectListener(@Nonnull ConfigurationAwareMessageSource messageSource, @Nonnull SimpMessagingTemplate messagingTemplate) {
        this.messageSource = messageSource;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles newly scheduled effects.
     */
    @EventListener
    public void onEffectSchedule(@Nonnull ScheduleEffectEvent event) {
        EffectEntity entity = new EffectEntity(event.getEffect().getEffectId(), this.messageSource.getMessage(EffectFactory.getTitleLocalizationKey(event.getEffect().getFactory())), this.messageSource.getMessage(EffectFactory.getDescriptionLocalizationKey(event.getEffect().getFactory())));
        this.messagingTemplate.convertAndSend("/topic/effect/schedule", entity);

        event.getEffect().stateProperty().addListener((ob) -> this.updateEffectState(event.getEffect()));
        event.getEffect().progressProperty().addListener((ob) -> this.updateEffectState(event.getEffect()));
    }

    /**
     * Handles removed effects.
     */
    @EventListener
    public void onEffectRemove(@Nonnull RemoveEffectEvent event) {
        this.messagingTemplate.convertAndSend("/topic/effect/remove", new EffectRemovalEntity(event.getEffect().getEffectId()));
    }

    /**
     * Sends an updated version of the effect state and progress to the client.
     */
    private void updateEffectState(@Nonnull ScheduledEffect effect) {
        this.messagingTemplate.convertAndSend("/topic/effect/progress", new EffectProgressEntity(effect.getEffectId(), effect.getProgress(), effect.getState()));
    }
}
