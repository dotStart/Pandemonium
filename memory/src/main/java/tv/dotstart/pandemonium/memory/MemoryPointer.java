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

import tv.dotstart.pandemonium.memory.exception.MemoryAddressException;
import tv.dotstart.pandemonium.memory.exception.MemoryAddressOutOfBoundsException;
import tv.dotstart.pandemonium.memory.exception.MemoryReadException;
import tv.dotstart.pandemonium.memory.exception.MemoryStateException;
import tv.dotstart.pandemonium.memory.exception.MemoryWriteException;

/**
 * Represents a pointer to a certain area in memory.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface MemoryPointer extends MemoryAccessor {

    /**
     * Retrieves the base address in memory this pointer is referring to.
     *
     * This address is relative to the virtual process memory and may thus exceed module bounds.
     */
    @Nonnegative
    long getBaseAddress();

    /**
     * Retrieves the parent memory accessor which backs all calls in this pointer.
     */
    @Nonnull
    MemoryAccessor getMemoryAccessor();

    /**
     * Retrieves the offset this pointer refers to.
     *
     * When more than one offset is specified, the pointer is considered a deep pointer. In this
     * case the implementation will keep resolving pointers until a single offset is left.
     */
    @Nonnull
    long[] getOffsets();

    /**
     * Checks whether this pointer is a deep pointer (e.g. it references at least one more pointer
     * in order to gain access to its actual target memory area).
     */
    boolean isDeep();

    /**
     * Reads a byte value from this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    byte readByte() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an array of byte values from this pointer's address.
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
    MemoryPointer readByteArray(@Nonnull byte[] array) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer readByteArray(@Nonnegative long offset, @Nonnull byte[] array) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an array of byte values from this pointer's address.
     *
     * @throws ArrayIndexOutOfBoundsException    when the supplied array offset and length exceeds
     *                                           the bounds.
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or memory length is out
     *                                           of memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer readByteArray(@Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int length) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer readByteArray(@Nonnegative long offset, @Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int length) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an unsigned byte value from this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnegative
    short readUnsignedByte() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a short value from this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    short readShort() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an unsigned short value from this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnegative
    int readUnsignedShort() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an integer value from this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    int readInteger() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an unsigned integer from this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnegative
    long readUnsignedInteger() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a float value from this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    float readFloat() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a long value from this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    long readLong() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads an unsigned long value from this pointer's address.
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
    @Nonnegative
    BigInteger readUnsignedLong() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a double value from this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address or array length is out
     *                                           of
     *                                           memory bounds.
     * @throws MemoryReadException               when reading from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    double readDouble() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a NUL terminated string from this pointer's address.
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
    String readString() throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a NUL terminated string of a certain length from this pointer's address.
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
    String readString(@Nonnegative int maximumLength) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a struct from this pointer's address.
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
    <S extends Structure> S readStruct(@Nonnull Class<S> type) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Reads a struct from this pointer's address.
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
    MemoryPointer readStruct(@Nonnull Structure structure) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer readStruct(@Nonnegative long offset, @Nonnull Structure structure) throws MemoryAddressException, MemoryReadException, MemoryStateException;

    /**
     * Writes a byte value to this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer writeByte(short value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer writeByte(@Nonnegative long offset, short value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes an array of byte values to this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer writeByteArray(@Nonnull byte[] array) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer writeByteArray(@Nonnegative long offset, @Nonnull byte[] array) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes an array of byte values to this pointer's address.
     *
     * @throws ArrayIndexOutOfBoundsException    when the array length or offset is out of array
     *                                           bounds.
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer writeByteArray(@Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int length) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer writeByteArray(@Nonnegative long offset, @Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int length) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes a short value to this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer writeShort(int value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer writeShort(@Nonnegative long offset, int value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes an integer value to this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer writeInteger(long value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer writeInteger(@Nonnegative long offset, long value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes a long value to this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer writeLong(long value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer writeLong(@Nonnegative long offset, long value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes an unsigned long value to this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer writeLong(@Nonnull @Nonnegative BigInteger value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer writeLong(@Nonnegative long offset, @Nonnull @Nonnegative BigInteger value) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes a NUL terminated string to this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer writeString(@Nonnull String string) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer writeString(@Nonnegative long offset, @Nonnull String string) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * Writes a struct to this pointer's address.
     *
     * @throws MemoryAddressException            when the supplied address is invalid or
     *                                           inaccessible.
     * @throws MemoryAddressOutOfBoundsException when the supplied address is out of memory bounds.
     * @throws MemoryWriteException              when writing from memory fails.
     * @throws MemoryStateException              when the state of this accessor is invalid.
     */
    @Nonnull
    MemoryPointer writeStruct(@Nonnull Structure structure) throws MemoryAddressException, MemoryWriteException, MemoryStateException;

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    MemoryPointer writeStruct(@Nonnegative long offset, @Nonnull Structure structure) throws MemoryAddressException, MemoryWriteException, MemoryStateException;
}
