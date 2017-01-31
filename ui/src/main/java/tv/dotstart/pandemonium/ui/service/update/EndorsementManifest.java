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

import com.google.common.io.Files;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.ui.service.update.v1.EndorsementManifestV1;

/**
 * Provides a base interface for receiving endorsement information from the update server.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface EndorsementManifest {

    /**
     * Retrieves a dummy manifest without any contents.
     */
    @Nonnull
    static EndorsementManifest dummy() {
        return new DummyManifest();
    }

    /**
     * Retrieves an endorsement for the specified game UUID (if any).
     */
    @Nonnull
    Optional<? extends GameEndorsement> getEndorsement(@Nonnull UUID uuid);

    /**
     * Retrieves a set of active endorsements.
     */
    @Nonnull
    Set<? extends GameEndorsement> getEndorsements();

    @Nonnull
    ZonedDateTime getLastUpdate();

    /**
     * Retrieves the version of this manifest.
     */
    @Nonnegative
    int getVersion();

    /**
     * Checks whether a game with a certain UUID is endorsed.
     */
    boolean isEndorsed(@Nonnull UUID uuid);

    /**
     * Loads a cached endorsement from disk.
     */
    @Nonnull
    static EndorsementManifest load(@Nonnull Path file) {
        try {
            return load(Files.toString(file.toFile(), StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load manifest: " + ex.getMessage(), ex);
        }
    }

    /**
     * Loads a manifest from its serialized string representation.
     */
    @Nonnull
    static EndorsementManifest load(@Nonnull String manifest) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        try {
            EndorsementVersion version = mapper.readValue(manifest, EndorsementVersion.class);

            switch (version.getVersion()) {
                case 1:
                    return mapper.readValue(manifest, EndorsementManifestV1.class);
                default:
                    throw new IllegalStateException("Unsupported manifest version: " + version.getVersion());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load manifest: " + ex.getMessage(), ex);
        }
    }

    /**
     * Fetches the latest manifest from the update servers.
     */
    @Nonnull
    static EndorsementManifest fetch() {
        try {
            Content content = Request.Get("https://dotStart.github.io/Pandemonium/endorsements.json")
                    .execute()
                    .returnContent();

            if (!"application/json".equalsIgnoreCase(content.getType().getMimeType())) {
                throw new IllegalStateException("Illegal manifest type: " + content.getType());
            }

            return load(content.asString());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to fetch manifest: " + ex.getMessage(), ex);
        }
    }

    /**
     * Writes the endorsement manifest back to disk.
     *
     * @throws IOException when writing or serializing fails.
     */
    default void save(@Nonnull Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        mapper.writeValue(path.toFile(), this);
    }
}
