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
package tv.dotstart.pandemonium.ui.service;

import com.github.zafarkhaja.semver.Version;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Release;
import com.jcabi.github.Releases;
import com.jcabi.github.RtGithub;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import tv.dotstart.pandemonium.configuration.ApplicationConfiguration;
import tv.dotstart.pandemonium.ui.service.update.EndorsementManifest;

/**
 * Validates whether a new update for the application is available and if so makes this information
 * available by providing a download URL which may be directly referenced in the application UI.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Service
public class UpdateService {
    private static final Logger logger = LogManager.getFormatterLogger(UpdateService.class);

    private final StringProperty updateUrl = new SimpleStringProperty();
    private final ObjectProperty<EndorsementManifest> endorsementManifest = new SimpleObjectProperty<>();

    public UpdateService() {
        Path cacheFile = getEndorsementCacheFile();
        EndorsementManifest manifest = EndorsementManifest.dummy();

        if (Files.notExists(cacheFile)) {
            logger.warn("No locally cached endorsements available - Assuming defaults");
        } else {
            logger.info("Loading cached endorsement information");
            manifest = EndorsementManifest.load(cacheFile);
        }

        this.endorsementManifest.set(manifest);
    }

    /**
     * Retrieves the file responsible for storing the endorsement cache.
     */
    @Nonnull
    public static Path getEndorsementCacheFile() {
        return ApplicationConfiguration.getStoragePath().resolve("endorsements.json");
    }

    /**
     * Fetches an updated manifest from the update servers.
     */
    public void fetchEndorsements() {
        EndorsementManifest currentManifest = this.endorsementManifest.get();

        try {
            EndorsementManifest manifest = EndorsementManifest.fetch();

            if (!manifest.getLastUpdate().isAfter(currentManifest.getLastUpdate())) {
                logger.info("Locally cached manifest is up to date - No action required");
                return;
            }

            logger.info("Retrieved an updated manifest (last updated at %s)", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(manifest.getLastUpdate()));
            this.endorsementManifest.set(manifest);

            manifest.save(getEndorsementCacheFile());
        } catch (IllegalStateException ex) {
            logger.warn("Failed to fetch endorsement manifest: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.warn("Failed to write cached endorsement manifest: " + ex.getMessage(), ex);
        }
    }

    /**
     * Performs an update check.
     *
     * @throws IOException when contacting the GitHub servers fails.
     */
    public void performCheck() throws IOException {
        logger.info("Checking for application updates");

        // load the local application version from the jar manifest
        String implementationVersion = this.getClass().getPackage().getImplementationVersion();

        if (implementationVersion == null) {
            logger.warn("Application is running in a development environment (version is not locally available)");
            logger.warn("Update checks will be skipped");
            return;
        }

        Version localVersion = Version.valueOf(implementationVersion);
        logger.info("Local version: %s", localVersion.toString());

        // query GitHub for releases
        Releases releases =
                new RtGithub()
                        .repos()
                        .get(new Coordinates.Simple("dotStart", "Pandemonium"))
                        .releases();

        // iterate all releases and verify their respective state
        StreamSupport.stream(releases.iterate().spliterator(), false)
                .map(Release.Smart::new)
                .filter((r) -> {
                    try {
                        String name = null;

                        // retrieve a name for this release (prefer the tag name since it is usually
                        // in the right format already)
                        if (r.hasTag()) {
                            name = r.tag();
                        } else if (r.hasName()) {
                            name = r.name();
                        }

                        // validate whether we actually managed to get a value
                        if (name == null) {
                            return false;
                        }

                        // remove the "v" prefix if present (which it will probably be if we are making
                        // use of the tag name)
                        if (name.startsWith("v")) {
                            name = name.substring(1);
                        }

                        // parse and compare the versions
                        logger.info("Evaluating release \"%s\"", name);
                        Version version = Version.valueOf(name);

                        return version.greaterThan(localVersion);
                    } catch (IOException ex) {
                        logger.warn("Failed to evaluate release #" + r.number() + ": " + ex.getMessage(), ex);
                        return false;
                    }
                })
                .findAny()
                .ifPresent((r) -> {
                    try {
                        String url = r.htmlUrl().toExternalForm();
                        logger.info("A newer application version is available via %s", url);

                        // TODO: Evaluate channel

                        Platform.runLater(() -> this.updateUrl.set(url));
                    } catch (IOException ex) {
                        logger.warn("Failed to access updated release #" + r.number() + ": " + ex.getMessage(), ex);
                    }
                });
    }

    // <editor-fold desc="Getters & Setters">
    @Nullable
    public String getUpdateUrl() {
        return this.updateUrl.get();
    }

    @Nonnull
    public ReadOnlyStringProperty updateUrlProperty() {
        return this.updateUrl;
    }

    @Nonnull
    public EndorsementManifest getEndorsementManifest() {
        return this.endorsementManifest.get();
    }

    @Nonnull
    public ReadOnlyObjectProperty<EndorsementManifest> endorsementManifestProperty() {
        return this.endorsementManifest;
    }
    // </editor-fold>
}
