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
package tv.dotstart.pandemonium.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents the metadata associated with a game definition such as version, revision, project url,
 * reporting url and name of authors involved with its creation.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Immutable
@ThreadSafe
public class Metadata {
    private final String version;
    private final int revision;
    private final String projectUrl;
    private final String reportingUrl;
    private final Set<String> authors;

    public Metadata(@Nonnull String version, @Nonnegative int revision, @Nullable String projectUrl, @Nullable String reportingUrl, @Nonnull String... authors) {
        this.version = version;
        this.revision = revision;
        this.projectUrl = projectUrl;
        this.reportingUrl = reportingUrl;

        this.authors = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(authors)));
    }

    /**
     * Retrieves a set of authors who were involved in the creation of the game definition this
     * metadata object is referencing.
     *
     * When an empty set is returned, the definition will be attributed to "Unknown". It is,
     * however, recommended to always provide accurate author information.
     */
    @Nonnull
    public Set<String> getAuthors() {
        return this.authors;
    }

    /**
     * Retrieves a URL which refers to the definition's homepage.
     *
     * Most definitions will want to set this to their respective repository website (e.g. GitHub,
     * BitBucket or GitLab) as it will suffice as indication to the user.
     */
    @Nullable
    public String getProjectUrl() {
        return this.projectUrl;
    }

    /**
     * Retrieves a URL which refers to the definition's issue tracker.
     *
     * When set, this information will be provided to users as additional information (especially
     * when an uncaught exception is tracked back to a specific definition).
     */
    @Nonnull
    public String getReportingUrl() {
        return this.reportingUrl;
    }

    /**
     * Retrieves a compatibility revision which indicates compatibility in regards to effects and
     * their implementation.
     *
     * This revision should not be incremented unless effects are removed from the definition or
     * changed in a way that would otherwise provide an unfair advantage for users of either the
     * old or new version.
     *
     * If a mismatching preset is detected, the user is warned accordingly and a decode is only
     * attempted when the user actively confirms such behavior.
     */
    @Nonnegative
    public int getRevision() {
        return this.revision;
    }

    /**
     * Retrieves a human readable definition version.
     *
     * The values returned by this method are expected to be in valid <a
     * href="http://semver.org/spec/v2.0.0.html">SemVer</a> format as they might be used to check
     * for updates in future releases.
     */
    @Nonnull
    public String getVersion() {
        return this.version;
    }
}
