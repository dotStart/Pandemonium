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
 * Provides a base exception for error case where accessing a process's memory fails.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public abstract class ProcessMemoryException extends RuntimeException {

    public ProcessMemoryException() {
        super();
    }

    public ProcessMemoryException(String s) {
        super(s);
    }

    public ProcessMemoryException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ProcessMemoryException(Throwable throwable) {
        super(throwable);
    }
}
