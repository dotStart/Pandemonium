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
import React from "react";

/**
 * Provides an effect representation.
 */
export default class Effect extends React.Component {
    constructor(props) {
        super(props);
    }

    /**
     * Builds a style class name for this effect.
     * @returns {string}
     */
    getClassName() {
        return 'effect' + (!!this.props.effect.state ? ' ' + this.props.effect.state.toLowerCase() : '');
    }

    /**
     * Renders the effect in HTML.
     * @returns {XML}
     */
    render() {
        return <div className={this.getClassName()}>
            <div className="progress">
                <div className="bar" style={{width: this.props.effect.progress * 100 + '%'}}></div>
            </div>
            <div className="inner">
                <div className="detail">
                    <span className="title">{this.props.effect.title}</span>
                    <span className="description">{this.props.effect.description}</span>
                </div>
            </div>
        </div>
    }
}
