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
package tv.dotstart.pandemonium.ui.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.module.ModuleMessageSource;

/**
 * Provides beans in order to provide proper localization to the application UI.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Configuration
public class UILocalizationConfiguration {
    @Value("${localization.cache:true}")
    private boolean cache;

    /**
     * Provides a message source which is used during module development (e.g. when a module is
     * present within the classpath only).
     */
    @Bean
    @Nonnull
    public MessageSource gameMessageSource(@Nonnull ModuleMessageSource parentMessageSource) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("localization/game");
        messageSource.setBundleClassLoader(this.getClass().getClassLoader());

        if (!this.cache) {
            messageSource.setCacheMillis(1);
        }

        parentMessageSource.addSource(messageSource);
        return messageSource;
    }

    /**
     * Provides a message source for resolving UI messages.
     */
    @Bean
    @Nonnull
    public MessageSource uiMessageSource(@Nonnull ModuleMessageSource parentMessageSource) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("localization/ui");
        messageSource.setBundleClassLoader(this.getClass().getClassLoader());

        if (!this.cache) {
            messageSource.setCacheMillis(1);
        }

        parentMessageSource.addSource(messageSource);
        return messageSource;
    }
}
