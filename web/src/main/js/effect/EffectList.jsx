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
import _ from "lodash";
import Effect from "./Effect";
import React from "react";
import SockJS from "sockjs";
import Stomp from "stomp";

/**
 * Provides a list of effects.
 */
export default class EffectList extends React.Component {
    constructor() {
        super();

        this.state = {
            effects: {}
        };

        this.connect();
    }

    /**
     * Connects to the server.
     */
    connect() {
        this.socket = new SockJS('/websocket');
        this.stomp = Stomp.over(this.socket);

        this.stomp.connect({}, () => this.onConnect(), () => this.onFailure);
    }

    /**
     * Handles connections to the server side socket.
     */
    onConnect() {
        this.stomp.subscribe('/topic/effect/schedule', (effect) => this.onSchedule(effect));
        this.stomp.subscribe('/topic/effect/progress', (effect) => this.onProgress(effect));
        this.stomp.subscribe('/topic/effect/remove', (effect) => this.onRemove(effect));
    }

    onFailure() {
        console.log('Connection failed - Retrying in 5 seconds');
        window.setTimeout(() => this.connect(), 5000);
    }

    /**
     * Handles incoming effect schedules.
     */
    onSchedule(message) {
        const effect = JSON.parse(message.body);

        const update = {};
        update[effect.id] = effect;
        update[effect.id]['progress'] = 0.0;

        this.setState({effects: _.assign({}, this.state.effects, update)});
    }

    /**
     * Handles incoming effect progress updates.
     */
    onProgress(message) {
        const progress = JSON.parse(message.body);
        const effect = this.state.effects[progress.id];

        if (!effect) {
            console.log('Effect "' + progress.id + '" is not present')
            return
        }

        const update = {};
        update[progress.id] = effect;
        update[progress.id]['progress'] = progress.progress;
        update[progress.id]['state'] = progress.state;

        this.setState({effects: _.assign({}, this.state.effects, update)});
    }

    /**
     * Handles incoming effect removals.
     */
    onRemove(message) {
        const removal = JSON.parse(message.body);
        const effects = _.assign({}, this.state.effects);

        delete effects[removal.id];

        this.setState({effects: effects});
    }

    /**
     * Renders the contents of this effect list on the website.
     * @returns {XML}
     */
    render() {
        var i = 0;
        const effects = [];

        for (const key in this.state.effects) {
            if (++i > 4) {
                break;
            }

            effects.push(this.renderEffect(key));
        }

        return <section className="effect-list">
            {effects}
        </section>
    }

    /**
     * Renders a single effect.
     * @param e
     * @returns {XML}
     */
    renderEffect(e) {
        return <Effect key={e} effect={this.state.effects[e]}/>
    }
}
