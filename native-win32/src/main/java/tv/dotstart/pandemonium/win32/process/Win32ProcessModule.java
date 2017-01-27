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
package tv.dotstart.pandemonium.win32.process;

import com.sun.jna.Pointer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.process.ProcessMemoryPointer;
import tv.dotstart.pandemonium.process.ProcessModule;
import tv.dotstart.pandemonium.process.exception.memory.ProcessMemoryStateException;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class Win32ProcessModule implements ProcessModule {
    private final Win32Process process;
    private final Path path;
    private final long size;
    private final Pointer pointer;
    private final Win32ProcessMemoryPointer memoryPointer;

    public Win32ProcessModule(@Nonnull Win32Process process, @Nonnull Path path, @Nonnull Pointer pointer) throws IOException {
        this.process = process;
        this.path = path;
        this.pointer = pointer;

        this.size = Files.size(path);
        this.memoryPointer = new Win32ProcessMemoryPointer(process, this, pointer, new long[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getName() {
        return this.path.getFileName().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Path getPath() {
        return this.path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize() {
        return this.size;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer pointer(@Nonnegative long offset, @Nonnull @Nonnegative long... offsets) throws ProcessMemoryStateException {
        this.process.validate();

        if (this.process.accessHandle == null) {
            throw new ProcessMemoryStateException("Process is closed");
        }

        return new Win32ProcessMemoryPointer(this.process, this, this.pointer.share(offset), offsets);
    }
}
