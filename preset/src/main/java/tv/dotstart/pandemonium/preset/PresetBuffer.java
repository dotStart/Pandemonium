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
package tv.dotstart.pandemonium.preset;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Provides a wrapped buffer implementation which provides methods to en- and decode data types
 * specific to the preset format.
 *
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
public class PresetBuffer {
    private final ByteBuf buffer;

    PresetBuffer(@Nonnull ByteBuf buffer) {
        this.buffer = buffer;
    }

    /**
     * Retrieves the backing buffer implementation.
     */
    @Nonnull
    public ByteBuf getBuffer() {
        return this.buffer;
    }

    /**
     * Decodes a Base64 encoded buffer into a usable format.
     */
    @Nonnull
    public static PresetBuffer of(@Nonnull String encoded) {
        try {
            return wrap(Base64.getDecoder().decode(encoded));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Malformed buffer: Base64 encoding error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Retrieves the amount of bytes left to read in the preset buffer.
     */
    @Nonnegative
    public int readableBytes() {
        return this.buffer.readableBytes();
    }

    /**
     * Reads an array of an arbitrary value type from this buffer.
     *
     * @param function a function capable of pulling a value from this buffer.
     * @param <O>      a value type.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public <O> O[] readArray(@Nonnull Function<PresetBuffer, O> function) {
        O[] array = (O[]) Array.newInstance(Object.class, (int) this.readUnsignedInteger());

        for (int i = 0; i < array.length; ++i) {
            array[i] = function.apply(this);
        }

        return array;
    }

    /**
     * Reads a single byte from the buffer.
     *
     * @see ByteBuf#readByte()
     */
    public byte readByte() {
        return this.buffer.readByte();
    }

    /**
     * Reads an array of byte values from this buffer.
     */
    @Nonnull
    public byte[] readByteArray() {
        byte[] values = new byte[(int) this.readUnsignedInteger()];
        this.readBytes(values);

        return values;
    }

    /**
     * Reads a set of arbitrary value types from this buffer into the supplied collection.
     *
     * @param collection a collection to store the retrieved value in.
     * @param function   a function capable of pulling a value from this buffer.
     * @param <O>        a value type.
     * @param <C>        a collection type.
     */
    @Nonnull
    public <O, C extends Collection<O>> C readCollection(@Nonnull C collection, @Nonnull Function<PresetBuffer, O> function) {
        int length = (int) this.readUnsignedInteger();

        for (int i = 0; i < length; ++i) {
            collection.add(function.apply(this));
        }

        return collection;
    }

    /**
     * Reads a set of bytes into the provided array instance.
     */
    @Nonnull
    public PresetBuffer readBytes(@Nonnull byte[] data) {
        this.buffer.readBytes(data);
        return this;
    }

    /**
     * Reads a set of bytes into the provided array instance at the specified offset using the
     * specified maximum length.
     */
    @Nonnegative
    public PresetBuffer readBytes(@Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length) {
        this.buffer.readBytes(data, offset, length);
        return this;
    }

    /**
     * Reads a fixed-point double value from this buffer.
     */
    public double readDouble() {
        return this.readInteger() / 32d;
    }

    /**
     * Reads an array of fixed-point double values from this buffer.
     */
    @Nonnull
    public double[] readDoubleArray() {
        return ArrayUtils.toPrimitive(this.readArray(PresetBuffer::readDouble));
    }

    /**
     * Reads a set of fixed-point double values from this buffer.
     */
    @Nonnull
    public Set<Double> readDoubleSet() {
        return this.readSet(PresetBuffer::readDouble);
    }

    /**
     * Reads a set of values into a newly created set.
     *
     * @param function a function capable of pulling a value from this buffer.
     * @param <O>      a value type.
     */
    @Nonnull
    public <O> Set<O> readSet(@Nonnull Function<PresetBuffer, O> function) {
        return this.readCollection(new HashSet<>(), function);
    }

    /**
     * Reads a single short from the buffer.
     *
     * @see ByteBuf#readShort()
     */
    public short readShort() {
        return this.buffer.readShort();
    }

    /**
     * Reads an array of short values from this buffer.
     */
    @Nonnull
    public short[] readShortArray() {
        return ArrayUtils.toPrimitive(this.readArray(PresetBuffer::readShort));
    }

    /**
     * Reads a set of short values from this buffer.
     */
    @Nonnull
    public Set<Short> readShortSet() {
        return this.readSet(PresetBuffer::readShort);
    }

    /**
     * Reads an UTF-8 encoded string from this buffer.
     */
    @Nonnull
    public String readString() {
        return new String(this.readByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Reads and decodes a Base128 VarInt from this buffer.
     */
    public int readInteger() {
        int value = (int) this.readUnsignedInteger();
        return (value >>> 1) ^ (value << 31);
    }

    /**
     * Reads an array of integer values from this buffer.
     */
    @Nonnull
    public int[] readIntegerArray() {
        return ArrayUtils.toPrimitive(this.readArray(PresetBuffer::readInteger));
    }

    /**
     * Reads a set of integer values.
     */
    @Nonnull
    public Set<Integer> readIntegerSet() {
        return this.readSet(PresetBuffer::readInteger);
    }

    /**
     * Reads and decodes an unsigned Base128 VarInt encoded integer from this buffer.
     */
    @Nonnegative
    public long readUnsignedInteger() {
        return this.readUnsignedLong(5) & 0xFFFFFFFFL;
    }

    /**
     * Reads and decodes an array of unsigned integer values from this buffer.
     */
    @Nonnull
    public long[] readUnsignedIntegerArray() {
        return ArrayUtils.toPrimitive(this.readArray(PresetBuffer::readUnsignedInteger));
    }

    /**
     * Reads a set of unsigned integer values from this buffer.
     */
    @Nonnegative
    public Set<Long> readUnsignedIntegerSet() {
        return this.readSet(PresetBuffer::readUnsignedInteger);
    }

    /**
     * Reads and decodes a signed Base128 VarInt encoded long from this buffer.
     */
    public long readLong() {
        long value = this.readUnsignedLong();
        return (value >>> 1) ^ (value << 63);
    }

    /**
     * Reads and decodes an array of long values from this buffer.
     */
    @Nonnull
    public long[] readLongArray() {
        return ArrayUtils.toPrimitive(this.readArray(PresetBuffer::readLong));
    }

    /**
     * Reads a set of long values from this buffer.
     */
    @Nonnegative
    public Set<Long> readLongSet() {
        return this.readSet(PresetBuffer::readLong);
    }

    /**
     * Reads and decodes an unsigned Base128 VarInt encoded long from this buffer.
     */
    @Nonnegative
    public long readUnsignedLong() {
        return this.readUnsignedLong(10);
    }

    /**
     * Reads and decodes an array of unsigned long values from this buffer.
     */
    @Nonnull
    public long[] readUnsignedLongArray() {
        return ArrayUtils.toPrimitive((Long[]) this.readArray(PresetBuffer::readUnsignedLong));
    }

    /**
     * Reads and decodes a set of long values from this buffer.
     */
    @Nonnull
    public Set<Long> readUnsignedLongSet() {
        return this.readSet(PresetBuffer::readUnsignedLong);
    }

    /**
     * Reads an unsigned long of the specified maximum length.
     */
    @Nonnegative
    private long readUnsignedLong(@Nonnegative int length) {
        long value = 0;

        for (int i = 0; i < length; ++i) {
            byte bundle = this.buffer.readByte();
            value |= (bundle & 0x7FL) << (i * 7);

            if ((bundle & 0x80) != 0x80) {
                return value;
            }
        }

        throw new IllegalStateException("VarInt size out of bounds: Exceeded a maximum of " + length + " bytes");
    }

    /**
     * Converts the entire buffer into its byte array representation.
     */
    @Nonnull
    public byte[] toByteArray() {
        this.buffer.markReaderIndex();
        this.buffer.readerIndex(0);

        try {
            byte[] array = new byte[this.readableBytes()];
            this.readBytes(array);
            return array;
        } finally {
            this.buffer.resetReaderIndex();
        }
    }

    /**
     * Wraps a standard netty buffer.
     */
    @Nonnull
    public static PresetBuffer wrap(@Nonnull ByteBuf buffer) {
        return new PresetBuffer(buffer);
    }

    /**
     * Wraps an array of encoded bytes.
     */
    @Nonnull
    public static PresetBuffer wrap(@Nonnull byte[] data) {
        return new PresetBuffer(Unpooled.wrappedBuffer(data));
    }

    /**
     * Writes an array of arbitrary data into the buffer.
     *
     * @param array    an array of data.
     * @param consumer a consumer capable of encoding the data into this buffer.
     * @param <I>      a value type.
     * @see #writeUnsignedInteger(long)
     */
    @Nonnull
    public <I> PresetBuffer writeArray(@Nonnull I[] array, @Nonnull BiConsumer<PresetBuffer, I> consumer) {
        this.writeInteger(array.length);

        for (I element : array) {
            consumer.accept(this, element);
        }

        return this;
    }

    /**
     * Writes a single byte into the buffer.
     *
     * @see ByteBuf#writeByte(int)
     */
    @Nonnull
    public PresetBuffer writeByte(int value) {
        this.buffer.writeByte(value);
        return this;
    }

    /**
     * Writes a byte array along with its length into the buffer.
     *
     * @see #writeUnsignedInteger(long)
     * @see #writeBytes(byte[])
     */
    @Nonnull
    public PresetBuffer writeByteArray(@Nonnull byte[] value) {
        return this
                .writeUnsignedInteger(value.length)
                .writeBytes(value);
    }

    /**
     * Writes an array of bytes into the buffer.
     *
     * @see ByteBuf#writeBytes(byte[])
     */
    @Nonnull
    public PresetBuffer writeBytes(@Nonnull byte[] values) {
        this.buffer.writeBytes(values);
        return this;
    }

    /**
     * Writes an array of bytes into the buffer using an array offset using a length.
     *
     * @see ByteBuf#writeBytes(byte[], int, int)
     */
    @Nonnull
    public PresetBuffer writeBytes(@Nonnull byte[] value, @Nonnegative int offset, @Nonnegative int length) {
        this.buffer.writeBytes(value, offset, length);
        return this;
    }

    /**
     * Writes a collection of arbitrary data to the buffer.
     *
     * @param collection a collection of data.
     * @param consumer   a consumer which provides the logic necessary to write the data into the
     *                   buffer.
     * @param <I>        a data type.
     * @see #writeUnsignedInteger(long)
     */
    @Nonnull
    public <I> PresetBuffer writeCollection(@Nonnull Collection<I> collection, @Nonnull BiConsumer<PresetBuffer, I> consumer) {
        this.writeUnsignedInteger(collection.size());
        collection.forEach((e) -> consumer.accept(this, e));
        return this;
    }

    /**
     * Writes a fixed-point double value into the buffer.
     */
    @Nonnull
    public PresetBuffer writeDouble(double value) {
        return this.writeInteger((int) value * 32);
    }

    /**
     * Writes an array of fixed-point double values into the buffer.
     */
    @Nonnull
    public PresetBuffer writeDoubleArray(double[] value) {
        return this.writeArray(ArrayUtils.toObject(value), PresetBuffer::writeDouble);
    }

    /**
     * Writes a collection of fixed-point double values into the buffer.
     */
    @Nonnull
    public PresetBuffer writeDoubleCollection(@Nonnull Collection<Double> values) {
        return this.writeCollection(values, PresetBuffer::writeDouble);
    }

    /**
     * Writes a single short into the buffer.
     *
     * @see ByteBuf#writeShort(int)
     */
    @Nonnull
    public PresetBuffer writeShort(int value) {
        this.buffer.writeShort(value);
        return this;
    }

    /**
     * Writes a UTF-8 encoded string into the buffer.
     *
     * @see #writeByteArray(byte[])
     */
    @Nonnull
    public PresetBuffer writeString(@Nonnull String value) {
        return this.writeByteArray(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes a single Base128 VarInt encoded integer into the buffer.
     *
     * This method makes use of zig zag encoding for signed integers to reduce the size required to
     * represent negative values.
     *
     * @see ByteBuf#writeByte(int) for a documentation on the backing write method.
     */
    @Nonnull
    public PresetBuffer writeInteger(int value) {
        return this.writeUnsignedInteger((value << 1) ^ (value >>> 31));
    }

    /**
     * Writes an array of integers into the buffer.
     *
     * @see #writeArray(Object[], BiConsumer)
     */
    @Nonnull
    public PresetBuffer writeIntegerArray(int[] values) {
        return this.writeArray(ArrayUtils.toObject(values), PresetBuffer::writeInteger);
    }

    /**
     * Writes a collection of integers into the buffer.
     *
     * @see #writeCollection(Collection, BiConsumer)
     */
    @Nonnull
    public PresetBuffer writeIntegerCollection(@Nonnull Collection<Integer> values) {
        return this.writeCollection(values, PresetBuffer::writeInteger);
    }

    /**
     * Writes a single Base128 VarInt encoded integer into the buffer.
     *
     * @see ByteBuf#writeByte(int) for a documentation on the backing write method.
     */
    @Nonnull
    public PresetBuffer writeUnsignedInteger(@Nonnegative long value) {
        return this.writeUnsignedLong(value & 0xFFFFFFFFL);
    }

    /**
     * Writes an array of unsigned integers.
     *
     * @see #writeArray(Object[], BiConsumer)
     */
    @Nonnull
    public PresetBuffer writeUnsignedIntegerArray(@Nonnull long[] values) {
        return this.writeArray(ArrayUtils.toObject(values), PresetBuffer::writeUnsignedInteger);
    }

    /**
     * Writes a collection of unsigned integers.
     *
     * @see #writeCollection(Collection, BiConsumer)
     */
    @Nonnull
    public PresetBuffer writeUnsignedIntegerCollection(@Nonnull Collection<Long> collection) {
        return this.writeCollection(collection, PresetBuffer::writeUnsignedInteger);
    }

    /**
     * Writes a single Base128 VarInt encoded integer into the buffer.
     *
     * This method makes use of zig zag encoding for signed integers to reduce the size required to
     * represent negative values.
     *
     * @see ByteBuf#writeByte(int) for a documentation on the backing write method.
     */
    @Nonnull
    public PresetBuffer writeLong(long value) {
        return this.writeUnsignedLong((value << 1) ^ (value >>> 63));
    }

    /**
     * Writes an array of long values into the buffer.
     *
     * @see #writeUnsignedInteger(long)
     */
    @Nonnull
    public PresetBuffer writeLongArray(@Nonnull long[] values) {
        return this.writeArray(ArrayUtils.toObject(values), PresetBuffer::writeLong);
    }

    /**
     * Writes a collection of long values into the buffer.
     *
     * @see #writeUnsignedInteger(long)
     */
    @Nonnull
    public PresetBuffer writeLongCollection(@Nonnull Collection<Long> values) {
        return this.writeCollection(values, PresetBuffer::writeLong);
    }

    /**
     * Writes a single Base128 VarInt encoded integer into the buffer.
     *
     * @see ByteBuf#writeByte(int) for a documentation on the backing write method.
     */
    @Nonnull
    public PresetBuffer writeUnsignedLong(@Nonnegative long value) {
        do {
            byte bundle = (byte) (value & 0x7F);
            value >>>= 7;

            if (value != 0) {
                bundle |= 0x80;
            }

            this.buffer.writeByte(bundle);
        } while (value != 0);

        return this;
    }

    /**
     * Writes an array of unsigned long values into the buffer.
     *
     * @see #writeUnsignedInteger(long)
     */
    @Nonnull
    public PresetBuffer writeUnsignedLongArray(@Nonnull long[] values) {
        return this.writeArray(ArrayUtils.toObject(values), PresetBuffer::writeUnsignedLong);
    }

    /**
     * Writes a collection of unsigned long values into the buffer.
     *
     * @see #writeUnsignedInteger(long)
     */
    @Nonnull
    public PresetBuffer writeUnsignedLongCollection(@Nonnull Collection<Long> values) {
        return this.writeCollection(values, PresetBuffer::writeUnsignedInteger);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(this.toByteArray());
    }
}
