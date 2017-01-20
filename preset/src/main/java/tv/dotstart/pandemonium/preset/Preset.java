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
package tv.dotstart.pandemonium.preset;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Provides a representation for a serialized configuration preset which may be shared between
 * players in order to restore the exact same settings.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Immutable
@ThreadSafe
public final class Preset {
    private static final int VERSION_ID = 1;

    private final int version;
    private final UUID gameId;
    private final int revision;
    private final String seed;
    private final double effectChance;
    private final double combinationChance;
    private final double delayLow;
    private final double delayHigh;
    private final double durationLow;
    private final double durationHigh;
    private final Set<Integer> effectIds;

    Preset(@Nonnegative int version, @Nonnull UUID gameId, @Nonnegative int revision, @Nonnull String seed, @Nonnegative double effectChance, @Nonnegative double combinationChance, @Nonnegative double delayLow, @Nonnegative double delayHigh, @Nonnegative double durationLow, @Nonnegative double durationHigh, @Nonnull Set<Integer> effectIds) {
        this.version = version;
        this.gameId = gameId;
        this.revision = revision;
        this.seed = seed;
        this.effectChance = effectChance;
        this.combinationChance = combinationChance;
        this.delayLow = delayLow;
        this.delayHigh = delayHigh;
        this.durationLow = durationLow;
        this.durationHigh = durationHigh;
        this.effectIds = Collections.unmodifiableSet(effectIds);
    }

    /**
     * Creates an empty preset builder.
     */
    @Nonnull
    public static Builder builder(@Nonnull UUID gameId, @Nonnegative int revision) {
        return new Builder(gameId, revision);
    }

    /**
     * Creates a builder which inherits the configuration of another preset.
     */
    @Nonnull
    public static Builder copyOf(@Nonnull Preset preset) {
        return new Builder(preset.gameId, preset.revision, preset.effectIds);
    }

    // <editor-fold desc="Save & Load">

    /**
     * Reads a preset from a Base64 encoded string.
     *
     * @throws IllegalArgumentException  when the supplied buffer is of an invalid version.
     * @throws IllegalStateException     when one or more values are outside of their maximum
     *                                   bounds.
     * @throws IndexOutOfBoundsException when the buffer is shorter than expected.
     */
    @Nonnull
    public static Preset load(@Nonnull String encoded) {
        return load(PresetBuffer.of(encoded));
    }

    /**
     * Reads a preset from the supplied byte array.
     *
     * @throws IllegalArgumentException  when the supplied buffer is of an invalid version.
     * @throws IllegalStateException     when one or more values are outside of their maximum
     *                                   bounds.
     * @throws IndexOutOfBoundsException when the buffer is shorter than expected.
     */
    @Nonnull
    public static Preset load(@Nonnull byte[] array) {
        return load(PresetBuffer.wrap(array));
    }

    /**
     * Reads a preset from the supplied buffer.
     *
     * @throws IllegalArgumentException  when the supplied buffer is of an invalid version.
     * @throws IllegalStateException     when one or more values are outside of their maximum
     *                                   bounds.
     * @throws IndexOutOfBoundsException when the buffer is shorter than expected.
     */
    @Nonnull
    public static Preset load(@Nonnull ByteBuf buffer) {
        return load(PresetBuffer.wrap(buffer));
    }

    /**
     * Reads a preset from the supplied preset buffer.
     *
     * @throws IllegalArgumentException  when the supplied buffer is of an invalid version.
     * @throws IllegalStateException     when one or more values are outside of their maximum
     *                                   bounds.
     * @throws IndexOutOfBoundsException when the buffer is shorter than expected.
     */
    @Nonnull
    public static Preset load(@Nonnull PresetBuffer buffer) throws IllegalArgumentException {
        int version = (int) buffer.readUnsignedInteger();

        if (version != VERSION_ID) {
            throw new IllegalArgumentException("Unsupported preset version: Expected " + VERSION_ID + " but got " + version);
        }

        UUID gameId = new UUID(buffer.readLong(), buffer.readLong());
        int revision = (int) buffer.readUnsignedInteger();
        String seed = buffer.readString();
        double effectChance = buffer.readDouble();
        double combinationChance = buffer.readDouble();
        double delayLow = buffer.readDouble();
        double delayHigh = buffer.readDouble();
        double durationLow = buffer.readDouble();
        double durationHigh = buffer.readDouble();

        Set<Integer> effectIds = buffer.readUnsignedIntegerSet().stream()
                .map(Long::intValue)
                .collect(Collectors.toSet());

        return new Preset(version, gameId, revision, seed, effectChance, combinationChance, delayLow, delayHigh, durationLow, durationHigh, effectIds);
    }

    /**
     * Saves a preset into the supplied buffer.
     */
    public void save(@Nonnull ByteBuf buffer) {
        this.save(PresetBuffer.wrap(buffer));
    }

    /**
     * Saves a preset into the supplied preset buffer.
     */
    public void save(@Nonnull PresetBuffer buffer) {
        buffer
                .writeUnsignedInteger(VERSION_ID)
                .writeLong(this.gameId.getMostSignificantBits())
                .writeLong(this.gameId.getLeastSignificantBits())
                .writeUnsignedInteger(this.revision)
                .writeString(this.seed)
                .writeDouble(this.effectChance)
                .writeDouble(this.combinationChance)
                .writeDouble(this.delayLow)
                .writeDouble(this.delayHigh)
                .writeDouble(this.durationLow)
                .writeDouble(this.durationHigh)
                .writeUnsignedIntegerCollection(
                        this.effectIds.stream()
                                .map(Integer::longValue)
                                .collect(Collectors.toSet())
                );
    }

    /**
     * Saves a preset into a byte array.
     */
    @Nonnull
    public byte[] toByteArray() {
        PresetBuffer buffer = PresetBuffer.wrap(Unpooled.directBuffer());

        try {
            this.save(buffer);
            return buffer.toByteArray();
        } finally {
            buffer.getBuffer().release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String toString() {
        PresetBuffer buffer = PresetBuffer.wrap(Unpooled.directBuffer());

        try {
            this.save(buffer);
            return buffer.toString();
        } finally {
            buffer.getBuffer().release();
        }
    }
    // </editor-fold>

    // <editor-fold desc="Getters & Setters">
    @Nonnegative
    public int getVersion() {
        return this.version;
    }

    @Nonnull
    public UUID getGameId() {
        return this.gameId;
    }

    @Nonnegative
    public int getRevision() {
        return this.revision;
    }

    @Nonnull
    public String getSeed() {
        return this.seed;
    }

    @Nonnegative
    public double getEffectChance() {
        return this.effectChance;
    }

    @Nonnegative
    public double getCombinationChance() {
        return this.combinationChance;
    }

    @Nonnegative
    public double getDelayLow() {
        return this.delayLow;
    }

    @Nonnegative
    public double getDelayHigh() {
        return this.delayHigh;
    }

    @Nonnegative
    public double getDurationLow() {
        return this.durationLow;
    }

    @Nonnegative
    public double getDurationHigh() {
        return this.durationHigh;
    }

    @Nonnull
    public Set<Integer> getEffectIds() {
        return this.effectIds;
    }
    // </editor-fold>

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Preset preset = (Preset) o;
        return this.version == preset.version &&
                this.revision == preset.revision &&
                Double.compare(preset.effectChance, this.effectChance) == 0 &&
                Double.compare(preset.combinationChance, this.combinationChance) == 0 &&
                Double.compare(preset.delayLow, this.delayLow) == 0 &&
                Double.compare(preset.delayHigh, this.delayHigh) == 0 &&
                Double.compare(preset.durationLow, this.durationLow) == 0 &&
                Double.compare(preset.durationHigh, this.durationHigh) == 0 &&
                Objects.equals(this.gameId, preset.gameId) &&
                Objects.equals(this.seed, preset.seed) &&
                Objects.equals(this.effectIds, preset.effectIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.version, this.gameId, this.revision, this.seed, this.effectChance, this.combinationChance, this.delayLow, this.delayHigh, this.durationLow, this.durationHigh, this.effectIds);
    }

    /**
     * Provides a factory for preset instances.
     */
    public static final class Builder {
        private final UUID gameId;
        private final int revision;
        private final ObservableList<Integer> effectIds = FXCollections.observableArrayList();

        private String seed = "";
        private double effectChance = 50;
        private double combinationChance = 0;
        private double delayLow = 0;
        private double delayHigh = 300;
        private double durationLow = 0;
        private double durationHigh = 300;

        Builder(@Nonnull UUID gameId, @Nonnegative int revision) {
            this.gameId = gameId;
            this.revision = revision;
        }

        Builder(@Nonnull UUID gameId, @Nonnegative int revision, @Nonnull Set<Integer> effectIds) {
            this(gameId, revision);
            this.effectIds.addAll(effectIds);
        }

        /**
         * Builds a preset using this builder's configuration.
         */
        @Nonnull
        public Preset build() {
            return new Preset(VERSION_ID, this.gameId, this.revision, this.seed, this.effectChance, this.combinationChance, this.delayLow, this.delayHigh, this.durationLow, this.durationHigh, new HashSet<>(this.effectIds));
        }

        /**
         * Adds an effect to the preset.
         */
        @Nonnull
        public Builder addEffect(@Nonnegative int effectId) {
            this.effectIds.add(effectId);
            return this;
        }

        /**
         * Removes an effect from the preset.
         */
        @Nonnull
        public Builder removeEffect(@Nonnegative int effectId) {
            this.effectIds.remove((Integer) effectId);
            return this;
        }

        // <editor-fold desc="Getters & Setters">
        @Nonnull
        public UUID getGameId() {
            return this.gameId;
        }

        @Nonnegative
        public int getRevision() {
            return this.revision;
        }

        @Nonnull
        public ObservableList<Integer> getEffectIds() {
            return this.effectIds;
        }

        @Nonnull
        public String getSeed() {
            return this.seed;
        }

        @Nonnull
        public Builder setSeed(@Nonnull String seed) {
            this.seed = seed;
            return this;
        }

        @Nonnegative
        public double getEffectChance() {
            return this.effectChance;
        }

        @Nonnull
        public Builder setEffectChance(@Nonnegative double effectChance) {
            this.effectChance = effectChance;
            return this;
        }

        @Nonnegative
        public double getCombinationChance() {
            return this.combinationChance;
        }

        @Nonnull
        public Builder setCombinationChance(@Nonnegative double combinationChance) {
            this.combinationChance = combinationChance;
            return this;
        }

        @Nonnegative
        public double getDelayLow() {
            return this.delayLow;
        }

        @Nonnull
        public Builder setDelayLow(@Nonnegative double delayLow) {
            this.delayLow = delayLow;
            return this;
        }

        @Nonnegative
        public double getDelayHigh() {
            return this.delayHigh;
        }

        @Nonnull
        public Builder setDelayHigh(@Nonnegative double delayHigh) {
            this.delayHigh = delayHigh;
            return this;
        }

        @Nonnegative
        public double getDurationLow() {
            return this.durationLow;
        }

        @Nonnull
        public Builder setDurationLow(@Nonnegative double durationLow) {
            this.durationLow = durationLow;
            return this;
        }

        @Nonnegative
        public double getDurationHigh() {
            return this.durationHigh;
        }

        @Nonnull
        public Builder setDurationHigh(@Nonnegative double durationHigh) {
            this.durationHigh = durationHigh;
            return this;
        }
        // </editor-fold>
    }
}
