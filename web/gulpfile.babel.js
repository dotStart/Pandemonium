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
'use strict';
import gulp from "gulp";
import babel from "gulp-babel";
import less from "gulp-less";
import LessAutoprefix from "less-plugin-autoprefix";
import plumber from "gulp-plumber";
import sourcemaps from "gulp-sourcemaps";
import uglify from "gulp-uglify";

const SCRIPT_INPUT_DIRECTORY = 'src/main/js/';
const SCRIPT_OUTPUT_DIRECTORY = 'target/classes/static/script/';
const STYLE_INPUT_DIRECTORY = 'src/main/less/';
const STYLE_OUTPUT_DIRECTORY = 'target/classes/static/style/';

const autoprefix = new LessAutoprefix({
    browsers: ['last 2 versions']
});

gulp.task('build', ['script', 'style']);

/**
 * Transpiles all ES6 scripts into ES5 to achieve maximum source compatibility.
 */
gulp.task('script', () => {
    return gulp.src(
        [
            '**/*.js',
            '**/*.jsx'
        ],
        {
            cwd: SCRIPT_INPUT_DIRECTORY
        }
    )
        .pipe(plumber())
        .pipe(sourcemaps.init())
        .pipe(babel(
            {
                presets: ['es2015', 'react'],
                plugins: ['transform-es2015-modules-amd'],
            }
        ))
        .pipe(uglify())
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest(SCRIPT_OUTPUT_DIRECTORY));
});

/**
 * Transpiles all less stylesheets and adds prefixes/compatibility hacks for the last two browser
 * releases.
 */
gulp.task('style', () => {
    return gulp.src(`${STYLE_INPUT_DIRECTORY}/*.less`)
        .pipe(plumber())
        .pipe(sourcemaps.init())
        .pipe(less(
            {
                plugins: autoprefix
            }
        ))
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest(STYLE_OUTPUT_DIRECTORY));
});

/**
 * Watches for changes in the input files and recompiles the sources when needed.
 */
gulp.task('watch', ['build'], () => {
    gulp.watch(
        [
            `${SCRIPT_INPUT_DIRECTORY}/**/*.js`,
            `${SCRIPT_INPUT_DIRECTORY}/**/*.jsx`
        ],
        ['script']
    );
    gulp.watch(`${STYLE_INPUT_DIRECTORY}/*.less`, ['style']);
});
