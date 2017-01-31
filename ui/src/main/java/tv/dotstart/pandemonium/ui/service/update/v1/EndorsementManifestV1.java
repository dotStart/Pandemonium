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
package tv.dotstart.pandemonium.ui.service.update.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.ui.service.update.EndorsementManifest;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class EndorsementManifestV1 implements EndorsementManifest {
    private final ZonedDateTime lastUpdate;
    private final Set<GameEndorsementV1> endorsements;

    @JsonCreator
    public EndorsementManifestV1(@Nonnegative @JsonProperty(value = "version", required = true) int version, @Nonnull @JsonProperty(value = "lastUpdate", required = true) String lastUpdate, @Nonnull @JsonProperty(value = "games", required = true) Set<GameEndorsementV1> endorsements) {
        this.lastUpdate = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(lastUpdate, ZonedDateTime::from);
        this.endorsements = endorsements;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<GameEndorsementV1> getEndorsement(@Nonnull UUID uuid) {
        return this.endorsements.stream()
                .filter((e) -> uuid.equals(e.getId()))
                .findAny();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    @JsonProperty("games")
    public Set<GameEndorsementV1> getEndorsements() {
        return Collections.unmodifiableSet(this.endorsements);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ZonedDateTime getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonProperty("version")
    public int getVersion() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEndorsed(@Nonnull UUID uuid) {
        return this.getEndorsement(uuid).isPresent();
    }
}
