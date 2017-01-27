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

import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tv.dotstart.pandemonium.effect.EffectConfiguration;
import tv.dotstart.pandemonium.preset.Preset;

/**
 * Provides a wrapper for game instances in order to augment them with the properties necessary to
 * configure its respective values.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class GameConfiguration {
    private final ObjectProperty<Preset> preset = new SimpleObjectProperty<>();
    private final StringProperty seed = new SimpleStringProperty();
    private final DoubleProperty effectChance = new SimpleDoubleProperty(50);
    private final DoubleProperty combinationChance = new SimpleDoubleProperty();
    private final DoubleProperty delayLow = new SimpleDoubleProperty();
    private final DoubleProperty delayHigh = new SimpleDoubleProperty(300);
    private final DoubleProperty durationLow = new SimpleDoubleProperty();
    private final DoubleProperty durationHigh = new SimpleDoubleProperty(300);
    private final ObservableList<EffectConfiguration> effectConfigurations = FXCollections.observableArrayList();

    private final Game game;
    private final Preset.Builder presetBuilder;

    public GameConfiguration(@Nonnull Game game) {
        this.game = game;
        this.presetBuilder = Preset.builder(game.getId(), game.getMetadata().getRevision());

        this.effectConfigurations.addAll(
                game.getEffectFactories().stream()
                        .map(EffectConfiguration::new)
                        .collect(Collectors.toList())
        );

        this.effectConfigurations.forEach((c) -> this.presetBuilder.addEffect(c.getEffectFactory().getEffectId()));

        // hook event listeners and bindings in order to re-generate the binding when necessary
        this.preset.bind(Bindings.createObjectBinding(this::updatePreset, this.seed, this.effectChance, this.combinationChance, this.delayLow, this.delayHigh, this.durationLow, this.durationHigh, this.effectConfigurations, this.presetBuilder.getEffectIds()));

        this.effectConfigurations.forEach((e) -> e.activeProperty().addListener((ob, o, n) -> {
            if (n) {
                this.presetBuilder.addEffect(e.getEffectFactory().getEffectId());
            } else {
                this.presetBuilder.removeEffect(e.getEffectFactory().getEffectId());
            }
        }));
    }

    /**
     * Generates a new random seed.
     */
    public void generateSeed() {
        this.setSeed(new BigInteger(64, new Random()).toString(16));
    }


    // <editor-fold desc="Event Handlers & Bindings">

    /**
     * Updates the preset object for this game configuration.
     */
    @Nullable
    @SuppressWarnings("ConstantConditions")
    private Preset updatePreset() {
        // when no seed is presented to us at this point, we got a fresh object and need to generate
        // a new random seed to simplify things for users
        if (this.getSeed() == null) {
            this.generateSeed();
        }

        return this.presetBuilder
                .setSeed(this.getSeed())
                .setEffectChance(this.getEffectChance())
                .setCombinationChance(this.getCombinationChance())
                .setDelayLow(this.getDelayLow())
                .setDelayHigh(this.getDelayHigh())
                .setDurationLow(this.getDurationLow())
                .setDurationHigh(this.getDurationHigh())
                .build();
    }
    // </editor-fold>

    // <editor-fold desc="Getters & Setters">
    @Nonnull
    public Preset getPreset() {
        return this.preset.get();
    }

    @Nonnull
    public ObjectProperty<Preset> presetProperty() {
        return this.preset;
    }

    @Nullable
    public String getSeed() {
        return this.seed.get();
    }

    @Nonnull
    public StringProperty seedProperty() {
        return this.seed;
    }

    public void setSeed(@Nullable String seed) {
        this.seed.set(seed);
    }

    @Nonnegative
    public double getEffectChance() {
        return this.effectChance.get();
    }

    @Nonnull
    public DoubleProperty effectChanceProperty() {
        return this.effectChance;
    }

    public void setEffectChance(@Nonnegative double effectChance) {
        this.effectChance.set(effectChance);
    }

    @Nonnegative
    public double getCombinationChance() {
        return this.combinationChance.get();
    }

    @Nonnull
    public DoubleProperty combinationChanceProperty() {
        return this.combinationChance;
    }

    public void setCombinationChance(@Nonnegative double combinationChance) {
        this.combinationChance.set(combinationChance);
    }

    @Nonnegative
    public double getDelayLow() {
        return this.delayLow.get();
    }

    @Nonnull
    public DoubleProperty delayLowProperty() {
        return this.delayLow;
    }

    public void setDelayLow(@Nonnegative double delayLow) {
        this.delayLow.set(delayLow);
    }

    @Nonnegative
    public double getDelayHigh() {
        return this.delayHigh.get();
    }

    @Nonnull
    public DoubleProperty delayHighProperty() {
        return this.delayHigh;
    }

    public void setDelayHigh(@Nonnegative double delayHigh) {
        this.delayHigh.set(delayHigh);
    }

    @Nonnegative
    public double getDurationLow() {
        return this.durationLow.get();
    }

    @Nonnull
    public DoubleProperty durationLowProperty() {
        return this.durationLow;
    }

    public void setDurationLow(@Nonnegative double durationLow) {
        this.durationLow.set(durationLow);
    }

    @Nonnegative
    public double getDurationHigh() {
        return this.durationHigh.get();
    }

    @Nonnull
    public DoubleProperty durationHighProperty() {
        return this.durationHigh;
    }

    public void setDurationHigh(@Nonnegative double durationHigh) {
        this.durationHigh.set(durationHigh);
    }

    @Nonnull
    public ObservableList<EffectConfiguration> getEffectConfigurations() {
        return FXCollections.unmodifiableObservableList(this.effectConfigurations);
    }

    @Nonnull
    public Game getGame() {
        return this.game;
    }
    // </editor-fold>

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        GameConfiguration that = (GameConfiguration) o;
        return Objects.equals(this.seed, that.seed) &&
                Objects.equals(this.effectChance, that.effectChance) &&
                Objects.equals(this.combinationChance, that.combinationChance) &&
                Objects.equals(this.delayLow, that.delayLow) &&
                Objects.equals(this.delayHigh, that.delayHigh) &&
                Objects.equals(this.durationLow, that.durationLow) &&
                Objects.equals(this.durationHigh, that.durationHigh) &&
                Objects.equals(this.effectConfigurations, that.effectConfigurations) &&
                Objects.equals(this.game, that.game);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.seed, this.effectChance, this.combinationChance, this.delayLow, this.delayHigh, this.durationLow, this.durationHigh, this.effectConfigurations, this.game);
    }
}
