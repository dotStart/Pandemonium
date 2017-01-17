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
package tv.dotstart.pandemonium.fx;

import org.controlsfx.dialog.ExceptionDialog;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import javafx.application.Platform;

/**
 * Provides an exception handler which turns all of its received exceptions into user friendly
 * dialogs before exiting the application.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class FXExceptionHandler implements Thread.UncaughtExceptionHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void uncaughtException(@Nonnull Thread thread, @Nonnull Throwable throwable) {
        ExceptionDialog dialog = new ExceptionDialog(throwable);
        dialog.setTitle("Application Error");
        dialog.setHeaderText("An unexpected error has occurred.");
        dialog.setContentText("The application has encountered an unexpected state and is unable to recover. Please report this issue to the Pandemonium developers as a bug.");

        if (Platform.isFxApplicationThread()) {
            dialog.showAndWait();
            Platform.exit();
        } else {
            Platform.runLater(() -> {
                dialog.showAndWait();
                Platform.exit();
            });
        }
    }
}
