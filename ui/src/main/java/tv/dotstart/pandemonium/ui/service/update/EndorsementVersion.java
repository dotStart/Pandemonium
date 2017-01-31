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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnegative;

/**
 * Provides a simple POJO which may be used in order to retrieve the version of an endorsement
 * document.
 *
 * This type is used to evaluate compatibility with the server side endorsement document without
 * requiring chains of try-catch clauses.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class EndorsementVersion {
    private final int version;

    @JsonCreator
    private EndorsementVersion(@Nonnegative @JsonProperty(value = "version", required = true) int version) {
        this.version = version;
    }

    @Nonnegative
    public int getVersion() {
        return this.version;
    }
}
