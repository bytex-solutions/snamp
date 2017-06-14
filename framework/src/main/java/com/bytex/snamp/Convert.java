package com.bytex.snamp;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.io.Buffers;
import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.openmbean.OpenType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.*;

/**
 * Converts a base data type to another base data type.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public final class Convert {
    private static abstract class TypeConverter<C> extends ClassMap<C> {
        private static final long serialVersionUID = -2745877310143387409L;

        final C getConverter(final Object value) {
            return value == null ? null : getOrAdd(value.getClass());
        }
    }

    private static final class ToIntConverter extends TypeConverter<ToIntFunction> {
        private static final long serialVersionUID = -7571891959758237238L;

        private <T> ToIntConverter addConverter(final Class<T> type, final ToIntFunction<? super T> converter) {
            put(type, converter);
            return this;
        }

        @SuppressWarnings("unchecked")
        OptionalInt convert(final Object value) {
            final ToIntFunction converter = getConverter(value);
            return converter == null ?
                    OptionalInt.empty() :
                    OptionalInt.of(converter.applyAsInt(value));
        }
    }

    private static final class ToLongConverter extends TypeConverter<ToLongFunction>{
        private static final long serialVersionUID = 1973865787305278420L;

        <T> ToLongConverter addConverter(final Class<T> type, final ToLongFunction<? super T> converter){
            put(type, converter);
            return this;
        }

        @SuppressWarnings("unchecked")
        OptionalLong convert(final Object value) {
            final ToLongFunction converter = getConverter(value);
            return converter == null ?
                    OptionalLong.empty() :
                    OptionalLong.of(converter.applyAsLong(value));
        }
    }

    private static final class ToDoubleConverter extends TypeConverter<ToDoubleFunction>{
        private static final long serialVersionUID = 7425665427678455939L;

        <T> ToDoubleConverter addConverter(final Class<T> type, final ToDoubleFunction<? super T> converter){
            put(type, converter);
            return this;
        }

        @SuppressWarnings("unchecked")
        OptionalDouble convert(final Object value) {
            final ToDoubleFunction converter = getConverter(value);
            return converter == null ?
                    OptionalDouble.empty() :
                    OptionalDouble.of(converter.applyAsDouble(value));
        }
    }

    private static final class ToTypeConverter<O> extends TypeConverter<Function> {
        private static final long serialVersionUID = 3279547883268029767L;
        private final Class<O> outputType;

        ToTypeConverter(@Nonnull final Class<O> type){
            outputType = type;
        }

        private <I> ToTypeConverter<O> addConverter(final Class<I> type, final Function<? super I, ? extends O> converter) {
            put(type, converter);
            return this;
        }

        @SuppressWarnings("unchecked")
        Optional<O> convert(final Object value) {
            final Function converter = getConverter(value);
            return converter == null ?
                    Optional.empty() :
                    toType(converter.apply(value), outputType);
        }

        Optional<O> convert(final Object value,
                                 final Function<Object, Optional<O>> fallback) {
            final Optional<O> result = convert(value);
            return result.isPresent() ? result : fallback.apply(value);
        }
    }

    private static final LazySoftReference<Convert> INSTANCE = new LazySoftReference<>();

    private final ToIntConverter TO_INT;
    private final ToIntConverter TO_BYTE;
    private final ToIntConverter TO_SHORT;
    private final ToLongConverter TO_LONG;
    private final ToDoubleConverter TO_FLOAT;
    private final ToDoubleConverter TO_DOUBLE;
    private final ToIntConverter TO_CHAR;
    private final ToTypeConverter<BigInteger> TO_BIG_INTEGER;
    private final ToTypeConverter<BigDecimal> TO_BIG_DECIMAL;
    private final ToTypeConverter<Boolean> TO_BOOLEAN;
    private final ToTypeConverter<Date> TO_DATE;
    private final ToTypeConverter<ByteBuffer> TO_BYTE_BUFFER;
    private final ToTypeConverter<CharBuffer> TO_CHAR_BUFFER;
    private final ToTypeConverter<ShortBuffer> TO_SHORT_BUFFER;
    private final ToTypeConverter<IntBuffer> TO_INT_BUFFER;
    private final ToTypeConverter<LongBuffer> TO_LONG_BUFFER;
    private final ToTypeConverter<FloatBuffer> TO_FLOAT_BUFFER;
    private final ToTypeConverter<DoubleBuffer> TO_DOUBLE_BUFFER;
    private final ToTypeConverter<byte[]> TO_BYTE_ARRAY;
    private final ToTypeConverter<char[]> TO_CHAR_ARRAY;
    private final ToTypeConverter<short[]> TO_SHORT_ARRAY;
    private final ToTypeConverter<int[]> TO_INT_ARRAY;
    private final ToTypeConverter<long[]> TO_LONG_ARRAY;
    private final ToTypeConverter<float[]> TO_FLOAT_ARRAY;
    private final ToTypeConverter<double[]> TO_DOUBLE_ARRAY;
    private final ToTypeConverter<String[]> TO_STRING_ARRAY;
    private final ToTypeConverter<boolean[]> TO_BOOL_ARRAY;
    private final ToTypeConverter<Duration> TO_DURATION;

    private Convert() {
        TO_INT = new ToIntConverter()
                .addConverter(Boolean.class, v -> v ? 1 : 0)
                .addConverter(Long.class, Long::intValue)
                .addConverter(Character.class, Character::charValue)
                .addConverter(Integer.class, Integer::intValue)
                .addConverter(Short.class, Short::intValue)
                .addConverter(Byte.class, Byte::intValue)
                .addConverter(Float.class, Math::round)
                .addConverter(Double.class, Double::intValue)
                .addConverter(String.class, Integer::parseInt);

        TO_BYTE = new ToIntConverter()
                .addConverter(Boolean.class, v -> v ? 1 : 0)
                .addConverter(Character.class, Character::charValue)
                .addConverter(Long.class, Long::byteValue)
                .addConverter(Integer.class, Integer::byteValue)
                .addConverter(Short.class, Short::byteValue)
                .addConverter(Byte.class, Byte::byteValue)
                .addConverter(Float.class, Float::byteValue)
                .addConverter(Double.class, Double::byteValue)
                .addConverter(String.class, Byte::parseByte);

        TO_SHORT = new ToIntConverter()
                .addConverter(Boolean.class, v -> v ? 1 : 0)
                .addConverter(Character.class, Character::charValue)
                .addConverter(Long.class, Long::shortValue)
                .addConverter(Integer.class, Integer::shortValue)
                .addConverter(Short.class, Short::shortValue)
                .addConverter(Byte.class, Byte::shortValue)
                .addConverter(Float.class, Float::shortValue)
                .addConverter(Double.class, Double::shortValue)
                .addConverter(String.class, Short::parseShort);

        TO_LONG = new ToLongConverter()
                .addConverter(Boolean.class, v -> v ? 1L : 0L)
                .addConverter(Character.class, Character::charValue)
                .addConverter(Long.class, Long::longValue)
                .addConverter(Integer.class, Integer::longValue)
                .addConverter(Short.class, Short::longValue)
                .addConverter(Byte.class, Byte::longValue)
                .addConverter(Float.class, Math::round)
                .addConverter(Double.class, Math::round)
                .addConverter(String.class, Long::parseLong)
                .addConverter(Date.class, Date::getTime)
                .addConverter(Instant.class, Instant::toEpochMilli)
                .addConverter(Duration.class, Duration::toMillis);

        TO_FLOAT = new ToDoubleConverter()
                .addConverter(Boolean.class, v -> v ? 1F : 0F)
                .addConverter(Character.class, Character::charValue)
                .addConverter(Long.class, Long::floatValue)
                .addConverter(Integer.class, Integer::floatValue)
                .addConverter(Short.class, Short::floatValue)
                .addConverter(Byte.class, Byte::floatValue)
                .addConverter(Float.class, Float::floatValue)
                .addConverter(Double.class, Double::floatValue)
                .addConverter(String.class, Float::parseFloat);

        TO_DOUBLE = new ToDoubleConverter()
                .addConverter(Character.class, Character::charValue)
                .addConverter(Boolean.class, v -> v ? 1D : 0D)
                .addConverter(Long.class, Long::doubleValue)
                .addConverter(Integer.class, Integer::doubleValue)
                .addConverter(Short.class, Short::doubleValue)
                .addConverter(Byte.class, Byte::doubleValue)
                .addConverter(Float.class, Float::doubleValue)
                .addConverter(Double.class, Double::doubleValue)
                .addConverter(String.class, Double::parseDouble)
                .addConverter(Date.class, Date::getTime)
                .addConverter(Instant.class, Instant::toEpochMilli)
                .addConverter(Duration.class, Duration::toMillis);

        TO_CHAR = new ToIntConverter()
                .addConverter(Character.class, Character::charValue)
                .addConverter(Long.class, Long::shortValue)
                .addConverter(Integer.class, Integer::shortValue)
                .addConverter(Short.class, Short::shortValue)
                .addConverter(Byte.class, Byte::shortValue)
                .addConverter(Float.class, Float::shortValue)
                .addConverter(Double.class, Double::shortValue)
                .addConverter(String.class, v -> v.isEmpty() ? '\0' : v.charAt(0))
                .addConverter(BigInteger.class, v -> v.toString().charAt(0))
                .addConverter(BigDecimal.class, v -> v.toString().charAt(0));

        TO_BIG_INTEGER = new ToTypeConverter<>(BigInteger.class)
                .addConverter(BigInteger.class, Function.identity())
                .<Character>addConverter(Character.class, BigInteger::valueOf)
                .addConverter(Long.class, BigInteger::valueOf)
                .<Integer>addConverter(Integer.class, BigInteger::valueOf)
                .<Short>addConverter(Short.class, BigInteger::valueOf)
                .<Byte>addConverter(Byte.class, BigInteger::valueOf)
                .addConverter(Float.class, v -> BigInteger.valueOf(v.longValue()))
                .addConverter(Double.class, v -> BigInteger.valueOf(v.longValue()))
                .addConverter(String.class, BigInteger::new)
                .addConverter(BigDecimal.class, BigDecimal::toBigInteger)
                .addConverter(Date.class, v -> BigInteger.valueOf(v.getTime()))
                .addConverter(Instant.class, v -> BigInteger.valueOf(v.toEpochMilli()))
                .addConverter(Duration.class, v -> BigInteger.valueOf(v.toMillis()))
                .addConverter(Number.class, v -> BigInteger.valueOf(v.longValue()));

        TO_BIG_DECIMAL = new ToTypeConverter<>(BigDecimal.class)
                .addConverter(BigDecimal.class, Function.identity())
                .addConverter(Character.class, BigDecimal::valueOf)
                .addConverter(Long.class, BigDecimal::valueOf)
                .addConverter(Integer.class, BigDecimal::valueOf)
                .addConverter(Short.class, BigDecimal::valueOf)
                .addConverter(Byte.class, BigDecimal::valueOf)
                .addConverter(Float.class, BigDecimal::valueOf)
                .addConverter(Double.class, BigDecimal::valueOf)
                .addConverter(String.class, BigDecimal::new)
                .addConverter(BigInteger.class, BigDecimal::new)
                .addConverter(Date.class, v -> BigDecimal.valueOf(v.getTime()))
                .addConverter(Instant.class, v -> BigDecimal.valueOf(v.toEpochMilli()))
                .addConverter(Duration.class, v -> BigDecimal.valueOf(v.toMillis()))
                .addConverter(Number.class, v -> BigDecimal.valueOf(v.doubleValue()));

        TO_BOOLEAN = new ToTypeConverter<>(Boolean.class)
                .addConverter(Boolean.class, Function.identity())
                .addConverter(Character.class, v -> v != '\0')
                .addConverter(Long.class, Convert::intToBoolean)
                .addConverter(Integer.class, Convert::intToBoolean)
                .addConverter(Short.class, Convert::intToBoolean)
                .addConverter(Byte.class, Convert::intToBoolean)
                .addConverter(Float.class, Convert::realToBoolean)
                .addConverter(Double.class, Convert::realToBoolean)
                .addConverter(String.class, Boolean::parseBoolean)
                .addConverter(BigInteger.class, v -> v.compareTo(BigInteger.ZERO) != 0)
                .addConverter(BigDecimal.class, v -> v.compareTo(BigDecimal.ZERO) != 0)
                .addConverter(Date.class, v -> v.getTime() > 0)
                .addConverter(Duration.class, v -> v.compareTo(Duration.ZERO) > 0);

        TO_DATE = new ToTypeConverter<>(Date.class)
                .addConverter(Date.class, Function.identity())
                .addConverter(Long.class, Date::new)
                .addConverter(Integer.class, Date::new)
                .addConverter(Short.class, Date::new)
                .addConverter(Byte.class, Date::new)
                .addConverter(String.class, v -> Date.from(Instant.parse(v)))
                .addConverter(Duration.class, v -> new Date(v.toMillis()))
                .addConverter(Instant.class, Date::from);

        TO_BYTE_BUFFER = new ToTypeConverter<>(ByteBuffer.class)
                .addConverter(ByteBuffer.class, Function.identity())
                .addConverter(ShortBuffer.class, Buffers::toByteBuffer)
                .addConverter(IntBuffer.class, Buffers::toByteBuffer)
                .addConverter(LongBuffer.class, Buffers::toByteBuffer)
                .addConverter(CharBuffer.class, Buffers::toByteBuffer)
                .addConverter(FloatBuffer.class, Buffers::toByteBuffer)
                .addConverter(DoubleBuffer.class, Buffers::toByteBuffer)
                .addConverter(Byte.class, Buffers::wrap);

        TO_CHAR_BUFFER = new ToTypeConverter<>(CharBuffer.class)
                .addConverter(CharBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asCharBuffer)
                .addConverter(Character.class, Buffers::wrap)
                .addConverter(String.class, CharBuffer::wrap)
                .addConverter(char[].class, CharBuffer::wrap);

        TO_SHORT_BUFFER = new ToTypeConverter<>(ShortBuffer.class)
                .addConverter(ShortBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asShortBuffer)
                .addConverter(Short.class, Buffers::wrap)
                .addConverter(Byte.class, b -> Buffers.wrap(b.shortValue()))
                .addConverter(short[].class, ShortBuffer::wrap);

        TO_INT_BUFFER = new ToTypeConverter<>(IntBuffer.class)
                .addConverter(IntBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asIntBuffer)
                .addConverter(Integer.class, Buffers::wrap)
                .addConverter(Short.class, s -> Buffers.wrap(s.intValue()))
                .addConverter(Byte.class, b -> Buffers.wrap(b.intValue()))
                .addConverter(Float.class, f -> Buffers.wrap(Float.floatToIntBits(f)))
                .addConverter(Character.class, c -> Buffers.wrap((int) c))
                .addConverter(int[].class, IntBuffer::wrap);

        TO_LONG_BUFFER = new ToTypeConverter<>(LongBuffer.class)
                .addConverter(LongBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asLongBuffer)
                .addConverter(Long.class, Buffers::wrap)
                .addConverter(Integer.class, i -> Buffers.wrap(i.longValue()))
                .addConverter(Short.class, s -> Buffers.wrap(s.longValue()))
                .addConverter(Byte.class, b -> Buffers.wrap(b.longValue()))
                .addConverter(Float.class, f -> Buffers.wrap((long) Float.floatToIntBits(f)))
                .addConverter(Double.class, d -> Buffers.wrap(Double.doubleToLongBits(d)))
                .addConverter(Character.class, c -> Buffers.wrap((long) c))
                .addConverter(long[].class, LongBuffer::wrap);

        TO_FLOAT_BUFFER = new ToTypeConverter<>(FloatBuffer.class)
                .addConverter(FloatBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asFloatBuffer)
                .addConverter(Integer.class, i -> Buffers.wrap(i.floatValue()))
                .addConverter(Short.class, s -> Buffers.wrap(s.floatValue()))
                .addConverter(Byte.class, b -> Buffers.wrap(b.floatValue()))
                .addConverter(Character.class, c -> Buffers.wrap((float) c))
                .addConverter(float[].class, FloatBuffer::wrap);

        TO_DOUBLE_BUFFER = new ToTypeConverter<>(DoubleBuffer.class)
                .addConverter(DoubleBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asDoubleBuffer)
                .addConverter(Long.class, l -> Buffers.wrap(l.doubleValue()))
                .addConverter(Integer.class, i -> Buffers.wrap(i.doubleValue()))
                .addConverter(Short.class, s -> Buffers.wrap(s.doubleValue()))
                .addConverter(Byte.class, b -> Buffers.wrap(b.doubleValue()))
                .addConverter(Character.class, c -> Buffers.wrap((double) c))
                .addConverter(double[].class, DoubleBuffer::wrap);

        TO_BYTE_ARRAY = new ToTypeConverter<>(byte[].class)
                .addConverter(byte[].class, Function.identity())
                .addConverter(Byte[].class, ArrayUtils::unwrapArray)
                .addConverter(short[].class, ArrayUtils::toByteArray)
                .addConverter(Short[].class, ArrayUtils::toByteArray)
                .addConverter(int[].class, ArrayUtils::toByteArray)
                .addConverter(Integer[].class, ArrayUtils::toByteArray)
                .addConverter(long[].class, ArrayUtils::toByteArray)
                .addConverter(Long[].class, ArrayUtils::toByteArray)
                .addConverter(float[].class, ArrayUtils::toByteArray)
                .addConverter(Float[].class, ArrayUtils::toByteArray)
                .addConverter(double[].class, ArrayUtils::toByteArray)
                .addConverter(Double[].class, ArrayUtils::toByteArray)
                .addConverter(char[].class, ArrayUtils::toByteArray)
                .addConverter(Character[].class, ArrayUtils::toByteArray)
                .addConverter(String.class, String::getBytes)
                .addConverter(Byte.class, b -> new byte[]{b})
                .addConverter(Short.class, Shorts::toByteArray)
                .addConverter(Integer.class, Ints::toByteArray)
                .addConverter(Long.class, Longs::toByteArray)
                .addConverter(Float.class, f -> Ints.toByteArray(Float.floatToIntBits(f)))
                .addConverter(Double.class, d -> Longs.toByteArray(Double.doubleToLongBits(d)))
                .addConverter(Date.class, d -> Longs.toByteArray(d.getTime()))
                .addConverter(Duration.class, d -> Longs.toByteArray(d.toMillis()))
                .addConverter(Instant.class, i -> Longs.toByteArray(i.toEpochMilli()))
                .addConverter(Character.class, Chars::toByteArray)
                .addConverter(BigInteger.class, BigInteger::toByteArray)
                .addConverter(ByteBuffer.class, Buffers::readRemaining);

        TO_CHAR_ARRAY = new ToTypeConverter<>(char[].class)
                .addConverter(Character.class, c -> new char[]{c})
                .addConverter(char[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toCharArray)
                .addConverter(Character[].class, ArrayUtils::unwrapArray)
                .addConverter(CharBuffer.class, Buffers::readRemaining)
                .addConverter(String.class, String::toCharArray);

        TO_SHORT_ARRAY = new ToTypeConverter<>(short[].class)
                .addConverter(Short.class, c -> new short[]{c})
                .addConverter(Byte.class, b -> new short[]{b})
                .addConverter(short[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toShortArray)
                .addConverter(Short[].class, ArrayUtils::unwrapArray)
                .addConverter(ShortBuffer.class, Buffers::readRemaining);

        TO_INT_ARRAY = new ToTypeConverter<>(int[].class)
                .addConverter(Integer.class, i -> new int[]{i})
                .addConverter(Short.class, c -> new int[]{c})
                .addConverter(Byte.class, b -> new int[]{b})
                .addConverter(Character.class, c -> new int[]{c})
                .addConverter(int[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toIntArray)
                .addConverter(Integer[].class, ArrayUtils::unwrapArray)
                .addConverter(IntBuffer.class, Buffers::readRemaining);

        TO_LONG_ARRAY = new ToTypeConverter<>(long[].class)
                .addConverter(Long.class, l -> new long[]{l})
                .addConverter(Integer.class, i -> new long[]{i})
                .addConverter(Short.class, c -> new long[]{c})
                .addConverter(Byte.class, b -> new long[]{b})
                .addConverter(Character.class, c -> new long[]{c})
                .addConverter(long[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toLongArray)
                .addConverter(Long[].class, ArrayUtils::unwrapArray)
                .addConverter(LongBuffer.class, Buffers::readRemaining);

        TO_FLOAT_ARRAY = new ToTypeConverter<>(float[].class)
                .addConverter(Float.class, f -> new float[]{f})
                .addConverter(Integer.class, i -> new float[]{i})
                .addConverter(Short.class, c -> new float[]{c})
                .addConverter(Byte.class, b -> new float[]{b})
                .addConverter(Character.class, c -> new float[]{c})
                .addConverter(float[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toFloatArray)
                .addConverter(Float[].class, ArrayUtils::unwrapArray)
                .addConverter(FloatBuffer.class, Buffers::readRemaining);

        TO_DOUBLE_ARRAY = new ToTypeConverter<>(double[].class)
                .addConverter(Float.class, f -> new double[]{f})
                .addConverter(Double.class, d -> new double[]{d})
                .addConverter(Long.class, l -> new double[]{l})
                .addConverter(Integer.class, i -> new double[]{i})
                .addConverter(Short.class, c -> new double[]{c})
                .addConverter(Byte.class, b -> new double[]{b})
                .addConverter(Character.class, c -> new double[]{c})
                .addConverter(double[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toDoubleArray)
                .addConverter(Double[].class, ArrayUtils::unwrapArray)
                .addConverter(DoubleBuffer.class, Buffers::readRemaining);

        TO_STRING_ARRAY = new ToTypeConverter<>(String[].class)
                .addConverter(String[].class, Function.identity())
                .addConverter(byte[].class, array -> ArrayUtils.transformByteArray(array, String.class, Integer::toString))
                .addConverter(Byte[].class, array -> ArrayUtils.transform(array, String.class, Integer::toString))
                .addConverter(short[].class, array -> ArrayUtils.transformShortArray(array, String.class, Integer::toString))
                .addConverter(Short[].class, array -> ArrayUtils.transform(array, String.class, Integer::toString))
                .addConverter(int[].class, array -> ArrayUtils.transformIntArray(array, String.class, Integer::toString))
                .addConverter(Integer[].class, array -> ArrayUtils.transform(array, String.class, Number::toString))
                .addConverter(long[].class, array -> ArrayUtils.transformLongArray(array, String.class, Long::toString))
                .addConverter(Long[].class, array -> ArrayUtils.transform(array, String.class, Number::toString))
                .addConverter(boolean[].class, array -> ArrayUtils.transformBooleanArray(array, String.class, Object::toString))
                .addConverter(Boolean[].class, array -> ArrayUtils.transform(array, String.class, Object::toString))
                .addConverter(float[].class, array -> ArrayUtils.transformFloatArray(array, String.class, Double::toString))
                .addConverter(Float[].class, array -> ArrayUtils.transform(array, String.class, Double::toString))
                .addConverter(double[].class, array -> ArrayUtils.transformDoubleArray(array, String.class, Double::toString))
                .addConverter(Double[].class, array -> ArrayUtils.transform(array, String.class, Number::toString))
                .addConverter(char[].class, array -> new String[]{new String(array)})
                .addConverter(Character[].class, array -> new String[]{new String(ArrayUtils.unwrapArray(array))})
                .addConverter(BigInteger[].class, array -> ArrayUtils.transform(array, String.class, BigInteger::toString))
                .addConverter(BigDecimal[].class, array -> ArrayUtils.transform(array, String.class, BigDecimal::toString))
                .addConverter(Date[].class, array -> ArrayUtils.transform(array, String.class, v -> Long.toString(v.getTime())))
                .addConverter(Duration[].class, array -> ArrayUtils.transform(array, String.class, Duration::toString))
                .addConverter(Instant[].class, array -> ArrayUtils.transform(array, String.class, Instant::toString))
                .addConverter(Object[].class, array -> ArrayUtils.transform(array, String.class, Objects::toString))
                .addConverter(Object.class, v -> new String[]{v.toString()});

        TO_BOOL_ARRAY = new ToTypeConverter<>(boolean[].class)
                .addConverter(Boolean.class, b -> new boolean[]{b})
                .addConverter(Character.class, v -> new boolean[]{v != '\0'})
                .addConverter(Long.class, v -> new boolean[]{intToBoolean(v)})
                .addConverter(Integer.class, v -> new boolean[]{intToBoolean(v)})
                .addConverter(Short.class, v -> new boolean[]{intToBoolean(v)})
                .addConverter(Byte.class, v -> new boolean[]{intToBoolean(v)})
                .addConverter(Float.class, v -> new boolean[]{realToBoolean(v)})
                .addConverter(Double.class, v -> new boolean[]{realToBoolean(v)})
                .addConverter(String.class, v -> new boolean[]{Boolean.parseBoolean(v)})
                .addConverter(BigInteger.class, v -> ArrayUtils.toBoolArray(v.toByteArray()))
                .addConverter(Date.class, v -> new boolean[]{v.getTime() > 0})
                .addConverter(Duration.class, v -> new boolean[]{v.compareTo(Duration.ZERO) > 0})
                .addConverter(boolean[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toBoolArray)
                .addConverter(Boolean[].class, ArrayUtils::unwrapArray);

        TO_DURATION = new ToTypeConverter<>(Duration.class)
                .addConverter(Duration.class, Function.identity())
                .addConverter(Number.class, n -> Duration.ofNanos(n.longValue()))
                .addConverter(String.class, Duration::parse)
                .addConverter(StringBuffer.class, Duration::parse)
                .addConverter(StringBuilder.class, Duration::parse)
                .addConverter(char[].class, chars -> Duration.parse(new String(chars)))
                .addConverter(CharBuffer.class, Duration::parse);
    }

    private static Convert getInstance() {
        return INSTANCE.lazyGet(Convert::new);
    }

    private static boolean intToBoolean(final Number value){
        return value.longValue() != 0;
    }

    private static boolean realToBoolean(final Number value){
        return !(Double.isNaN(value.doubleValue()) || Double.isInfinite(value.doubleValue()));
    }

    @SuppressWarnings("unchecked")
    private static <T> T unsafeCast(final Object obj){
        return (T) obj;
    }

    /**
     * Determines whether the specified object is an instance of the type described by token.
     * @param value An object to test.
     * @param target Token that describes a type.
     * @return {@literal true}, if the specified object is an instance of the type described by token; otherwise, {@literal false}.
     */
    public static boolean isInstance(final Object value, final TypeToken<?> target) {
        return value != null && target.isSupertypeOf(value.getClass());
    }

    public static <T> Optional<T> toType(final Object value, final TypeToken<T> target) {
        return isInstance(value, target) ? Optional.of(unsafeCast(value)) : Optional.empty();
    }

    /**
     * Casts value to the specified JMX Open Type.
     * @param <T> Type of the conversion result.
     * @param value Value to cast.
     * @param type JMX Open Type. Cannot be {@literal null}.
     * @return Converter value.
     */
    public static <T> Optional<T> toType(final Object value, final OpenType<T> type){
        return type.isValue(value) ? Optional.of(unsafeCast(value)) : Optional.empty();
    }

    public static <T> Optional<T> toType(final Object obj,
                                         @Nonnull final Class<T> expectedType) {
        return expectedType.isInstance(obj) ? Optional.of(unsafeCast(obj)) : Optional.empty();
    }

    public static <I, T> Function<? super I, Optional<T>> toType(final Class<T> expectedType){
        return input -> toType(input, expectedType);
    }

    public static OptionalInt toInt(final Object value){
        return getInstance().TO_INT.convert(value);
    }

    public static byte toByte(final Object value, final byte defval){
        return (byte) getInstance().TO_BYTE.convert(value).orElse(defval);
    }

    public static <E extends Throwable> byte toByte(final Object value, final Supplier<? extends E> exceptionFactory) throws E{
        return (byte) getInstance().TO_BYTE.convert(value).orElseThrow(exceptionFactory);
    }

    public static short toShort(final Object value, final short defval){
        return (short) getInstance().TO_SHORT.convert(value).orElse(defval);
    }

    public static <E extends Throwable> short toShort(final Object value, final Supplier<? extends E> exceptionFactory) throws E{
        return (short) getInstance().TO_SHORT.convert(value).orElseThrow(exceptionFactory);
    }

    public static OptionalLong toLong(final Object value){
        return getInstance().TO_LONG.convert(value);
    }

    public static OptionalDouble toDouble(final Object value){
        return getInstance().TO_DOUBLE.convert(value);
    }

    public static float toFloat(final Object value, final short defval){
        return (float) getInstance().TO_FLOAT.convert(value).orElse(defval);
    }

    public static <E extends Throwable> float toFloat(final Object value, final Supplier<? extends E> exceptionFactory) throws E{
        return (float) getInstance().TO_FLOAT.convert(value).orElseThrow(exceptionFactory);
    }

    public static Optional<BigInteger> toBigInteger(final Object value){
        return getInstance().TO_BIG_INTEGER.convert(value);
    }

    public static Optional<BigDecimal> toBigDecimal(final Object value) {
        return getInstance().TO_BIG_DECIMAL.convert(value);
    }

    public static Optional<Boolean> toBoolean(final Object value){
        return getInstance().TO_BOOLEAN.convert(value);
    }

    public static char toChar(final Object value, final char defval){
        return (char) getInstance().TO_CHAR.convert(value).orElse(defval);
    }

    public static <E extends Throwable> char toChar(final Object value, final Supplier<? extends E> exceptionFactory) throws E{
        return (char) getInstance().TO_CHAR.convert(value).orElseThrow(exceptionFactory);
    }

    public static Optional<Date> toDate(final Object value) {
        return getInstance().TO_DATE.convert(value);
    }

    public static Optional<byte[]> toByteArray(final Object value){
        return getInstance().TO_BYTE_ARRAY.convert(value);
    }

    public static Optional<ByteBuffer> toByteBuffer(final Object value){
        return getInstance().TO_BYTE_BUFFER.convert(value, v -> toByteArray(v).map(ByteBuffer::wrap));
    }

    public static Optional<CharBuffer> toCharBuffer(final Object value){
        return getInstance().TO_CHAR_BUFFER.convert(value, v -> toByteBuffer(v).map(ByteBuffer::asCharBuffer));
    }

    public static Optional<ShortBuffer> toShortBuffer(final Object value) {
        return getInstance().TO_SHORT_BUFFER.convert(value, v -> toByteBuffer(v).map(ByteBuffer::asShortBuffer));
    }

    public static Optional<IntBuffer> toIntBuffer(final Object value){
        return getInstance().TO_INT_BUFFER.convert(value, v -> toByteBuffer(v).map(ByteBuffer::asIntBuffer));
    }

    public static Optional<LongBuffer> toLongBuffer(final Object value){
        return getInstance().TO_LONG_BUFFER.convert(value, v -> toByteBuffer(v).map(ByteBuffer::asLongBuffer));
    }

    public static Optional<FloatBuffer> toFloatBuffer(final Object value){
        return getInstance().TO_FLOAT_BUFFER.convert(value, v -> toByteBuffer(v).map(ByteBuffer::asFloatBuffer));
    }

    public static Optional<DoubleBuffer> toDoubleBuffer(final Object value){
        return getInstance().TO_DOUBLE_BUFFER.convert(value, v -> toByteBuffer(v).map(ByteBuffer::asDoubleBuffer));
    }

    public static Optional<char[]> toCharArray(final Object value){
        return getInstance().TO_CHAR_ARRAY.convert(value, v -> toByteArray(v).map(ArrayUtils::toCharArray));
    }

    public static Optional<short[]> toShortArray(final Object value){
        return getInstance().TO_SHORT_ARRAY.convert(value, v -> toByteArray(v).map(ArrayUtils::toShortArray));
    }

    public static Optional<int[]> toIntArray(final Object value){
        return getInstance().TO_INT_ARRAY.convert(value, v -> toByteArray(v).map(ArrayUtils::toIntArray));
    }

    public static Optional<long[]> toLongArray(final Object value){
        return getInstance().TO_LONG_ARRAY.convert(value, v -> toByteArray(v).map(ArrayUtils::toLongArray));
    }

    public static Optional<float[]> toFloatArray(final Object value){
        return getInstance().TO_FLOAT_ARRAY.convert(value, v -> toByteArray(v).map(ArrayUtils::toFloatArray));
    }

    public static Optional<double[]> toDoubleArray(final Object value){
        return getInstance().TO_DOUBLE_ARRAY.convert(value, v -> toByteArray(v).map(ArrayUtils::toDoubleArray));
    }

    public static Optional<String[]> toStringArray(final Object value) {
        return getInstance().TO_STRING_ARRAY.convert(value);
    }

    public static Optional<boolean[]> toBooleanArray(final Object value){
        return getInstance().TO_BOOL_ARRAY.convert(value, v -> toByteArray(v).map(ArrayUtils::toBoolArray));
    }

    public static Optional<Duration> toDuration(final Object value){
        return getInstance().TO_DURATION.convert(value);
    }

    public static <T> Optional<T[]> toArray(final Object value, final Class<T[]> arrayType,
                                            final Function<Object, Optional<T>> converter) {
        Optional<T[]> resultArray = toType(value, arrayType);
        if (resultArray.isPresent())
            return resultArray;
        else if (value instanceof Object[]) {
            final Object[] array = (Object[]) value;
            final Object[] result = ObjectArrays.newArray(arrayType.getComponentType(), array.length);
            for (int i = 0; i < result.length; i++) {
                final Optional<T> item = converter.apply(array[i]);
                if (item.isPresent())
                    result[i] = item.get();
                else
                    return Optional.empty();
            }
            return toType(result, arrayType);
        } else
            return converter.apply(value).flatMap(i -> {
                final Object[] result = ObjectArrays.newArray(arrayType.getComponentType(), 1);
                result[0] = i;
                return toType(result, arrayType);
            });
    }
}
