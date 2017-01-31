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

/**
 * Provides a mapper which provides insight on whether a game is currently paused or has been reset
 * since its state was last polled.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface GameStateMapper {

    /**
     * Checks whether the player has already entered the game or whether they are currently idling
     * on a title screen or a main menu.
     *
     * This method is used in order to decide when to first initialize the random number generator
     * and for most games will probably be tied to the values provided by {@link #hasReset()} or its
     * backing addresses.
     *
     * Generally it is recommended to somehow implement this method as it ensures fair conditions
     * for all race participants.
     */
    default boolean inGame() {
        return true;
    }

    /**
     * Checks whether the game is currently paused in some way shape or form.
     *
     * If this method returns true, all processing of effects will be paused until its return value
     * returns to false to prevent loading screens and other types of pausing systems from
     * interfering with the fair and equal conditions within a race.
     *
     * It is generally recommended to at least check whether the game is currently stuck on a
     * loading screen to not provide an unfair advantage to players with slower hard drives when
     * racing games which make use of in-game time (e.g. load removers).
     */
    default boolean isPaused() {
        return false;
    }

    /**
     * Checks whether the game has reverted its state to the initial map since the last call to this
     * method was performed.
     *
     * When "true" is returned from this method, the internal random number generator is reset to
     * the respective initial seed.
     */
    default boolean hasReset() {
        return false;
    }
}
