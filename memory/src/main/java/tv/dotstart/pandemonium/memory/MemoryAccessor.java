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
package tv.dotstart.pandemonium.memory;

import com.sun.jna.Structure;

import java.math.BigInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Signed;

import tv.dotstart.pandemonium.memory.exception.MemoryAddressException;
import tv.dotstart.pandemonium.memory.exception.MemoryAddressOutOfBoundsException;
import tv.dotstart.pandemonium.memory.exception.MemoryReadException;
import tv.dotstart.pandemonium.memory.exception.MemoryStateException;
import tv.dotstart.pandemonium.memory.exception.MemoryWriteException;
import tv.dotstart.pandemonium.memory.process.Process;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface MemoryAccessor {

    /**
     * Creates a pointer at the specified base address and offsets.
     *
     * If one or more offsets are supplied, a deep pointer is created. Check {@link
     * MemoryPointer#getOffsets()} for more information.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer createPointer(@Nonnegative long offset, @Nonnull long... offsets) throws MemoryAddressException, MemoryStateException;

    /**
     * Retrieves the process this memory area belongs to.
     */
    @Nonnull
    Process getProcess();

    /**
     * Reads a byte value from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Signed
    byte readByte(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an array of byte values from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor readByteArray(@Nonnegative long offset, @Nonnull byte[] array) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an array of byte values from this memory area.
     *
     * @throws ArrayIndexOutOfBoundsException    when the supplied array offset or length is out of
     *                                           bounds.
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor readByteArray(@Nonnegative long offset, @Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int length) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an unsigned byte value from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnegative
    default short readUnsignedByte(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException {
        return (short) (this.readByte(offset) & 0xFF);
    }

    /**
     * Reads a short value from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Signed
    short readShort(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an unsigned short value from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnegative
    default int readUnsignedShort(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException {
        return this.readShort(offset) & 0xFFFF;
    }

    /**
     * Reads an integer value from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Signed
    int readInteger(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an unsigned integer value from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnegative
    default long readUnsignedInteger(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException {
        return this.readInteger(offset) & 0xFFFFFFFFL;
    }

    /**
     * Reads a float value from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    float readFloat(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a long value from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Signed
    long readLong(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an unsigned long value from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    @Nonnegative
    default BigInteger readUnsignedLong(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException {
        byte[] array = new byte[8];
        this.readByteArray(offset, array);

        return new BigInteger(1, array);
    }

    /**
     * Reads a double value from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    double readDouble(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a native encoded NUL terminated string from this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    String readString(@Nonnegative long offset) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a native encoded NUL terminated string from this memory area.
     *
     * This method is guaranteed to never exceed the maximum length supplied in its second
     * parameter.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    String readString(@Nonnegative long offset, @Nonnegative int maximumLength) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a struct from this memory area.
     *
     * @throws IllegalArgumentException          when the supplied struct implementation lacks a
     *                                           pointer based constructor.
     * @throws IllegalStateException             when constructing the struct causes an exception to
     *                                           be thrown.
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    <S extends Structure> S readStruct(@Nonnegative long offset, @Nonnull Class<S> type) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Writes a byte value into this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor writeByte(@Nonnegative long offset, short value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes an array of byte values into this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryWriteException              when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor writeByteArray(@Nonnegative long offset, @Nonnull byte[] array) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes an array of byte values into this memory area.
     *
     * @throws ArrayIndexOutOfBoundsException    when the array offset or length is out of bounds.
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor writeByteArray(@Nonnegative long offset, @Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int length) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes a short value into this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor writeShort(@Nonnegative long offset, int value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes an integer value into this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor writeInteger(@Nonnegative long offset, long value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes a long value into this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor writeLong(@Nonnegative long offset, long value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes an unsigned long value into this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor writeLong(@Nonnegative long offset, @Nonnull @Nonnegative BigInteger value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes a native encoded NUL terminated string into this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor writeString(@Nonnegative long offset, @Nonnull String string) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes a struct into this memory area.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryAccessor writeStruct(@Nonnegative long offset, @Nonnull Structure structure) throws MemoryAddressException, MemoryWriteException, MemoryStateException;
}
