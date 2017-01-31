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
package tv.dotstart.pandemonium.ui.service.update;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
class DummyManifest implements EndorsementManifest {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<GameEndorsement> getEndorsement(@Nonnull UUID uuid) {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Set<GameEndorsement> getEndorsements() {
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ZonedDateTime getLastUpdate() {
        return ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersion() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEndorsed(@Nonnull UUID uuid) {
        return false;
    }
}
