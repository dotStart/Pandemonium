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
package tv.dotstart.pandemonium.process.exception;

/**
 * Provides an exception for cases where attaching or detaching to or from a process fails due to
 * the VMs lack of permissions in the operating system.
 *
 * This usually means that administrator permissions are required to attach to a process in order to
 * read and write to its memory. In most cases this means that this application needs to be
 * restarted with the proper permissions.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class ProcessPermissionException extends ProcessAttachmentException {

    public ProcessPermissionException() {
        super();
    }

    public ProcessPermissionException(String s) {
        super(s);
    }

    public ProcessPermissionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ProcessPermissionException(Throwable throwable) {
        super(throwable);
    }
}
