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
package tv.dotstart.pandemonium.process.exception.memory;

/**
 * Provides an exception for cases where a process's memory is unavailable due to its or its
 * backing process's state.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class ProcessMemoryStateException extends ProcessMemoryException {

    public ProcessMemoryStateException() {
        super();
    }

    public ProcessMemoryStateException(String s) {
        super(s);
    }

    public ProcessMemoryStateException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ProcessMemoryStateException(Throwable throwable) {
        super(throwable);
    }
}
