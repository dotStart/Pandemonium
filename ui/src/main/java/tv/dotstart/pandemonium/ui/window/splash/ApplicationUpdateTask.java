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
package tv.dotstart.pandemonium.ui.window.splash;

import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.ui.service.UpdateService;

/**
 * Performs an update check during application initialization.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class ApplicationUpdateTask implements InitializationTask {
    private final UpdateService updateService;

    public ApplicationUpdateTask(@Nonnull UpdateService updateService) {
        this.updateService = updateService;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getLocalizationKey() {
        return "update.application";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() throws Exception {
        this.updateService.performCheck();
    }
}
