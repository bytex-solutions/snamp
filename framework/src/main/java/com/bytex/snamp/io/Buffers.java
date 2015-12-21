package com.bytex.snamp.io;

import com.google.common.primitives.*;

import java.nio.*;
import java.util.Objects;
import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Represents a set of methods to work with {@link java.nio.Buffer}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class Buffers {
    /**
     * Represents size of the {@code char} data type, in bytes
     */
    public static final int CHAR_SIZE_IN_BYTES = Chars.BYTES;

    /**
     * Represents size of the {@code short} data type, in bytes
     */
    public static final int SHORT_SIZE_IN_BYTES = Shorts.BYTES;

    /**
     * Represents size of the {@code int} data type, in bytes
     */
    public static final int INT_SIZE_IN_BYTES = Ints.BYTES;

    /**
     * Represents size of the {@code long} data type, in bytes
     */
    public static final int LONG_SIZE_IN_BYTES = Longs.BYTES;

    /**
     * Represents size of the {@code float} data type, in bytes
     */
    public static final int FLOAT_SIZE_IN_BYTES = Floats.BYTES;

    /**
     * Represents size of the {@code double} data type, in bytes
     */
    public static final int DOUBLE_SIZE_IN_BYTES = Doubles.BYTES;

    /**
     * Allocates a new {@link java.nio.ByteBuffer} with the specified capacity of {@code byte} elements.
     * @param maxNumOfElements Maximum number of bytes that can be placed into the buffer.
     * @param direct {@literal true} to allocate buffer in native memory; {@literal false} to allocate buffer in managed memory.
     * @return A new instance of the buffer.
     */
    public static ByteBuffer allocByteBuffer(final int maxNumOfElements, final boolean direct){
        return direct ? ByteBuffer.allocateDirect(maxNumOfElements) : ByteBuffer.allocate(maxNumOfElements);
    }

    private static ByteBuffer allocByteBuffer(final int maxNumOfElements,
                                              final int elementSize,
                                              final boolean direct){
        return allocByteBuffer(maxNumOfElements * elementSize, direct);
    }

    /**
     * Allocates a new {@link java.nio.CharBuffer} with the specified capacity of {@code char} elements.
     * @param maxNumOfElements Maximum number of characters that can be placed into the buffer.
     * @param direct {@literal true} to allocate buffer in native memory; {@literal false} to allocate buffer in managed memory.
     * @return A new instance of the buffer.
     */
    public static CharBuffer allocCharBuffer(final int maxNumOfElements, final boolean direct) {
        return allocByteBuffer(maxNumOfElements, CHAR_SIZE_IN_BYTES, direct).asCharBuffer();
    }

    /**
     * Allocates a new {@link java.nio.ShortBuffer} with the specified capacity of {@code short} elements.
     * @param maxNumOfElements Maximum number of 2-byte words that can be placed into the buffer.
     * @param direct {@literal true} to allocate buffer in native memory; {@literal false} to allocate buffer in managed memory.
     * @return A new instance of the buffer.
     */
    public static ShortBuffer allocShortBuffer(final int maxNumOfElements, final boolean direct){
        return allocByteBuffer(maxNumOfElements, SHORT_SIZE_IN_BYTES, direct).asShortBuffer();
    }

    /**
     * Allocates a new {@link java.nio.IntBuffer} with the specified capacity of {@code int} elements.
     * @param maxNumOfElements Maximum number of 4-byte words that can be placed into the buffer.
     * @param direct {@literal true} to allocate buffer in native memory; {@literal false} to allocate buffer in managed memory.
     * @return A new instance of the buffer.
     */
    public static IntBuffer allocIntBuffer(final int maxNumOfElements, final boolean direct){
        return allocByteBuffer(maxNumOfElements, INT_SIZE_IN_BYTES, direct).asIntBuffer();
    }

    /**
     * Allocates a new {@link java.nio.LongBuffer} with the specified capacity of {@code long} elements.
     * @param maxNumOfElements Maximum number of 8-byte words that can be placed into the buffer.
     * @param direct {@literal true} to allocate buffer in native memory; {@literal false} to allocate buffer in managed memory.
     * @return A new instance of the buffer.
     */
    public static LongBuffer allocLongBuffer(final int maxNumOfElements, final boolean direct){
        return allocByteBuffer(maxNumOfElements, LONG_SIZE_IN_BYTES, direct).asLongBuffer();
    }

    /**
     * Allocates a new {@link java.nio.ByteBuffer} with the specified capacity of {@code float} elements.
     * @param maxNumOfElements Maximum number of 4-byte words that can be placed into the buffer.
     * @param direct {@literal true} to allocate buffer in native memory; {@literal false} to allocate buffer in managed memory.
     * @return A new instance of the buffer.
     */
    public static FloatBuffer allocFloatBuffer(final int maxNumOfElements, final boolean direct){
        return allocByteBuffer(maxNumOfElements, FLOAT_SIZE_IN_BYTES, direct).asFloatBuffer();
    }

    /**
     * Allocates a new {@link java.nio.ByteBuffer} with the specified capacity of {@code double} elements.
     * @param maxNumOfElements Maximum number of 8-byte words that can be placed into the buffer.
     * @param direct {@literal true} to allocate buffer in native memory; {@literal false} to allocate buffer in managed memory.
     * @return A new instance of the buffer.
     */
    public static DoubleBuffer allocDoubleBuffer(final int maxNumOfElements, final boolean direct){
        return allocByteBuffer(maxNumOfElements, DOUBLE_SIZE_IN_BYTES, direct).asDoubleBuffer();
    }

    public static <B extends Buffer> B allocBuffer(final Class<B> bufferType,
                                                   final int maxNumOfElements,
                                                   final boolean direct){
        if(Objects.equals(bufferType, ByteBuffer.class))
            return bufferType.cast(allocByteBuffer(maxNumOfElements, direct));
        else if(Objects.equals(bufferType, CharBuffer.class))
            return bufferType.cast(allocCharBuffer(maxNumOfElements, direct));
        else if(Objects.equals(bufferType, ShortBuffer.class))
            return bufferType.cast(allocShortBuffer(maxNumOfElements, direct));
        else if(Objects.equals(bufferType, IntBuffer.class))
            return bufferType.cast(allocIntBuffer(maxNumOfElements, direct));
        else if(Objects.equals(bufferType, LongBuffer.class))
            return bufferType.cast(allocLongBuffer(maxNumOfElements, direct));
        else if(Objects.equals(bufferType, FloatBuffer.class))
            return bufferType.cast(allocFloatBuffer(maxNumOfElements, direct));
        else if(Objects.equals(bufferType, DoubleBuffer.class))
            return bufferType.cast(allocDoubleBuffer(maxNumOfElements, direct));
        else return null;
    }

    /**
     * Reads all bytes from the specified buffer beginning with its current position.
     * @param buffer The buffer to read.
     * @return An array of bytes restored from the buffer.
     */
    public static byte[] readRemaining(final ByteBuffer buffer){
        if(buffer.hasRemaining()){
            final byte[] result = new byte[buffer.remaining()];
            buffer.get(result);
            return result;
        }
        else return emptyArray(byte[].class);
    }

    public static char[] readRemaining(final CharBuffer buffer){
        if(buffer.hasRemaining()){
            final char[] result = new char[buffer.remaining()];
            buffer.get(result);
            return result;
        }
        else return emptyArray(char[].class);
    }

    public static short[] readRemaining(final ShortBuffer buffer){
        if(buffer.hasRemaining()){
            final short[] result = new short[buffer.remaining()];
            buffer.get(result);
            return result;
        }
        else return emptyArray(short[].class);
    }

    public static int[] readRemaining(final IntBuffer buffer){
        if(buffer.hasRemaining()){
            final int[] result = new int[buffer.remaining()];
            buffer.get(result);
            return result;
        }
        else return emptyArray(int[].class);
    }

    public static long[] readRemaining(final LongBuffer buffer){
        if(buffer.hasRemaining()){
            final long[] result = new long[buffer.remaining()];
            buffer.get(result);
            return result;
        }
        else return emptyArray(long[].class);
    }

    public static float[] readRemaining(final FloatBuffer buffer){
        if(buffer.hasRemaining()){
            final float[] result = new float[buffer.remaining()];
            buffer.get(result);
            return result;
        }
        else return emptyArray(float[].class);
    }

    public static double[] readRemaining(final DoubleBuffer buffer){
        if(buffer.hasRemaining()){
            final double[] result = new double[buffer.remaining()];
            buffer.get(result);
            return result;
        }
        else return emptyArray(double[].class);
    }

    /**
     * Reads an array of available elements from the specified buffer.
     * <p>
     *     The following list describes relationship between runtime type of
     *     the argument ({@link java.nio.Buffer} and output array:
     *     <ul>
     *         <li>{@link java.nio.ByteBuffer} -&gt; {@code byte[]}</li>
     *         <li>{@link java.nio.CharBuffer} -&gt; {@code char[]}</li>
     *         <li>{@link java.nio.ShortBuffer} -&gt; {@code short[]}</li>
     *         <li>{@link java.nio.IntBuffer} -&gt; {@code int[]}</li>
     *         <li>{@link java.nio.LongBuffer} -&gt; {@code long[]}</li>
     *         <li>{@link java.nio.FloatBuffer} -&gt; {@code float[]}</li>
     *         <li>{@link java.nio.DoubleBuffer} -&gt; {@code double[]}</li>
     *     </ul>
     * </p>
     * @param buffer The buffer to read.
     * @return An array of primitive type.
     */
    public static Object readRemaining(final Buffer buffer){
        if(buffer instanceof ByteBuffer)
            return readRemaining((ByteBuffer)buffer);
        else if(buffer instanceof CharBuffer)
            return readRemaining((CharBuffer)buffer);
        else if(buffer instanceof ShortBuffer)
            return readRemaining((ShortBuffer)buffer);
        else if(buffer instanceof IntBuffer)
            return readRemaining((IntBuffer)buffer);
        else if(buffer instanceof LongBuffer)
            return readRemaining((LongBuffer)buffer);
        else if(buffer instanceof FloatBuffer)
            return readRemaining((FloatBuffer)buffer);
        else if(buffer instanceof DoubleBuffer)
            return readRemaining((DoubleBuffer)buffer);
        else return null;
    }

    private static <B extends Buffer> B rewind(final B buffer){
        buffer.rewind();
        return buffer;
    }

    public static ByteBuffer wrap(final byte... items){
        return rewind(ByteBuffer.wrap(items));
    }

    public static CharBuffer wrap(final char... items){
        return rewind(CharBuffer.wrap(items));
    }

    public static ShortBuffer wrap(final short... items){
        return rewind(ShortBuffer.wrap(items));
    }

    public static IntBuffer wrap(final int... items){
        return rewind(IntBuffer.wrap(items));
    }

    public static LongBuffer wrap(final long... items){
        return rewind(LongBuffer.wrap(items));
    }

    public static FloatBuffer wrap(final float... items){
        return rewind(FloatBuffer.wrap(items));
    }

    public static DoubleBuffer wrap(final double... items){
        return rewind(DoubleBuffer.wrap(items));
    }

    public static ByteBuffer toByteBuffer(final CharBuffer buffer){
        final ByteBuffer result = allocByteBuffer(buffer.remaining(), CHAR_SIZE_IN_BYTES, true);
        while (buffer.hasRemaining())
            result.putChar(buffer.get());
        result.rewind();
        return result;
    }

    public static ByteBuffer toByteBuffer(final ShortBuffer buffer) {
        final ByteBuffer result = allocByteBuffer(buffer.remaining(), SHORT_SIZE_IN_BYTES, true);
        while (buffer.hasRemaining())
            result.putShort(buffer.get());
        result.rewind();
        return result;
    }

    public static ByteBuffer toByteBuffer(final IntBuffer buffer) {
        final ByteBuffer result = allocByteBuffer(buffer.remaining(), INT_SIZE_IN_BYTES, true);
        while (buffer.hasRemaining())
            result.putInt(buffer.get());
        result.rewind();
        return result;
    }

    public static ByteBuffer toByteBuffer(final LongBuffer buffer) {
        final ByteBuffer result = allocByteBuffer(buffer.remaining(), LONG_SIZE_IN_BYTES, true);
        while (buffer.hasRemaining())
            result.putLong(buffer.get());
        result.rewind();
        return result;
    }

    public static ByteBuffer toByteBuffer(final FloatBuffer buffer) {
        final ByteBuffer result = allocByteBuffer(buffer.remaining(), FLOAT_SIZE_IN_BYTES, true);
        while (buffer.hasRemaining())
            result.putFloat(buffer.get());
        result.rewind();
        return result;
    }

    public static ByteBuffer toByteBuffer(final DoubleBuffer buffer) {
        final ByteBuffer result = allocByteBuffer(buffer.remaining(), DOUBLE_SIZE_IN_BYTES, true);
        while (buffer.hasRemaining())
            result.putDouble(buffer.get());
        result.rewind();
        return result;
    }

    public static Class<?> getArrayType(final Class<? extends Buffer> bufferType) {
        if(bufferType == null) return null;
        else if(ByteBuffer.class.isAssignableFrom(bufferType))
            return byte[].class;
        else if(CharBuffer.class.isAssignableFrom(bufferType))
            return char[].class;
        else if(ShortBuffer.class.isAssignableFrom(bufferType))
            return short[].class;
        else if(IntBuffer.class.isAssignableFrom(bufferType))
            return int[].class;
        else if(LongBuffer.class.isAssignableFrom(bufferType))
            return long[].class;
        else if(FloatBuffer.class.isAssignableFrom(bufferType))
            return float[].class;
        else if(DoubleBuffer.class.isAssignableFrom(bufferType))
            return double[].class;
        else return null;
    }
}
