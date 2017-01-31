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
package tv.dotstart.pandemonium.fx.localization;

import org.springframework.context.MessageSource;

import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * Provides a message source interface which is aware of the currently configured locale and as such
 * does not require a locale to be passed at runtime.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface ConfigurationAwareMessageSource extends MessageSource {

    /**
     * Retrieves the locale which is currently selected for this specific message source.
     */
    @Nonnull
    Locale getConfiguredLocale();

    /**
     * Retrieves a message from within the message source using the currently selected locale.
     *
     * When no translation is available for the requested message code, a standard value is expected
     * to be returned back.
     */
    @Nonnull
    String getMessage(@Nonnull String code, @Nonnull Object... arguments);
}
