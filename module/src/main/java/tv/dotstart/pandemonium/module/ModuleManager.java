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
package tv.dotstart.pandemonium.module;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tv.dotstart.pandemonium.game.Game;
import tv.dotstart.pandemonium.game.GameConfiguration;

/**
 * Provides a central manager for handling game definitions provided through module jars and any of
 * the module jars within the classpath.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class ModuleManager {
    private static final Logger logger = LogManager.getFormatterLogger(ModuleManager.class);

    private final ObservableList<GameConfiguration> games = FXCollections.observableList(new CopyOnWriteArrayList<>());
    private final List<ClassLoader> moduleLoaders = new ArrayList<>();
    private final ReentrantLock gamesLock = new ReentrantLock();

    private final ApplicationContext context;

    @Autowired
    public ModuleManager(@Nonnull ApplicationContext context) {
        this.context = context;
    }

    // <editor-fold desc="Event Handlers">

    /**
     * Configures the current application context to make use of a special class loader
     * implementation.
     */
    @PostConstruct
    private void configureContext() {
        AnnotationConfigApplicationContext applicationContext = (AnnotationConfigApplicationContext) this.context;
        applicationContext.setClassLoader(new DelegatingClassLoader(this.getClass().getClassLoader()));

        this.loadModules();
    }

    /**
     * Listens for context refreshes in order to update the list of games recognized by the
     * application.
     */
    @EventListener
    private void onContextRefresh(@Nonnull ContextRefreshedEvent event) {
        this.gamesLock.lock();

        try {
            Collection<Game> games = event.getApplicationContext().getBeansOfType(Game.class, false, true).values();

            // remove all games which are no longer mentioned in the bean listing since they are no
            // longer valid
            this.games.removeIf(c -> !games.contains(c.getGame()));

            // add all new instances to the list since they were most likely introduced through freshly
            // loaded addons
            games.forEach((g) -> {
                if (this.games.stream().anyMatch((c) -> c.getGame().equals(g))) {
                    return;
                }

                // TODO: Load previous configuration
                this.games.add(new GameConfiguration(g));
            });
        } finally {
            this.gamesLock.unlock();
        }
    }
    // </editor-fold>

    // <editor-fold desc="Getters & Setters">

    /**
     * Retrieves a list of recognized games within the application.
     */
    @Nonnull
    public ObservableList<GameConfiguration> getGames() {
        return FXCollections.unmodifiableObservableList(this.games);
    }

    /**
     * Retrieves the directory which stores all active modules.
     */
    @Nonnull
    private Path getModuleDirectory() {
        return Paths.get("addons");
    }

    // </editor-fold>

    /**
     * Loads a specific module file.
     */
    private void loadModule(@Nonnull Path path) {
        try {
            this.moduleLoaders.add(new URLClassLoader(new URL[]{path.toUri().toURL()}, this.getClass().getClassLoader()));
        } catch (MalformedURLException ex) {
            logger.error("Failed to access module file at path \"" + path.toAbsolutePath() + "\": " + ex.getMessage(), ex);
            logger.error("No further attempts of loading this module will be made");
        }
    }

    /**
     * Attempts to load all module files within the module directory.
     */
    private void loadModules() {
        Path moduleDirectory = this.getModuleDirectory();

        if (Files.notExists(moduleDirectory)) {
            logger.info("Module directory \"%s\" does not exist - Creating a new directory to house modules", moduleDirectory);

            try {
                Files.createDirectories(moduleDirectory);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to create module directory \"" + moduleDirectory + "\": " + ex.getMessage(), ex);
            }

            return;
        }

        try (Stream<Path> stream = Files.list(moduleDirectory)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .sorted()
                    .forEachOrdered(this::loadModule);
        } catch (IOException ex) {
            logger.warn("Cannot access module directory " + moduleDirectory.toAbsolutePath() + ": " + ex.getMessage(), ex);
            logger.warn("Modules will be unavailable - No further attempts will be made");
        }
    }

    /**
     * Provides a delegating class loader implementation.
     *
     * This implementation forwards all loading requests for classes to any of the module loaders
     * present within the module manager at this point in time.
     */
    private class DelegatingClassLoader extends ClassLoader {

        DelegatingClassLoader(@Nonnull ClassLoader classLoader) {
            super(classLoader);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<?> loadClass(String s) throws ClassNotFoundException {
            try {
                return super.loadClass(s);
            } catch (ClassNotFoundException ex) {
                for (ClassLoader loader : ModuleManager.this.moduleLoaders) {
                    try {
                        return loader.loadClass(s);
                    } catch (ClassNotFoundException ignore) {
                    }
                }

                throw ex;
            }
        }
    }
}
