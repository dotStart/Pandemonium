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
package tv.dotstart.pandemonium.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.tools.Platform;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;

import java.nio.file.Paths;

import javax.annotation.Nonnull;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tv.dotstart.pandemonium.configuration.ApplicationConfiguration;
import tv.dotstart.pandemonium.fx.FX;
import tv.dotstart.pandemonium.fx.FXExceptionHandler;
import tv.dotstart.pandemonium.ui.configuration.helper.DefaultApplicationConfiguration;
import tv.dotstart.pandemonium.ui.configuration.helper.WebEnabledApplicationConfiguration;
import tv.dotstart.pandemonium.ui.window.MainWindow;
import tv.dotstart.pandemonium.ui.window.SplashScreen;

/**
 * Provides a JavaFX entry-point to the application which initializes the backing framework
 * implementation as well as the module manager.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class Pandemonium extends Application {
    private static final Logger logger = LogManager.getFormatterLogger(Pandemonium.class);

    private final ConfigurableApplicationContext context;

    public Pandemonium() {
        // load the previous application configuration or generate a new configuration to decide
        // whether it is necessary to construct a web context or a regular context to skip the web
        // server initialization if necessary
        ApplicationConfiguration configuration = new ApplicationConfiguration();

        if (configuration.isWebEnabled()) {
            this.context = new AnnotationConfigEmbeddedWebApplicationContext(WebEnabledApplicationConfiguration.class);
        } else {
            this.context = new AnnotationConfigApplicationContext(DefaultApplicationConfiguration.class);
        }

        // register the application instance with the application context now since it is not
        // actually managed by spring
        this.context.getBeanFactory().registerSingleton("application", this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(@Nonnull Stage primaryStage) throws Exception {
        // register host services with the application context now since it may not be available
        // if we attempt to access it during construction
        this.context.getBeanFactory().registerSingleton("hostServices", this.getHostServices());

        // scan a single extra package provided through a system property in order to simplify addon
        // development in IDEs
        String extraPackage = System.getProperty("pandemonium.scan.package");

        if (extraPackage != null) {
            ((AnnotationConfigRegistry) this.context).scan(extraPackage);
        }

        // register a global exception handler with the current thread as well as all other threads
        // through the default exception handler to notify users of application bugs
        FXExceptionHandler exceptionHandler = this.context.getBean(FXExceptionHandler.class);
        Thread.currentThread().setUncaughtExceptionHandler(exceptionHandler);
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

        // bootstrap the main application scene into our primary stage
        Scene rootScene = this.context.getBean(FX.class).createScene(SplashScreen.class);

        if (javafx.application.Platform.isSupported(ConditionalFeature.TRANSPARENT_WINDOW)) {
            rootScene.setFill(Color.TRANSPARENT);
        }

        primaryStage.initStyle((javafx.application.Platform.isSupported(ConditionalFeature.TRANSPARENT_WINDOW) ? StageStyle.TRANSPARENT : StageStyle.UNDECORATED));
        primaryStage.getIcons().add(new Image(Pandemonium.class.getResource("/icon/application256.png").toExternalForm()));
        primaryStage.setTitle("Pandemonium");
        primaryStage.setScene(rootScene);
        primaryStage.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        logger.info("Bye! :)");

        this.context.close();
    }

    /**
     * Provides a Java entry-point which directly launches JavaFX as well as the backing framework
     * implementation.
     */
    public static void main(@Nonnull String[] arguments) {
        // print copyright information
        {
            Package p = Pandemonium.class.getPackage();

            logger.info("Pandemonium " + (p.getImplementationVersion() != null ? "v%s" : "(Development Snapshot)"), p.getImplementationVersion());
            logger.info("Licensed under the terms of the Apache License, Version 2.0 <https://apache.org/licenses/LICENSE-2.0.txt>");
            logger.info("");
        }

        // print environment information
        logger.info("Java Implementation: %s provided by %s <%s>", System.getProperty("java.version"), System.getProperty("java.vendor"), System.getProperty("java.vendor.url"));
        logger.info("Java Home: %s", System.getProperty("java.home"));
        logger.info("Java Classpath: %s", System.getProperty("java.class.path"));

        logger.info("Operating System: %s" + (Platform.getCurrent() != Platform.WINDOWS ? " v%s" : ""), System.getProperty("os.name"), System.getProperty("os.version"));
        logger.info("System Architecture: %s", System.getProperty("os.arch"));

        logger.info("Working Directory: %s", Paths.get("").toAbsolutePath());
        logger.info("User: %s (home is located at %s)", System.getProperty("user.name"), System.getProperty("user.home"));

        // launch JavaFX
        Application.launch(Pandemonium.class, arguments);
    }
}
