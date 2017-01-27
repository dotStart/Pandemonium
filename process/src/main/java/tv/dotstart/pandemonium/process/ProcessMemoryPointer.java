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
package tv.dotstart.pandemonium.process;

import com.sun.jna.Structure;

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tv.dotstart.pandemonium.process.exception.memory.ProcessMemoryReadException;
import tv.dotstart.pandemonium.process.exception.memory.ProcessMemoryStateException;
import tv.dotstart.pandemonium.process.exception.memory.ProcessMemoryWriteException;

/**
 * Represents a pointer to an address in process memory.
 *
 * This interface provides the main method of accessing specific memory addresses of a game and
 * provides methods for reading and writing various standard data types.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public interface ProcessMemoryPointer {

    /**
     * Retrieves the process module this pointer is referencing.
     */
    @Nonnull
    ProcessModule getModule();

    /**
     * Retrieves the process this pointer is referencing.
     */
    @Nonnull
    Process getProcess();

    /**
     * Checks whether this pointer is considered "deep".
     *
     * Deep pointers do not directly reference an address in process memory but instead consist of a
     * chain of pointers with additional offsets along the way.
     *
     * A pointer is considered a deep pointer if it consists of one or more offsets apart from its
     * respective base address.
     */
    boolean isDeep();

    /**
     * Checks whether this pointer is considered "direct".
     *
     * Direct pointers refer to an address directly (as their name suggests) and its values can be
     * accessed with as little as one read call to the backing API.
     */
    default boolean isDirect() {
        return !this.isDeep();
    }

    /**
     * Creates a pointer relative to this pointer's position.
     *
     * When one or more offsets are supplied, a deep pointer is created using the new offset.
     *
     * @param offset  an offset to resolve the new base pointer from.
     * @param offsets an array of offsets to resolve nested pointers from.
     * @see #resolve() to convert pointers constructed by this method into direct pointers.
     */
    @Nonnull
    ProcessMemoryPointer pointer(@Nonnegative long offset, @Nonnegative long... offsets);

    /**
     * Reads a single byte from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    default byte readByte() {
        return this.readByte(0);
    }

    /**
     * Reads a single byte from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    byte readByte(@Nonnegative long offset);

    /**
     * Reads a byte array from from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer readByteArray(@Nonnull byte[] array) {
        return this.readByteArray(0, array);
    }

    /**
     * Reads a byte array from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer readByteArray(@Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int arrayLength) {
        return this.readByteArray(0, array, arrayOffset, arrayLength);
    }

    /**
     * Reads a byte array from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer readByteArray(@Nonnegative long offset, @Nonnull byte[] array) {
        return this.readByteArray(offset, array, 0, array.length);
    }

    /**
     * Reads a byte array from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer readByteArray(@Nonnegative long offset, @Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int arrayLength);

    /**
     * Reads a byte buffer from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer readByteBuffer(@Nonnull ByteBuffer buffer) {
        return this.readByteBuffer(0, buffer);
    }

    /**
     * Reads a byte buffer from the address this pointer reference.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer readByteBuffer(@Nonnull ByteBuffer buffer, @Nonnegative int bufferOffset, @Nonnegative int bufferLength) {
        return this.readByteBuffer(0, buffer, bufferOffset, bufferLength);
    }

    /**
     * Reads a byte buffer from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer readByteBuffer(@Nonnegative long offset, @Nonnull ByteBuffer buffer) {
        return this.readByteBuffer(offset, buffer, 0, buffer.limit() - buffer.position());
    }

    /**
     * Reads a byte buffer from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer readByteBuffer(@Nonnegative long offset, @Nonnull ByteBuffer buffer, @Nonnegative int bufferOffset, @Nonnegative int bufferLength);

    /**
     * Reads a double from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    default double readDouble() {
        return this.readDouble(0);
    }

    /**
     * Reads a double from this address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    double readDouble(@Nonnegative long offset);

    /**
     * Reads a float from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    default float readFloat() {
        return this.readFloat(0);
    }

    /**
     * Reads a float from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    float readFloat(@Nonnegative long offset);

    /**
     * Reads an integer from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    default int readInteger() {
        return this.readInteger(0);
    }

    /**
     * Reads an integer from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    int readInteger(@Nonnegative long offset);

    /**
     * Reads a long from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    default long readLong() {
        return this.readLong(0);
    }

    /**
     * Reads a long from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    long readLong(@Nonnegative long offset);

    /**
     * Reads a pointer address depending on the process pointer sizes.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer readPointerAddress() {
        return this.readPointerAddress(0);
    }

    /**
     * Reads a pointer address depending on the process pointer sizes.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer readPointerAddress(@Nonnegative long offset);

    /**
     * Reads a short from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    default short readShort() {
        return this.readShort(0);
    }

    /**
     * Reads a short from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    short readShort(@Nonnegative long offset);

    /**
     * Reads a NUL terminated string of a maximum length from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    default String readString(@Nonnegative int maxLength) {
        return this.readString(0, maxLength);
    }

    /**
     * Reads a NUL terminated string of a maximum length from the address this pointer references
     * plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    String readString(@Nonnegative long offset, @Nonnegative int maxLength);

    /**
     * Reads a structure from the address this pointer references.
     *
     * @param <S> a structure type.
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    default <S extends Structure> S readStructure(@Nonnull Class<S> type) {
        return this.readStructure(0, type);
    }

    /**
     * Reads a structure from the address this pointer references plus the supplied offset.
     *
     * @param <S> a structure type.
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    <S extends Structure> S readStructure(@Nonnegative long offset, @Nonnegative Class<S> type);

    /**
     * Reads an unsigned byte from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnegative
    default short readUnsignedByte() {
        return this.readUnsignedByte(0);
    }

    /**
     * Reads an unsigned byte from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnegative
    short readUnsignedByte(@Nonnegative long offset);

    /**
     * Reads an unsigned integer from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnegative
    default long readUnsignedInteger() {
        return this.readUnsignedInteger(0);
    }

    /**
     * Reads an unsigned integer from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnegative
    long readUnsignedInteger(@Nonnegative long offset);

    /**
     * Reads an unsigned short from the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnegative
    default int readUnsignedShort() {
        return this.readUnsignedShort(0);
    }

    /**
     * Reads an unsigned short from the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnegative
    int readUnsignedShort(@Nonnegative long offset);

    /**
     * Resolves this pointer's address and turns it into a non-deep pointer which references the
     * target address directly.
     *
     * Note: It may not be safe to make use of this method in some games since addresses may change
     * at runtime. However in many cases resolving a pointer may result in increased performance due
     * to the lower amount of API calls to the operating system.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer resolve() {
        return this.resolve(0);
    }

    /**
     * Resolves this pointer's address and turns it into a non-deep pointer which references the
     * target address plus a supplied offset directly.
     *
     * If this pointer is already a direct pointer, a reference to this pointer is returned instead
     * of creating a new instance with equal properties.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryReadException  when reading from the process memory fails.
     * @see #resolve(long) for a more detailed description of this method.
     */
    @Nonnull
    ProcessMemoryPointer resolve(@Nonnegative long offset);

    /**
     * Writes a byte value to the address this pointer references.
     *
     * When a value bigger than byte is passed (such as a short), its bounds will be limited to byte
     * bounds by cutting off all remaining bits. This behavior replicates unsigned types as found in
     * other programming languages.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeByte(short value) {
        return this.writeByte(0, value);
    }

    /**
     * Writes a byte value to the address this pointer references plus the supplied offset.
     *
     * When a value bigger than byte is passed (such as a short), its bounds will be limited to byte
     * bounds by cutting off all remaining bits. This behavior replicates unsigned types as found in
     * other programming languages.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer writeByte(@Nonnegative long offset, short value);

    /**
     * Writes a byte array to the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeByteArray(@Nonnull byte[] array) {
        return this.writeByteArray(0, array);
    }

    /**
     * Writes a byte array to the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeByteArray(@Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int arrayLength) {
        return this.writeByteArray(0, array, arrayOffset, arrayLength);
    }

    /**
     * Writes a byte array to the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeByteArray(@Nonnegative long offset, @Nonnull byte[] array) {
        return this.writeByteArray(offset, array, 0, array.length);
    }

    /**
     * Writes a byte array to the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer writeByteArray(@Nonnegative long offset, @Nonnull byte[] array, @Nonnegative int arrayOffset, @Nonnegative int arrayLength);

    /**
     * Writes a byte buffer to the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeByteBuffer(@Nonnull ByteBuffer buffer) {
        return this.writeByteBuffer(0, buffer);
    }

    /**
     * Writes a byte buffer to the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeByteBuffer(@Nonnull ByteBuffer buffer, @Nonnegative int bufferOffset, @Nonnegative int bufferLength) {
        return this.writeByteBuffer(0, buffer, bufferOffset, bufferLength);
    }

    /**
     * Writes a byte buffer to the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeByteBuffer(@Nonnegative long offset, @Nonnull ByteBuffer buffer) {
        return this.writeByteBuffer(offset, buffer, buffer.position(), buffer.remaining());
    }

    /**
     * Writes a byte buffer to the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer writeByteBuffer(@Nonnegative long offset, @Nonnull ByteBuffer buffer, @Nonnegative int bufferOffset, @Nonnegative int bufferLength);

    /**
     * Writes a double to the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeDouble(double value) {
        return this.writeDouble(0, value);
    }

    /**
     * Writes a double to the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer writeDouble(@Nonnegative long offset, double value);

    /**
     * Writes a float to the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeFloat(float value) {
        return this.writeFloat(0, value);
    }

    /**
     * Writes a float to the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer writeFloat(@Nonnegative long offset, float value);

    /**
     * Writes an integer to the address this pointer references.
     *
     * When a value bigger than integer is passed (such as a short), its bounds will be limited to
     * byte bounds by cutting off all remaining bits. This behavior replicates unsigned types as
     * found in other programming languages.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeInteger(long value) {
        return this.writeInteger(0, value);
    }

    /**
     * Writes an integer to the address this pointer references plus the supplied offset.
     *
     * When a value bigger than integer is passed (such as a short), its bounds will be limited to
     * integer bounds by cutting off all remaining bits. This behavior replicates unsigned types as
     * found in other programming languages.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer writeInteger(@Nonnegative long offset, long value);

    /**
     * Writes a long value to the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeLong(long value) {
        return this.writeLong(0, value);
    }

    /**
     * Writes a long value to the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer writeLong(@Nonnegative long offset, long value);

    /**
     * Writes a short value to the address this pointer references.
     *
     * When a value bigger than byte is passed (such as a short), its bounds will be limited to byte
     * bounds by cutting off all remaining bits. This behavior replicates unsigned types as found in
     * other programming languages without introducing new boxed types to the library.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeShort(int value) {
        return this.writeShort(0, value);
    }

    /**
     * Writes a short value to the address this pointer references plus the supplied offset.
     *
     * When a value bigger than byte is passed (such as a short), its bounds will be limited to byte
     * bounds by cutting off all remaining bits. This behavior replicates unsigned types as found in
     * other programming languages without introducing new boxed types to the library.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer writeShort(@Nonnegative long offset, int value);

    /**
     * Writes a string value to the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeString(@Nonnull String value) {
        return this.writeString(0, value);
    }

    /**
     * Writes a string value to the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer writeString(@Nonnegative long offset, @Nonnull String value);

    /**
     * Writes a string value of a certain length to the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeString(@Nonnull String value, @Nonnegative int maxLength) {
        return this.writeString(0, value, maxLength);
    }

    /**
     * Writes a string value of a certain length to the address this pointer references plus the
     * supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeString(@Nonnegative long offset, @Nonnull String value, @Nonnegative int maxLength) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException("String exceeded maximum bounds: Expected maximum length of " + maxLength + " characters but got " + value.length());
        }

        return this.writeString(offset, value);
    }

    /**
     * Writes a structure to the address this pointer references.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    default ProcessMemoryPointer writeStructure(@Nonnull Structure structure) {
        return this.writeStructure(0, structure);
    }

    /**
     * Writes a structore to the address this pointer references plus the supplied offset.
     *
     * @throws ProcessMemoryStateException when the process or memory state prevents access.
     * @throws ProcessMemoryWriteException when writing to the process memory fails.
     */
    @Nonnull
    ProcessMemoryPointer writeStructure(@Nonnegative long offset, @Nonnull Structure structure);
}
