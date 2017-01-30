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
package tv.dotstart.pandemonium.web.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * Provides a wrapper type for encapsulating an effect for the purposes of transporting its data to
 * to the web browser.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class EffectEntity {
    private final UUID id;
    private final String title;
    private final String description;

    public EffectEntity(@Nonnull UUID id, @Nonnull String title, @Nonnull String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    @Nonnull
    public UUID getId() {
        return this.id;
    }

    @Nonnull
    public String getTitle() {
        return this.title;
    }

    @Nonnull
    public String getDescription() {
        return this.description;
    }
}
