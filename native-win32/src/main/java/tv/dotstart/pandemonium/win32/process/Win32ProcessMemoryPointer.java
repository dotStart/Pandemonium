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

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.ptr.IntByReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.process.Process;
import tv.dotstart.pandemonium.process.ProcessMemoryPointer;
import tv.dotstart.pandemonium.process.ProcessModule;
import tv.dotstart.pandemonium.process.exception.memory.ProcessMemoryReadException;
import tv.dotstart.pandemonium.process.exception.memory.ProcessMemoryStateException;
import tv.dotstart.pandemonium.process.exception.memory.ProcessMemoryWriteException;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class Win32ProcessMemoryPointer implements ProcessMemoryPointer {
    private static final Logger logger = LogManager.getFormatterLogger(Win32ProcessMemoryPointer.class);

    private final Win32Process process;
    private final Win32ProcessModule module;
    private final Pointer baseAddress;
    private final long[] offsets;

    Win32ProcessMemoryPointer(@Nonnull Win32Process process, @Nonnull Win32ProcessModule module, @Nonnull Pointer baseAddress, @Nonnull long[] offsets) {
        this.process = process;
        this.module = module;
        this.baseAddress = baseAddress;
        this.offsets = offsets;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessModule getModule() {
        return this.module;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Process getProcess() {
        return this.process;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeep() {
        return this.offsets.length > 1;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer pointer(@Nonnegative long offset, @Nonnegative long... offsets) {
        long[] combined = new long[this.offsets.length + offsets.length];
        System.arraycopy(this.offsets, 0, combined, 0, this.offsets.length);
        System.arraycopy(offsets, this.offsets.length, combined, 0, offsets.length);

        return new Win32ProcessMemoryPointer(this.process, this.module, this.baseAddress.share(offset), combined);
    }

    /**
     * Reads an arbitrary amount of data from process memory.
     */
    @Nonnull
    public Memory read(@Nonnull Pointer pointer, @Nonnegative int length) {
        this.process.validate();

        if (this.process.accessHandle == null) {
            throw new ProcessMemoryStateException("Process is closed");
        }

        Memory memory = new Memory(length);

        IntByReference bytesRead = new IntByReference();
        Kernel32.INSTANCE.ReadProcessMemory(this.process.accessHandle, pointer, memory, length, bytesRead);

        if (bytesRead.getValue() != length) {
            throw new ProcessMemoryReadException(String.format("Failed to read process memory from address 0x%016X: Expected %d bytes but received %d", Pointer.nativeValue(pointer), length, bytesRead.getValue()));
        }

        return memory;
    }

    /**
     * Reads an arbitrary amount of data from process memory.
     */
    @Nonnull
    public Memory read(@Nonnegative long offset, @Nonnegative int length) {
        return this.read(this.resolvePointer(offset), length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte(@Nonnegative long offset) {
        return this.read(offset, 1).getByte(0);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer readByteArray(@Nonnegative long offset, @Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int arrayLength) {
        this.read(offset, arrayLength).read(0, array, arrayOffset, arrayLength);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer readByteBuffer(@Nonnegative long offset, @Nonnull ByteBuffer buffer, @Nonnegative int bufferOffset, @Nonnegative int bufferLength) {
        byte[] array = new byte[bufferLength];
        this.readByteArray(offset, array);

        buffer.position(bufferOffset);
        buffer.put(array, 0, array.length);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double readDouble(@Nonnegative long offset) {
        return this.read(offset, 8).getDouble(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float readFloat(@Nonnegative long offset) {
        return this.read(offset, 4).getFloat(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInteger(@Nonnegative long offset) {
        return this.read(offset, 4).getInt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong(@Nonnegative long offset) {
        return this.read(offset, 8).getInt(0);
    }

    /**
     * Reads a pointer value from the target address.
     *
     * This is a direct replacement for {@link Memory#getPointer(long)} which respects the target
     * process architecture.
     */
    @Nonnull
    public Pointer readPointer(@Nonnegative Pointer pointer) {
        if (this.process.is64Bit()) {
            return new Pointer(this.read(pointer, 8).getLong(0));
        }

        return new Pointer(this.read(pointer, 4).getInt(0));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer readPointerAddress(@Nonnegative long offset) {
        return new Win32ProcessMemoryPointer(this.process, this.module, this.readPointer(this.resolvePointer(offset)), new long[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort(@Nonnegative long offset) {
        return this.read(offset, 4).getShort(0);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String readString(@Nonnegative long offset, @Nonnegative int maxLength) {
        return this.read(offset, maxLength).getString(0);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <S extends Structure> S readStructure(@Nonnegative long offset, @Nonnegative Class<S> type) {
        try {
            MethodHandle constructor = MethodHandles.publicLookup().findConstructor(type, MethodType.methodType(void.class, Pointer.class));
            S structure = (S) constructor.invoke(this.resolvePointer(offset));
            structure.read();

            return structure;
        } catch (ProcessMemoryReadException | ProcessMemoryStateException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new ProcessMemoryReadException("Cannot read structure: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readUnsignedByte(@Nonnegative long offset) {
        return (short) (this.read(offset, 1).getByte(0) & 0xFF);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readUnsignedInteger(@Nonnegative long offset) {
        return this.read(offset, 4).getInt(0) & 0xFFFFFFFFL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedShort(@Nonnegative long offset) {
        return this.read(offset, 2).getShort(0) & 0xFFFF;
    }

    /**
     * Resolves a pointer within this address space.
     */
    @Nonnull
    public Pointer resolvePointer(@Nonnegative long offset) {
        Pointer address = this.baseAddress;

        for (long deepOffset : this.offsets) {
            address = this.readPointer(address).share(deepOffset);
        }

        return address.share(offset);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer resolve(@Nonnegative long offset) {
        return new Win32ProcessMemoryPointer(this.process, this.module, this.resolvePointer(offset), new long[0]);
    }

    /**
     * Writes an arbitrary value into memory.
     */
    @Nonnull
    public Win32ProcessMemoryPointer write(@Nonnegative long offset, @Nonnegative int length, @Nonnegative Pointer sourcePointer) {
        Pointer pointer = this.resolvePointer(offset);
        IntByReference bytesWritten = new IntByReference();
        Kernel32.INSTANCE.WriteProcessMemory(this.process.accessHandle, pointer, sourcePointer, length, bytesWritten);

        if (bytesWritten.getValue() != length) {
            throw new ProcessMemoryWriteException(String.format("Failed to write process memory at address 0x%016X: Expected to write %d bytes but wrote %d bytes", Pointer.nativeValue(pointer), length, bytesWritten.getValue()));
        }

        return this;
    }

    /**
     * Writes an arbitrary value into memory.
     */
    @Nonnull
    public Win32ProcessMemoryPointer write(@Nonnegative long offset, @Nonnegative int length, @Nonnull Consumer<Memory> writer) {
        this.process.validate();

        if (this.process.accessHandle == null) {
            throw new ProcessMemoryStateException("Process is closed");
        }

        Memory memory = new Memory(length);
        writer.accept(memory);

        return this.write(offset, length, memory);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer writeByte(@Nonnegative long offset, short value) {
        return this.write(offset, 1, (m) -> m.setByte(0, (byte) value));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer writeByteArray(@Nonnegative long offset, @Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int arrayLength) {
        return this.write(offset, arrayLength, (m) -> m.write(0, array, arrayOffset, arrayLength));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer writeByteBuffer(@Nonnegative long offset, @Nonnull ByteBuffer buffer, @Nonnegative int bufferOffset, @Nonnegative int bufferLength) {
        buffer.position(bufferOffset);

        // since non-direct buffers are located on the heap we cannot actually refer to them in the
        // context of OS calls so we'll just convert them into an array of bytes and write the data
        // directly
        if (!buffer.isDirect()) {
            byte[] data = new byte[bufferLength];
            buffer.get(data);

            return this.writeByteArray(offset, data);
        }

        // direct buffers are located off-heap in a dedicated memory area which we can easily
        // provide a pointer to and thus we'll just pass a pointer to the backing API as if we
        // allocated this memory specifically for this task
        // FIXME: Evaluate whether buffer positions affect this process as expected
        return this.write(offset, bufferLength, Native.getDirectBufferPointer(buffer));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer writeDouble(@Nonnegative long offset, double value) {
        return this.write(offset, 8, (m) -> m.setDouble(0, value));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer writeFloat(@Nonnegative long offset, float value) {
        return this.write(offset, 4, (m) -> m.setFloat(0, value));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer writeInteger(@Nonnegative long offset, long value) {
        return this.write(offset, 4, (m) -> m.setInt(0, (int) value));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer writeLong(@Nonnegative long offset, long value) {
        return this.write(offset, 8, (m) -> m.setLong(0, value));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer writeShort(@Nonnegative long offset, int value) {
        return this.write(offset, 2, (m) -> m.setShort(0, (short) value));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer writeString(@Nonnegative long offset, @Nonnull String value) {
        return this.write(offset, value.length(), (m) -> m.setString(0, value));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ProcessMemoryPointer writeStructure(@Nonnegative long offset, @Nonnull Structure structure) {
        structure.write();

        return this.write(offset, Native.getNativeSize(structure.getClass()), structure.getPointer());
    }
}
