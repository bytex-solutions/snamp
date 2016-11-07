package com.bytex.snamp;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.io.Buffers;
import com.google.common.primitives.*;
import com.google.common.reflect.TypeToken;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Converts a base data type to another base data type.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Convert {
    private static abstract class TypeConverter<C> extends ConcurrentHashMap<Class<?>, C> {
        private static final long serialVersionUID = -2745877310143387409L;

        private Optional<C> getConverter(final Object value) {
            for (Class<?> key = value.getClass(), lookup = key; lookup != null; lookup = lookup.getSuperclass()){
                final C converter = get(lookup);
                if(converter != null)
                    return Optional.of(firstNonNull(putIfAbsent(key, converter), converter));//cache converter for the origin key, not for current inheritance frame
            }
            return Optional.empty();
        }

        private <E extends Throwable> C getConverter(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E{
            final Optional<C> converter = getConverter(value);
            if(converter.isPresent())
                return converter.get();
            else
                throw exceptionFactory.apply(value);
        }
    }

    private static final class ToIntConverter extends TypeConverter<ToIntFunction>{
        private static final long serialVersionUID = -7571891959758237238L;

        private <T> ToIntConverter addConverter(final Class<T> type, final ToIntFunction<? super T> converter){
            put(type, converter);
            return this;
        }

        @SuppressWarnings("unchecked")
        private <E extends Throwable> int convert(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
            return super.getConverter(value, exceptionFactory).applyAsInt(value);
        }
    }

    private static final class ToLongConverter extends TypeConverter<ToLongFunction>{
        private static final long serialVersionUID = 1973865787305278420L;

        private <T> ToLongConverter addConverter(final Class<T> type, final ToLongFunction<? super T> converter){
            put(type, converter);
            return this;
        }

        @SuppressWarnings("unchecked")
        private <E extends Throwable> long convert(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
            return super.getConverter(value, exceptionFactory).applyAsLong(value);
        }
    }

    private static final class ToDoubleConverter extends TypeConverter<ToDoubleFunction>{
        private static final long serialVersionUID = 7425665427678455939L;

        private <T> ToDoubleConverter addConverter(final Class<T> type, final ToDoubleFunction<? super T> converter){
            put(type, converter);
            return this;
        }

        @SuppressWarnings("unchecked")
        private <E extends Throwable> double convert(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
            return super.getConverter(value, exceptionFactory).applyAsDouble(value);
        }
    }

    private static final class ToTypeConverter<O> extends TypeConverter<Function> {
        private static final long serialVersionUID = 3279547883268029767L;

        private <I> ToTypeConverter<O> addConverter(final Class<I> type, final Function<? super I, ? extends O> converter) {
            put(type, converter);
            return this;
        }

        @SuppressWarnings("unchecked")
        private <E extends Throwable> O convert(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
            return (O) super.getConverter(value, exceptionFactory).apply(value);
        }

        @SuppressWarnings("unchecked")
        private <E extends Throwable, F> O convert(final Object value,
                                                   final Function<Object, ? extends E> exceptionFactory,
                                                   final ToTypeConverter<F> fallback,
                                                   final Function<? super F, ? extends O> fallbackConverter) throws E{
            final Optional<Function> converter = super.getConverter(value);
            return converter.isPresent() ? (O) converter.get().apply(value) : fallbackConverter.apply(fallback.convert(value, exceptionFactory));
        }
    }

    private static final LazySoftReference<ToIntConverter> TO_INT = new LazySoftReference<>();
    private static final LazySoftReference<ToIntConverter> TO_BYTE = new LazySoftReference<>();
    private static final LazySoftReference<ToIntConverter> TO_SHORT = new LazySoftReference<>();
    private static final LazySoftReference<ToLongConverter> TO_LONG = new LazySoftReference<>();
    private static final LazySoftReference<ToDoubleConverter> TO_FLOAT = new LazySoftReference<>();
    private static final LazySoftReference<ToDoubleConverter> TO_DOUBLE = new LazySoftReference<>();
    private static final LazySoftReference<ToIntConverter> TO_CHAR = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<BigInteger>> TO_BIG_INTEGER = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<BigDecimal>> TO_BIG_DECIMAL = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<Boolean>> TO_BOOLEAN = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<Date>> TO_DATE = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<ByteBuffer>> TO_BYTE_BUFFER = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<CharBuffer>> TO_CHAR_BUFFER = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<ShortBuffer>> TO_SHORT_BUFFER = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<IntBuffer>> TO_INT_BUFFER = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<LongBuffer>> TO_LONG_BUFFER = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<FloatBuffer>> TO_FLOAT_BUFFER = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<DoubleBuffer>> TO_DOUBLE_BUFFER = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<byte[]>> TO_BYTE_ARRAY = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<char[]>> TO_CHAR_ARRAY = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<short[]>> TO_SHORT_ARRAY = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<int[]>> TO_INT_ARRAY = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<long[]>> TO_LONG_ARRAY = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<float[]>> TO_FLOAT_ARRAY = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<double[]>> TO_DOUBLE_ARRAY = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<String[]>> TO_STRING_ARRAY = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<boolean[]>> TO_BOOL_ARRAY = new LazySoftReference<>();
    private static final LazySoftReference<ToTypeConverter<Duration>> TO_DURATION = new LazySoftReference<>();

    private static final class TypeTokenCastException extends ClassCastException{
        private static final long serialVersionUID = -1754975745564795992L;

        private TypeTokenCastException(final Object value, final TypeToken<?> target){
            super(String.format("Unable to cast %s to %s", value, target));
        }
    }

    private static boolean intToBoolean(final Number value){
        return value.longValue() != 0;
    }

    private static boolean realToBoolean(final Number value){
        return !(Double.isNaN(value.doubleValue()) || Double.isInfinite(value.doubleValue()));
    }

    private Convert(){
        throw new InstantiationError();
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

    @SuppressWarnings("unchecked")
    public static <T> T toTypeToken(final Object value, final TypeToken<T> target) throws ClassCastException {
        if (isInstance(value, target))
            return (T) value;
        else throw new TypeTokenCastException(value, target);
    }

    /**
     * Casts value to the specified JMX Open Type.
     * @param <T> Type of the conversion result.
     * @param value Value to cast.
     * @param type JMX Open Type. Cannot be {@literal null}.
     * @return Converter value.
     * @throws OpenDataException Unable to cast value to the specified JMX Open Type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T toOpenType(final Object value, final OpenType<T> type) throws OpenDataException{
        if(type.isValue(value))
            return (T)value;
        else throw new OpenDataException(String.format("Unable cast %s to %s", value, type));
    }

    public static <C, I, O> O toType(final C input,
                                     final Class<I> expectedType,
                                     final Function<? super I, ? extends O> then,
                                     final Function<? super C, ? extends O> fallback){
        return expectedType.isInstance(input) ? then.apply(expectedType.cast(input)) : fallback.apply(input);
    }

    public static <C, I, O> O toType(final C input,
                                     final Class<I> expectedType,
                                     final Function<? super I, ? extends O> then){
        return expectedType.isInstance(input) ? then.apply(expectedType.cast(input)) : null;
    }

    public static <C, I> int toInt(final C input,
                                   final Class<I> expectedType,
                                   final ToIntFunction<? super I> then,
                                   final ToIntFunction<? super C> fallback){
        return expectedType.isInstance(input) ? then.applyAsInt(expectedType.cast(input)) : fallback.applyAsInt(input);
    }

    public static <E extends Throwable> int toInt(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToIntConverter> CONVERTER = () -> new ToIntConverter()
                .addConverter(Boolean.class, v -> v ? 1 : 0)
                .addConverter(Long.class, Long::intValue)
                .addConverter(Character.class, Character::charValue)
                .addConverter(Integer.class, Integer::intValue)
                .addConverter(Short.class, Short::intValue)
                .addConverter(Byte.class, Byte::intValue)
                .addConverter(Float.class, Math::round)
                .addConverter(Double.class, Double::intValue)
                .addConverter(String.class, Integer::parseInt);

        return TO_INT.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static int toInt(final Object value) {
        return toInt(value, v -> new ClassCastException(String.format("Unable to convert '%s' to integer", v)));
    }

    public static <E extends Throwable> byte toByte(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E{
        final Supplier<ToIntConverter> CONVERTER = () -> new ToIntConverter()
                .addConverter(Boolean.class, v -> v ? 1 : 0)
                .addConverter(Character.class, Character::charValue)
                .addConverter(Long.class, Long::byteValue)
                .addConverter(Integer.class, Integer::byteValue)
                .addConverter(Short.class, Short::byteValue)
                .addConverter(Byte.class, Byte::byteValue)
                .addConverter(Float.class, Float::byteValue)
                .addConverter(Double.class, Double::byteValue)
                .addConverter(String.class, Byte::parseByte);

        return (byte) TO_BYTE.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static byte toByte(final Object value){
        return toByte(value, v -> new ClassCastException(String.format("Unable to convert '%s' to byte", v)));
    }

    public static <E extends Throwable> short toShort(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E{
        final Supplier<ToIntConverter> CONVERTER = () -> new ToIntConverter()
                .addConverter(Boolean.class, v -> v ? 1 : 0)
                .addConverter(Character.class, Character::charValue)
                .addConverter(Long.class, Long::shortValue)
                .addConverter(Integer.class, Integer::shortValue)
                .addConverter(Short.class, Short::shortValue)
                .addConverter(Byte.class, Byte::shortValue)
                .addConverter(Float.class, Float::shortValue)
                .addConverter(Double.class, Double::shortValue)
                .addConverter(String.class, Short::parseShort);

        return (short) TO_SHORT.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static short toShort(final Object value){
        return toShort(value, v -> new ClassCastException(String.format("Unable to convert '%s' to short", v)));
    }

    public static <C, I> long toLong(final C input,
                                     final Class<I> expectedType,
                                     final ToLongFunction<? super I> then,
                                     final ToLongFunction<? super C> fallback){
        return expectedType.isInstance(input) ? then.applyAsLong(expectedType.cast(input)) : fallback.applyAsLong(input);
    }

    public static <E extends Throwable> long toLong(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E{
        final Supplier<ToLongConverter> CONVERTER = () -> new ToLongConverter()
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

        return TO_LONG.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static long toLong(final Object value){
        return toLong(value, v -> new ClassCastException(String.format("Unable to convert '%s' to long", v)));
    }

    public static <C, I> double toDouble(final C input,
                                         final Class<I> expectedType,
                                         final ToDoubleFunction<? super I> then,
                                         final ToDoubleFunction<? super C> fallback){
        return expectedType.isInstance(input) ? then.applyAsDouble(expectedType.cast(input)) : fallback.applyAsDouble(input);
    }

    public static <E extends Throwable> double toDouble(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E{
        final Supplier<ToDoubleConverter> CONVERTER = () -> new ToDoubleConverter()
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

        return TO_DOUBLE.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static double toDouble(final Object value){
        return toDouble(value, v -> new ClassCastException(String.format("Unable to convert '%s' to double", v)));
    }

    public static <E extends Throwable> float toFloat(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E{
        final Supplier<ToDoubleConverter> CONVERTER = () -> new ToDoubleConverter()
                .addConverter(Boolean.class, v -> v ? 1F : 0F)
                .addConverter(Character.class, Character::charValue)
                .addConverter(Long.class, Long::floatValue)
                .addConverter(Integer.class, Integer::floatValue)
                .addConverter(Short.class, Short::floatValue)
                .addConverter(Byte.class, Byte::floatValue)
                .addConverter(Float.class, Float::floatValue)
                .addConverter(Double.class, Double::floatValue)
                .addConverter(String.class, Float::parseFloat);

        return (float) TO_FLOAT.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static float toFloat(final Object value){
        return toFloat(value, v -> new ClassCastException(String.format("Unable to convert '%s' to float", v)));
    }

    public static <E extends Throwable> BigInteger toBigInteger(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E{
        final Supplier<ToTypeConverter<BigInteger>> CONVERTER = () -> new ToTypeConverter<BigInteger>()
                .addConverter(BigInteger.class, Function.identity())
                .<Character>addConverter(Character.class,  BigInteger::valueOf)
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
                .addConverter(Duration.class, v -> BigInteger.valueOf(v.toMillis()));

        return TO_BIG_INTEGER.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static BigInteger toBigInteger(final Object value){
        return toBigInteger(value, v -> new ClassCastException(String.format("Unable to convert '%s' to BigInteger", v)));
    }

    public static <E extends Throwable> BigDecimal toBigDecimal(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<BigDecimal>> CONVERTER = () -> new ToTypeConverter<BigDecimal>()
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
                .addConverter(Duration.class, v -> BigDecimal.valueOf(v.toMillis()));

        return TO_BIG_DECIMAL.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static BigDecimal toBigDecimal(final Object value) {
        return toBigDecimal(value, v -> new ClassCastException(String.format("Unable to convert '%s' to BigDecimal", v)));
    }

    public static <E extends Throwable> boolean toBoolean(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E{
        final Supplier<ToTypeConverter<Boolean>> CONVERTER = () -> new ToTypeConverter<Boolean>()
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

        return TO_BOOLEAN.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static boolean toBoolean(final Object value){
        return toBoolean(value, v -> new ClassCastException(String.format("Unable to convert '%s' to boolean", v)));
    }

    public static <E extends Throwable> char toChar(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToIntConverter> CONVERTER = () -> new ToIntConverter()
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

        return (char) TO_CHAR.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static char toChar(final Object value) {
        return toChar(value, v -> new ClassCastException(String.format("Unable to convert '%s' to character", v)));
    }

    public static <E extends Throwable> Date toDate(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<Date>> CONVERTER = () -> new ToTypeConverter<Date>()
                .addConverter(Date.class, Function.identity())
                .addConverter(Long.class, Date::new)
                .addConverter(Integer.class, Date::new)
                .addConverter(Short.class, Date::new)
                .addConverter(Byte.class, Date::new)
                .addConverter(String.class, v -> Date.from(Instant.parse(v)))
                .addConverter(Duration.class, v -> new Date(v.toMillis()))
                .addConverter(Instant.class, Date::from);

        return TO_DATE.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static Date toDate(final Object value) {
        return toDate(value, v -> new ClassCastException(String.format("Unable to convert '%s' to Date", v)));
    }

    private static ToTypeConverter<byte[]> getByteArrayConverter(){
        final Supplier<ToTypeConverter<byte[]>> CONVERTER = () -> new ToTypeConverter<byte[]>()
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
                .addConverter(Byte.class, b -> new byte[]{ b })
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

        return TO_BYTE_ARRAY.lazyGet(CONVERTER);
    }

    public static <E extends Throwable> ByteBuffer toByteBuffer(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<ByteBuffer>> CONVERTER = () -> new ToTypeConverter<ByteBuffer>()
                .addConverter(ByteBuffer.class, Function.identity())
                .addConverter(ShortBuffer.class, Buffers::toByteBuffer)
                .addConverter(IntBuffer.class, Buffers::toByteBuffer)
                .addConverter(LongBuffer.class, Buffers::toByteBuffer)
                .addConverter(CharBuffer.class, Buffers::toByteBuffer)
                .addConverter(FloatBuffer.class, Buffers::toByteBuffer)
                .addConverter(DoubleBuffer.class, Buffers::toByteBuffer)
                .addConverter(Byte.class, Buffers::wrap);

        return TO_BYTE_BUFFER.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), ByteBuffer::wrap);
    }

    public static ByteBuffer toByteBuffer(final Object value){
        return toByteBuffer(value, v -> new ClassCastException(String.format("Unable to convert '%s' to ByteBuffer", v)));
    }

    public static <E extends Throwable> CharBuffer toCharBuffer(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<CharBuffer>> CONVERTER = () -> new ToTypeConverter<CharBuffer>()
                .addConverter(CharBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asCharBuffer)
                .addConverter(Character.class, Buffers::wrap)
                .addConverter(String.class, CharBuffer::wrap)
                .addConverter(char[].class, CharBuffer::wrap)
                .addConverter(Object.class, v -> toByteBuffer(v).asCharBuffer());

        return TO_CHAR_BUFFER.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), bytes -> ByteBuffer.wrap(bytes).asCharBuffer());
    }

    public static CharBuffer toCharBuffer(final Object value){
        return toCharBuffer(value, v -> new ClassCastException(String.format("Unable to convert '%s' to CharBuffer", v)));
    }

    public static <E extends Throwable> ShortBuffer toShortBuffer(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<ShortBuffer>> CONVERTER = () -> new ToTypeConverter<ShortBuffer>()
                .addConverter(ShortBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asShortBuffer)
                .addConverter(Short.class, Buffers::wrap)
                .addConverter(Byte.class, b -> Buffers.wrap(b.shortValue()))
                .addConverter(short[].class, ShortBuffer::wrap);

        return TO_SHORT_BUFFER.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), bytes -> ByteBuffer.wrap(bytes).asShortBuffer());
    }

    public static ShortBuffer toShortBuffer(final Object value){
        return toShortBuffer(value, v -> new ClassCastException(String.format("Unable to convert '%s' to ShortBuffer", v)));
    }

    public static <E extends Throwable> IntBuffer toIntBuffer(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<IntBuffer>> CONVERTER = () -> new ToTypeConverter<IntBuffer>()
                .addConverter(IntBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asIntBuffer)
                .addConverter(Integer.class, Buffers::wrap)
                .addConverter(Short.class, s -> Buffers.wrap(s.intValue()))
                .addConverter(Byte.class, b -> Buffers.wrap(b.intValue()))
                .addConverter(Float.class, f -> Buffers.wrap(Float.floatToIntBits(f)))
                .addConverter(Character.class, c -> Buffers.wrap((int) c))
                .addConverter(int[].class, IntBuffer::wrap);

        return TO_INT_BUFFER.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), bytes -> ByteBuffer.wrap(bytes).asIntBuffer());
    }

    public static IntBuffer toIntBuffer(final Object value){
        return toIntBuffer(value, v -> new ClassCastException(String.format("Unable to convert '%s' to IntBuffer", v)));
    }

    public static <E extends Throwable> LongBuffer toLongBuffer(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<LongBuffer>> CONVERTER = () -> new ToTypeConverter<LongBuffer>()
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

        return TO_LONG_BUFFER.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), bytes -> ByteBuffer.wrap(bytes).asLongBuffer());
    }

    public static LongBuffer toLongBuffer(final Object value){
        return toLongBuffer(value, v -> new ClassCastException(String.format("Unable to convert '%s' to LongBuffer", v)));
    }

    public static <E extends Throwable> FloatBuffer toFloatBuffer(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<FloatBuffer>> CONVERTER = () -> new ToTypeConverter<FloatBuffer>()
                .addConverter(FloatBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asFloatBuffer)
                .addConverter(Integer.class, i -> Buffers.wrap(i.floatValue()))
                .addConverter(Short.class, s -> Buffers.wrap(s.floatValue()))
                .addConverter(Byte.class, b -> Buffers.wrap(b.floatValue()))
                .addConverter(Character.class, c -> Buffers.wrap((float) c))
                .addConverter(float[].class, FloatBuffer::wrap);

        return TO_FLOAT_BUFFER.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), bytes -> ByteBuffer.wrap(bytes).asFloatBuffer());
    }

    public static FloatBuffer toFloatBuffer(final Object value){
        return toFloatBuffer(value, v -> new ClassCastException(String.format("Unable to convert '%s' to FloatBuffer", v)));
    }

    public static <E extends Throwable> DoubleBuffer toDoubleBuffer(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<DoubleBuffer>> CONVERTER = () -> new ToTypeConverter<DoubleBuffer>()
                .addConverter(DoubleBuffer.class, Function.identity())
                .addConverter(ByteBuffer.class, ByteBuffer::asDoubleBuffer)
                .addConverter(Long.class, l -> Buffers.wrap(l.doubleValue()))
                .addConverter(Integer.class, i -> Buffers.wrap(i.doubleValue()))
                .addConverter(Short.class, s -> Buffers.wrap(s.doubleValue()))
                .addConverter(Byte.class, b -> Buffers.wrap(b.doubleValue()))
                .addConverter(Character.class, c -> Buffers.wrap((double) c))
                .addConverter(double[].class, DoubleBuffer::wrap);

        return TO_DOUBLE_BUFFER.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), bytes -> ByteBuffer.wrap(bytes).asDoubleBuffer());
    }

    public static DoubleBuffer toDoubleBuffer(final Object value){
        return toDoubleBuffer(value, v -> new ClassCastException(String.format("Unable to convert '%s' to DoubleBuffer", v)));
    }

    public static <E extends Throwable> byte[] toByteArray(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        return getByteArrayConverter().convert(value, exceptionFactory);
    }

    public static byte[] toByteArray(final Object value){
        return toByteArray(value, v -> new ClassCastException(String.format("Unable to convert '%s' to array of bytes", v)));
    }

    public static <E extends Throwable> char[] toCharArray(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<char[]>> CONVERTER = () -> new ToTypeConverter<char[]>()
                .addConverter(Character.class, c -> new char[]{ c })
                .addConverter(char[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toCharArray)
                .addConverter(Character[].class, ArrayUtils::unwrapArray)
                .addConverter(CharBuffer.class, Buffers::readRemaining)
                .addConverter(String.class, String::toCharArray);

        return TO_CHAR_ARRAY.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), ArrayUtils::toCharArray);
    }

    public static char[] toCharArray(final Object value){
        return toCharArray(value, v -> new ClassCastException(String.format("Unable to convert '%s' to array of chars", v)));
    }

    public static <E extends Throwable> short[] toShortArray(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<short[]>> CONVERTER = () -> new ToTypeConverter<short[]>()
                .addConverter(Short.class, c -> new short[]{ c })
                .addConverter(Byte.class, b -> new short[]{ b })
                .addConverter(short[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toShortArray)
                .addConverter(Short[].class, ArrayUtils::unwrapArray)
                .addConverter(ShortBuffer.class, Buffers::readRemaining);

        return TO_SHORT_ARRAY.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), ArrayUtils::toShortArray);
    }

    public static short[] toShortArray(final Object value){
        return toShortArray(value, v -> new ClassCastException(String.format("Unable to convert '%s' to array of 16-bit signed values", v)));
    }

    public static <E extends Throwable> int[] toIntArray(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<int[]>> CONVERTER = () -> new ToTypeConverter<int[]>()
                .addConverter(Integer.class, i -> new int[]{ i })
                .addConverter(Short.class, c -> new int[]{ c })
                .addConverter(Byte.class, b -> new int[]{ b })
                .addConverter(Character.class, c -> new int[]{ c })
                .addConverter(int[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toIntArray)
                .addConverter(Integer[].class, ArrayUtils::unwrapArray)
                .addConverter(IntBuffer.class, Buffers::readRemaining);

        return TO_INT_ARRAY.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), ArrayUtils::toIntArray);
    }

    public static int[] toIntArray(final Object value){
        return toIntArray(value, v -> new ClassCastException(String.format("Unable to convert '%s' to array of 32-bit signed values", v)));
    }

    public static <E extends Throwable> long[] toLongArray(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<long[]>> CONVERTER = () -> new ToTypeConverter<long[]>()
                .addConverter(Long.class, l -> new long[]{ l })
                .addConverter(Integer.class, i -> new long[]{ i })
                .addConverter(Short.class, c -> new long[]{ c })
                .addConverter(Byte.class, b -> new long[]{ b })
                .addConverter(Character.class, c -> new long[]{ c })
                .addConverter(long[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toLongArray)
                .addConverter(Long[].class, ArrayUtils::unwrapArray)
                .addConverter(LongBuffer.class, Buffers::readRemaining);

        return TO_LONG_ARRAY.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), ArrayUtils::toLongArray);
    }

    public static long[] toLongArray(final Object value){
        return toLongArray(value, v -> new ClassCastException(String.format("Unable to convert '%s' to array of 64-bit signed values", v)));
    }

    public static <E extends Throwable> float[] toFloatArray(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<float[]>> CONVERTER = () -> new ToTypeConverter<float[]>()
                .addConverter(Float.class, f -> new float[]{ f })
                .addConverter(Integer.class, i -> new float[]{ i })
                .addConverter(Short.class, c -> new float[]{ c })
                .addConverter(Byte.class, b -> new float[]{ b })
                .addConverter(Character.class, c -> new float[]{ c })
                .addConverter(float[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toFloatArray)
                .addConverter(Float[].class, ArrayUtils::unwrapArray)
                .addConverter(FloatBuffer.class, Buffers::readRemaining);

        return TO_FLOAT_ARRAY.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), ArrayUtils::toFloatArray);
    }

    public static float[] toFloatArray(final Object value){
        return toFloatArray(value, v -> new ClassCastException(String.format("Unable to convert '%s' to array of floating-point values with single precision", v)));
    }

    public static <E extends Throwable> double[] toDoubleArray(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<double[]>> CONVERTER = () -> new ToTypeConverter<double[]>()
                .addConverter(Float.class, f -> new double[]{ f })
                .addConverter(Double.class, d -> new double[]{ d })
                .addConverter(Long.class, l -> new double[]{ l })
                .addConverter(Integer.class, i -> new double[]{ i })
                .addConverter(Short.class, c -> new double[]{ c })
                .addConverter(Byte.class, b -> new double[]{ b })
                .addConverter(Character.class, c -> new double[]{ c })
                .addConverter(double[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toDoubleArray)
                .addConverter(Double[].class, ArrayUtils::unwrapArray)
                .addConverter(DoubleBuffer.class, Buffers::readRemaining);

        return TO_DOUBLE_ARRAY.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), ArrayUtils::toDoubleArray);
    }

    public static double[] toDoubleArray(final Object value){
        return toDoubleArray(value, v -> new ClassCastException(String.format("Unable to convert '%s' to array of floating-point values with double precision", v)));
    }

    public static <E extends Throwable> String[] toStringArray(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<String[]>> CONVERTER = () -> new ToTypeConverter<String[]>()
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
                .addConverter(char[].class, array -> new String[]{ new String(array) })
                .addConverter(Character[].class, array -> new String[] { new String(ArrayUtils.unwrapArray(array)) })
                .addConverter(BigInteger[].class, array -> ArrayUtils.transform(array, String.class, BigInteger::toString))
                .addConverter(BigDecimal[].class, array -> ArrayUtils.transform(array, String.class, BigDecimal::toString))
                .addConverter(Date[].class, array -> ArrayUtils.transform(array, String.class, v -> Long.toString(v.getTime())))
                .addConverter(Duration[].class, array -> ArrayUtils.transform(array, String.class, Duration::toString))
                .addConverter(Instant[].class, array -> ArrayUtils.transform(array, String.class, Instant::toString))
                .addConverter(Object[].class, array -> ArrayUtils.transform(array, String.class, Objects::toString))
                .addConverter(Object.class, v -> new String[]{ v.toString() });

        return TO_STRING_ARRAY.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static String[] toStringArray(final Object value){
        return toStringArray(value, v -> new ClassCastException(String.format("Unable to convert '%s' to array of strings", v)));
    }

    public static <E extends Throwable> boolean[] toBooleanArray(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E {
        final Supplier<ToTypeConverter<boolean[]>> CONVERTER = () -> new ToTypeConverter<boolean[]>()
                .addConverter(Boolean.class, b -> new boolean[]{ b })
                .addConverter(Character.class, v -> new boolean[]{ v != '\0'})
                .addConverter(Long.class, v -> new boolean[]{ intToBoolean(v) })
                .addConverter(Integer.class, v -> new boolean[]{intToBoolean(v)})
                .addConverter(Short.class, v -> new boolean[]{intToBoolean(v)})
                .addConverter(Byte.class, v -> new boolean[]{intToBoolean(v)})
                .addConverter(Float.class, v -> new boolean[]{realToBoolean(v)})
                .addConverter(Double.class, v -> new boolean[]{realToBoolean(v)})
                .addConverter(String.class, v -> new boolean[]{Boolean.parseBoolean(v)})
                .addConverter(BigInteger.class, v -> ArrayUtils.toBoolArray(v.toByteArray()))
                .addConverter(Date.class, v -> new boolean[]{ v.getTime() > 0 })
                .addConverter(Duration.class, v -> new boolean[]{ v.compareTo(Duration.ZERO) > 0 })
                .addConverter(boolean[].class, Function.identity())
                .addConverter(byte[].class, ArrayUtils::toBoolArray)
                .addConverter(Boolean[].class, ArrayUtils::unwrapArray);

        return TO_BOOL_ARRAY.lazyGet(CONVERTER).convert(value, exceptionFactory, getByteArrayConverter(), ArrayUtils::toBoolArray);
    }

    public static boolean[] toBooleanArray(final Object value){
        return toBooleanArray(value, v -> new ClassCastException(String.format("Unable to convert '%s' to array of booleans", v)));
    }

    public static <E extends Throwable> Duration toDuration(final Object value, final Function<Object, ? extends E> exceptionFactory) throws E{
        final Supplier<ToTypeConverter<Duration>> CONVERTER = () -> new ToTypeConverter<Duration>()
                .addConverter(Duration.class, Function.identity())
                .addConverter(Number.class, n -> Duration.ofNanos(n.longValue()))
                .addConverter(String.class, Duration::parse)
                .addConverter(StringBuffer.class, Duration::parse)
                .addConverter(StringBuilder.class, Duration::parse)
                .addConverter(char[].class, chars -> Duration.parse(new String(chars)))
                .addConverter(CharBuffer.class, Duration::parse);

        return TO_DURATION.lazyGet(CONVERTER).convert(value, exceptionFactory);
    }

    public static Duration toDuration(final Object value){
        return toDuration(value, v -> new ClassCastException(String.format("Unable to convert '%s' to Duration", v)));
    }
}
