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
package tv.dotstart.pandemonium.web;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.configuration.ApplicationConfiguration;

/**
 * Provides a configuration which acts as an entry-point to the spring web application.
 *
 * While this configuration is empty, it will bootstrap the web component of this application
 * through auto configurations when enabled within the application configuration.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Configuration
@EnableAutoConfiguration
public class WebConfiguration {

    /**
     * Provides an embedded servlet container factory which makes use of the configuration provided
     * by the application itself instead of making use of Spring's properties.
     */
    @Bean
    @Nonnull
    public EmbeddedServletContainerFactory embeddedServletContainerFactory(@Nonnull ApplicationConfiguration configuration) {
        return new JettyEmbeddedServletContainerFactory("", configuration.getWebPort());
    }
}
