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

/**
 * Provides a base interface for tasks which are executed during application initialization.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface InitializationTask {

    /**
     * Retrieves the localization key which is resolved to display in the UI while this task is
     * running.
     */
    @Nonnull
    String getLocalizationKey();

    /**
     * Checks whether this task is critical.
     *
     * If a critical task fails, the application startup is considered to have failed and the
     * user is notified of this problem.
     *
     * Non critical task failures are, however, just logged as a fatal event and ignored.
     */
    default boolean isCritical() {
        return false;
    }

    /**
     * Executes the task.
     *
     * @throws Exception when executing the task fails.
     */
    void run() throws Exception;
}
