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
package tv.dotstart.pandemonium.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.tools.Platform;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Provides a globally available configuration object which represents a parsed configuration file
 * and flushes it back to disk as it is changed by the user.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class ApplicationConfiguration {
    private static final Logger logger = LogManager.getFormatterLogger(ApplicationConfiguration.class);
    private static final Set<Locale> SUPPORTED_LOCALES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            // Fallback Language
            Locale.ENGLISH,

            // Additional Languages
            Locale.GERMAN
    )));

    private final ObjectProperty<Locale> applicationLocale = new SimpleObjectProperty<>(Locale.ENGLISH);
    private final BooleanProperty applicationCheckUpdates = new SimpleBooleanProperty(true);
    private final BooleanProperty applicationCheckAddonUpdates = new SimpleBooleanProperty(true);
    private final BooleanProperty applicationCheckEndorsementUpdates = new SimpleBooleanProperty(true);

    private final BooleanProperty audioPlaySchedule = new SimpleBooleanProperty(false);
    private final BooleanProperty audioPlayApply = new SimpleBooleanProperty(false);
    private final BooleanProperty audioPlayRevert = new SimpleBooleanProperty(false);
    private final StringProperty audioClipSchedule = new SimpleStringProperty();
    private final StringProperty audioClipApply = new SimpleStringProperty();
    private final StringProperty audioClipRevert = new SimpleStringProperty();

    private final BooleanProperty webEnabled = new SimpleBooleanProperty(false);
    private final StringProperty webAddress = new SimpleStringProperty("127.0.0.1");
    private final IntegerProperty webPort = new SimpleIntegerProperty(8080);

    private final StringProperty preset = new SimpleStringProperty();

    private final ObjectReader reader;
    private final ObjectWriter writer;

    public ApplicationConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.reader = mapper.readerForUpdating(this);
        this.writer = mapper.writerFor(ApplicationConfiguration.class);

        // attempt to use the system locale as a base locale
        Locale locale = Locale.getDefault();

        if (SUPPORTED_LOCALES.contains(locale)) {
            this.applicationLocale.set(locale);
        }

        // hook a change listener for validation purposes on all relevant properties
        this.applicationLocale.addListener((observable, oldValue, newValue) -> {
            // check whether we support the target locale and revert to English if we do no longer
            // provide the localization files required for the specified language
            if (newValue == null || !SUPPORTED_LOCALES.contains(newValue)) {
                this.setApplicationLocale(Locale.ENGLISH);
            }
        });

        // hook invalidation on all properties in order to update the local configuration file when
        // necessary
        this.applicationLocale.addListener(this::onPropertyInvalidation);
        this.applicationCheckAddonUpdates.addListener(this::onPropertyInvalidation);
        this.applicationCheckAddonUpdates.addListener(this::onPropertyInvalidation);
        this.applicationCheckEndorsementUpdates.addListener(this::onPropertyInvalidation);

        this.audioPlaySchedule.addListener(this::onPropertyInvalidation);
        this.audioPlayApply.addListener(this::onPropertyInvalidation);
        this.audioPlayRevert.addListener(this::onPropertyInvalidation);
        this.audioClipSchedule.addListener(this::onPropertyInvalidation);
        this.audioClipApply.addListener(this::onPropertyInvalidation);
        this.audioClipRevert.addListener(this::onPropertyInvalidation);

        this.webEnabled.addListener(this::onPropertyInvalidation);
        this.webAddress.addListener(this::onPropertyInvalidation);
        this.webPort.addListener(this::onPropertyInvalidation);

        this.preset.addListener(this::onPropertyInvalidation);
    }

    /**
     * Loads the previously stored configuration.
     */
    @PostConstruct
    private void loadPrevious() {
        Path configPath = getConfigurationPath();

        if (Files.notExists(configPath)) {
            logger.warn("No application configuration located");
            logger.warn("Assuming default values for all properties");
            return;
        }

        try {
            this.reader.readValue(configPath.toUri().toURL());
        } catch (IOException ex) {
            logger.error("Failed to read application configuration: " + ex.getMessage(), ex);
            logger.error("Assuming default values for all properties");
        }
    }

    /**
     * Handles invalidation of any property.
     */
    private void onPropertyInvalidation(@Nonnull Observable observable) {
        Path configDirectory = getStoragePath();
        Path configPath = getConfigurationPath();

        if (Files.notExists(configDirectory)) {
            logger.info("No storage directory available");
            logger.info("Creating an empty directory to house the configuration");

            try {
                Files.createDirectories(configDirectory);
            } catch (IOException ex) {
                logger.error("Failed to create configuration directory: " + ex.getMessage(), ex);
                return;
            }
        }

        try {
            this.writer.writeValue(configPath.toFile(), this);
        } catch (IOException ex) {
            logger.error("Failed to write application configuration: " + ex.getMessage(), ex);
            logger.error("Application WILL assume default property values at next startup");
        }
    }

    /**
     * Retrieves the path the application configuration is to be stored in.
     */
    @Nonnull
    public static Path getConfigurationPath() {
        return getStoragePath().resolve("config.json");
    }

    /**
     * Retrieves the path all configuration data is to be stored in.
     */
    @Nonnull
    public static Path getStoragePath() {
        switch (Platform.getCurrent()) {
            case WINDOWS:
                String baseDirectory = System.getenv("APPDATA");

                // if, for whatever reason, APPDATA is not populated, we'll just use the user
                // directory instead
                if (baseDirectory == null) {
                    baseDirectory = System.getProperty("user.home");
                }

                return Paths.get(baseDirectory, "Pandemonium");
        }

        return Paths.get(System.getProperty("user.home"), ".Pandemonium");
    }

    /**
     * Retrieves a set of supported locales.
     */
    @Nonnull
    public static Set<Locale> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    // <editor-fold desc="Getters & Setters">
    public Locale getApplicationLocale() {
        return this.applicationLocale.get();
    }

    @Nonnull
    public ObjectProperty<Locale> applicationLocaleProperty() {
        return this.applicationLocale;
    }

    @JsonProperty
    public void setApplicationLocale(Locale applicationLocale) {
        this.applicationLocale.set(applicationLocale);
    }

    public boolean isApplicationCheckUpdates() {
        return this.applicationCheckUpdates.get();
    }

    @Nonnull
    public BooleanProperty applicationCheckUpdatesProperty() {
        return this.applicationCheckUpdates;
    }

    @JsonProperty
    public void setApplicationCheckUpdates(boolean applicationCheckUpdates) {
        this.applicationCheckUpdates.set(applicationCheckUpdates);
    }

    public boolean isApplicationCheckAddonUpdates() {
        return this.applicationCheckAddonUpdates.get();
    }

    @Nonnull
    public BooleanProperty applicationCheckAddonUpdatesProperty() {
        return this.applicationCheckAddonUpdates;
    }

    @JsonProperty
    public void setApplicationCheckAddonUpdates(boolean applicationCheckAddonUpdates) {
        this.applicationCheckAddonUpdates.set(applicationCheckAddonUpdates);
    }

    public boolean isApplicationCheckEndorsementUpdates() {
        return this.applicationCheckEndorsementUpdates.get();
    }

    @Nonnull
    public BooleanProperty applicationCheckEndorsementUpdatesProperty() {
        return this.applicationCheckEndorsementUpdates;
    }

    @JsonProperty
    public void setApplicationCheckEndorsementUpdates(boolean applicationCheckEndorsementUpdates) {
        this.applicationCheckEndorsementUpdates.set(applicationCheckEndorsementUpdates);
    }

    public boolean isAudioPlaySchedule() {
        return this.audioPlaySchedule.get();
    }

    @Nonnull
    public BooleanProperty audioPlayScheduleProperty() {
        return this.audioPlaySchedule;
    }

    @JsonProperty
    public void setAudioPlaySchedule(boolean audioPlaySchedule) {
        this.audioPlaySchedule.set(audioPlaySchedule);
    }

    public boolean isAudioPlayApply() {
        return this.audioPlayApply.get();
    }

    @Nonnull
    public BooleanProperty audioPlayApplyProperty() {
        return this.audioPlayApply;
    }

    @JsonProperty
    public void setAudioPlayApply(boolean audioPlayApply) {
        this.audioPlayApply.set(audioPlayApply);
    }

    public boolean isAudioPlayRevert() {
        return this.audioPlayRevert.get();
    }

    @Nonnull
    public BooleanProperty audioPlayRevertProperty() {
        return this.audioPlayRevert;
    }

    @JsonProperty
    public void setAudioPlayRevert(boolean audioPlayRevert) {
        this.audioPlayRevert.set(audioPlayRevert);
    }

    public String getAudioClipSchedule() {
        return this.audioClipSchedule.get();
    }

    @Nonnull
    public StringProperty audioClipScheduleProperty() {
        return this.audioClipSchedule;
    }

    @JsonProperty
    public void setAudioClipSchedule(String audioClipSchedule) {
        this.audioClipSchedule.set(audioClipSchedule);
    }

    public String getAudioClipApply() {
        return this.audioClipApply.get();
    }

    @Nonnull
    public StringProperty audioClipApplyProperty() {
        return this.audioClipApply;
    }

    @JsonProperty
    public void setAudioClipApply(String audioClipApply) {
        this.audioClipApply.set(audioClipApply);
    }

    public String getAudioClipRevert() {
        return this.audioClipRevert.get();
    }

    @Nonnull
    public StringProperty audioClipRevertProperty() {
        return this.audioClipRevert;
    }

    @JsonProperty
    public void setAudioClipRevert(String audioClipRevert) {
        this.audioClipRevert.set(audioClipRevert);
    }

    public boolean isWebEnabled() {
        return this.webEnabled.get();
    }

    @Nonnull
    public BooleanProperty webEnabledProperty() {
        return this.webEnabled;
    }

    @JsonProperty
    public void setWebEnabled(boolean webEnabled) {
        this.webEnabled.set(webEnabled);
    }

    public String getWebAddress() {
        return this.webAddress.get();
    }

    @Nonnull
    public StringProperty webAddressProperty() {
        return this.webAddress;
    }

    @JsonProperty
    public void setWebAddress(String webAddress) {
        this.webAddress.set(webAddress);
    }

    public int getWebPort() {
        return this.webPort.get();
    }

    @Nonnull
    public IntegerProperty webPortProperty() {
        return this.webPort;
    }

    @JsonProperty
    public void setWebPort(int webPort) {
        this.webPort.set(webPort);
    }

    public String getPreset() {
        return this.preset.get();
    }

    @Nonnull
    public StringProperty presetProperty() {
        return this.preset;
    }

    @JsonProperty
    public void setPreset(String preset) {
        this.preset.set(preset);
    }
    // </editor-fold>
}
