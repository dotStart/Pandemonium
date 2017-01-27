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
package tv.dotstart.pandemonium.ui.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.effect.EffectFactory;
import tv.dotstart.pandemonium.game.Game;
import tv.dotstart.pandemonium.game.GameStateMapper;
import tv.dotstart.pandemonium.game.Metadata;
import tv.dotstart.pandemonium.process.Process;
import tv.dotstart.pandemonium.process.ProcessMemoryPointer;
import tv.dotstart.pandemonium.ui.test.effect.BulletEffectFactory;
import tv.dotstart.pandemonium.ui.test.effect.DifficultyEffectFactory;
import tv.dotstart.pandemonium.ui.test.effect.FieldOfViewEffectFactory;
import tv.dotstart.pandemonium.ui.test.effect.MouseSensitivityEffectFactory;
import tv.dotstart.pandemonium.ui.test.effect.ObjectiveLocatorEffectFactory;
import tv.dotstart.pandemonium.ui.test.effect.PraxisEffectFactory;
import tv.dotstart.pandemonium.ui.test.effect.ReticleEffectFactory;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@Component
public class DXHRGame implements Game {
    private static final Logger logger = LogManager.getFormatterLogger(DXHRGame.class);

    private static final UUID id = new UUID(0x726FFC341294293L, 0xAC07F5C72C6DF878L);
    private static final Metadata metadata = new Metadata("0.1.0", 0, "https://github.com/dotStart/Pandemonium", "https://github.com/dotstart/Pandemonium/issues/new", ".start");

    private static final long LOADING_PTR = 0x1876708;
    private static final long PAUSED_PTR = 0x18726B0;
    private static final long STREAM_GROUP_PTR = 0x1857924;
    private static final int STREAM_GROUP_LENGTH = 55;
    private static final String STREAM_GROUP_PROLOGUE = "det_sarifhq_rail_tutorial";

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Set<EffectFactory> getEffectFactories() {
        return new HashSet<>(Arrays.asList(
                new BulletEffectFactory.Empty(),
                new BulletEffectFactory.Full(),

                new DifficultyEffectFactory.Easy(),
                new DifficultyEffectFactory.Hard(),
                new DifficultyEffectFactory.Medium(),

                new FieldOfViewEffectFactory.High(),
                new FieldOfViewEffectFactory.Low(),

                new MouseSensitivityEffectFactory.X.High(),
                new MouseSensitivityEffectFactory.X.Invert(),
                new MouseSensitivityEffectFactory.X.Low(),

                new MouseSensitivityEffectFactory.Y.High(),
                new MouseSensitivityEffectFactory.Y.Invert(),
                new MouseSensitivityEffectFactory.Y.Low(),

                new ObjectiveLocatorEffectFactory(),

                new PraxisEffectFactory.High(),
                new PraxisEffectFactory.Low(),

                new ReticleEffectFactory()
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Set<String> getExecutableNames() {
        return Collections.singleton("dxhr.exe");
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<URL> getIcon() {
        return Optional.of(DXHRGame.class.getResource("/icon/game/dxhr.png"));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public GameStateMapper createStateMapper(@Nonnull Process process) {
        return new GameStateMapper() {
            private final ProcessMemoryPointer loadingPointer = process.pointer("dxhr.exe", LOADING_PTR);
            private final ProcessMemoryPointer pausedPointer = process.pointer("dxhr.exe", PAUSED_PTR);
            private final ProcessMemoryPointer streamGroupPointer = process.pointer("dxhr.exe", STREAM_GROUP_PTR);

            private String streamGroupId = null;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isPaused() {
                return this.loadingPointer.readByte() == 1 || this.pausedPointer.readByte() == 1;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasReset() {
                String streamGroupId = this.streamGroupPointer.readString(STREAM_GROUP_LENGTH);

                if (!streamGroupId.isEmpty() && (this.streamGroupId == null || !this.streamGroupId.equalsIgnoreCase(streamGroupId))) {
                    if (this.streamGroupId != null) {
                        logger.info("Leaving map %s", this.streamGroupId);
                    }

                    logger.info("Entering map %s", streamGroupId);
                }

                boolean reset = this.streamGroupId != null && !this.streamGroupId.equalsIgnoreCase(streamGroupId) && streamGroupId.equalsIgnoreCase(STREAM_GROUP_PROLOGUE);
                this.streamGroupId = streamGroupId;

                return reset;
            }
        };
    }
}
