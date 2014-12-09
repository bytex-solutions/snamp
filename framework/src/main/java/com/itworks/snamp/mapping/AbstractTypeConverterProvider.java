package com.itworks.snamp.mapping;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the base class for building type converter factory.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractTypeConverterProvider implements TypeConverterProvider {
    private final Map<Type, TypeConverterImpl<?>> converters;

    /**
     * Initializes a new type converter factory.
     */
    protected AbstractTypeConverterProvider() {
        converters = new HashMap<>(10);
        registerConverter(TypeLiterals.OBJECT, TypeLiterals.STRING,
                new Function<Object, String>() {
                    @Override
                    public String apply(final Object input) {
                        return Objects.toString(input, "");
                    }
                });
    }

    protected static <I extends O, O> Function<I, O> identityConverter() {
        return new Function<I, O>() {
            @Override
            public O apply(final I input) {
                return input;
            }
        };
    }

    /**
     * Registers a new converter.
     * <p>
     *     It is recommended to call this method inside of the constructor only.
     * @param inputType Type of the input value to be converted. Cannot be {@literal null}.
     * @param outputType Type of the conversion result. Cannot be {@literal null}.
     * @param converter The converter to register.
     * @param <I> Type definition of the input value to be converted.
     * @param <O> Type definition of the conversion result.
     */
    @SuppressWarnings("unchecked")
    protected final <I, O> void registerConverter(final TypeToken<I> inputType,
                                                  final TypeToken<O> outputType,
                                                  final Function<I, O> converter) {
        final TypeConverterImpl<O> impl;
        if (converters.containsKey(outputType.getType()))
            impl = (TypeConverterImpl<O>) converters.get(outputType.getType());
        else converters.put(outputType.getType(), impl = new TypeConverterImpl<>(outputType));
        impl.registerConverter(inputType, converter);
    }

    protected final <I extends O, O> void registerIdentityConverter(final TypeToken<I> inputType,
                                                  final TypeToken<O> outputType) {
        registerConverter(inputType, outputType, AbstractTypeConverterProvider.<I, O>identityConverter());
    }

    protected final <T> void registerIdentityConverter(final TypeToken<T> ioType){
        registerIdentityConverter(ioType, ioType);
    }

    private static interface InternalTypeConverter<T> extends TypeConverter<T> {
        T convertFrom(final Object value);
        Function getConverterFrom(final Type source);
    }

    private static final class TypeConverterImpl<T> extends HashMap<Type, Function> implements InternalTypeConverter<T> {
        private final TypeToken<T> returnType;

        private TypeConverterImpl(final TypeToken<T> type) {
            super(5);
            returnType = type;
        }

        /**
         * Returns the type for which this converter is instantiated.
         *
         * @return The type for which this converter is instantiated.
         */
        @Override
        public final TypeToken<T> getType() {
            return returnType;
        }

        @Override
        public Function getConverterFrom(final Type source) {
            //cache hit
            if (containsKey(source)) return get(source);
            //long-time search
            for (final Type cls : keySet())
                if (TypeLiterals.isAssignable(source, cls)) {
                    final Function converter = get(cls);
                    put(source, converter);
                    return converter;
                }
            return null;
        }

        /**
         * Determines whether the value of the specified type can be converted into {@link T}.
         *
         * @param source The type of the source value.
         * @return {@literal true}, if the value of the specified type can be converted into {@link T}; otherwise, {@literal false}.
         */
        @Override
        public final boolean canConvertFrom(final TypeToken<?> source) {
            return returnType.isAssignableFrom(source) ||
                    getConverterFrom(source.getType()) != null;
        }

        /**
         * Converts the value into an instance of {@link T} class.
         *
         * @param value The value to convert.
         * @return The value of the original type described by this converter.
         * @throws IllegalArgumentException Unsupported type of the source value. Check the type with {@link #canConvertFrom(TypeToken)} method.
         */
        @SuppressWarnings("unchecked")
        @Override
        public final T convertFrom(final Object value) throws IllegalArgumentException {
            if (value == null) return null;
            else if (TypeLiterals.isInstance(value, returnType))
                return TypeLiterals.cast(value, returnType);
            else {
                final Function converter = getConverterFrom(value.getClass());
                if (converter == null)
                    throw new IllegalArgumentException(String.format("Cannot convert %s to %s.", value, returnType));
                return TypeLiterals.cast(converter.apply(value), returnType);
            }
        }

        private <I> void registerConverter(final TypeToken<I> inputType,
                                           final Function<I, T> converter) {
            put(inputType.getType(), converter);
        }
    }

    private InternalTypeConverter getTypeConverter(final Type conversionType){
        if (conversionType == null) return null;
        else if (shouldNormalize(conversionType)) return getTypeConverter(normalizeClass(conversionType));
        else return converters.get(conversionType);
    }

    /**
     * Returns the converter for the specified type.
     *
     * @param t   The type for which the converter should be created.
     * @param <T> The type for which the converter should be created.
     * @return The converter for the specified type; or {@literal null}, if the converter for the specified type
     *         is not supported.
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <T> TypeConverter<T> getTypeConverter(final TypeToken<T> t) {
        return getTypeConverter(t.getType());
    }

    /**
     * Converts the input value into the specified type.
     * @param outputType  The type of the conversion result.
     * @param input The value to be converted.
     * @param <O> Type definition of the conversion result.
     * @return The conversion result.
     * @throws java.lang.IllegalArgumentException Conversion is not supported.
     */
    @SuppressWarnings("unchecked")
    public final <O> O convert(final TypeToken<O> outputType,
                                final Object input) throws IllegalArgumentException {
        final InternalTypeConverter<O> converter = getTypeConverter(outputType.getType());
        if (converter == null)
            throw new IllegalArgumentException(String.format("Converter for type %s not found.", outputType));
        else return converter.convertFrom(input);
    }

    /**
     * Gets converter for the specified input/output types.
     * @param inputType Type of the value to be converted.
     * @param outputType Type of the conversion result.
     * @return The conversion result.
     */
    @SuppressWarnings("unchecked")
    public final <I, O> Function<I, O> getPlainConverter(final TypeToken<I> inputType, final TypeToken<O> outputType) {
        final InternalTypeConverter<O> converter = getTypeConverter(outputType.getType());
        return converter != null && converter.canConvertFrom(inputType) ?
                converter.getConverterFrom(inputType.getType()) :
                null;
    }

    /**
     * Determines whether the specified Java type should be normalized.
     * @param classInfo The Java type.
     * @return The normalized Java type (if it is primitive type or array of primitive types).
     */
    protected static boolean shouldNormalize(final Class<?> classInfo){
        return classInfo.isPrimitive() || classInfo.isArray() && classInfo.getComponentType().isPrimitive();
    }

    /**
     * Determines whether the specified Java type should be normalized.
     * @param classInfo The Java type.
     * @return The normalized Java type (if it is primitive type or array of primitive types).
     */
    protected static boolean shouldNormalize(final Type classInfo){
        return classInfo instanceof Class<?> && shouldNormalize((Class<?>)classInfo);
    }

    /**
     * Returns the wrapper class for the primitive type.
     * @param classInfo The class information about primitive type.
     * @return The wrapper class for the primitive type.
     */
    public static Type normalizeClass(final Type classInfo){
        return classInfo instanceof Class<?> ? normalizeClass((Class<?>)classInfo) : classInfo;
    }

    /**
     * Returns the wrapper class for the primitive type.
     * @param classInfo The class information about primitive type.
     * @return The wrapper class for the primitive type.
     */
    public static Class<?> normalizeClass(final Class<?> classInfo){
        switch (classInfo.getCanonicalName()){
            case "byte": return Byte.class;
            case "short": return Short.class;
            case "int": return Integer.class;
            case "long": return Long.class;
            case "boolean": return Boolean.class;
            case "void": return Void.class;
            case "float": return Float.class;
            case "double": return Double.class;
            case "char": return Character.class;
            case "byte[]": return Byte[].class;
            case "short[]": return Short[].class;
            case "int[]": return Integer[].class;
            case "long[]": return Long[].class;
            case "boolean[]": return Boolean[].class;
            case "void[]": return Void[].class;
            case "float[]": return Float[].class;
            case "double[]": return Double[].class;
            case "char[]": return Character[].class;
            default: return classInfo;
        }
    }

    /**
     * Returns a type converter for the specified class name.
     * @param nativeType The class name for which converter is required.
     * @return A type converter for the specified class name.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final TypeConverter<?> getTypeConverter(final String nativeType){
        switch (nativeType){
            case "byte":
            case "java.lang.Byte": return getTypeConverter(Byte.class);
            case "short":
            case "java.lang.Short": return getTypeConverter(Short.class);
            case "int":
            case "java.lang.Integer": return getTypeConverter(Integer.class);
            case "long":
            case "java.lang.Long": return getTypeConverter(Long.class);
            case "boolean":
            case "java.lang.Boolean": return getTypeConverter(Boolean.class);
            case "float":
            case "java.lang.Float": return getTypeConverter(Float.class);
            case "double":
            case "java.lang.Double": return getTypeConverter(Double.class);
            case "void":
            case "java.lang.Void": return getTypeConverter(Void.class);
            case "char":
            case "java.lang.Character": return getTypeConverter(Character.class);
            case "java.lang.BigInteger": return getTypeConverter(BigInteger.class);
            case "java.lang.BigDecimal": return getTypeConverter(BigDecimal.class);
            case "byte[]":
            case "java.lang.Byte[]": return getTypeConverter(Byte[].class);
            case "short[]":
            case "java.lang.Short[]": return getTypeConverter(Short[].class);
            case "int[]":
            case "java.lang.Integer[]": return getTypeConverter(Integer[].class);
            case "long[]":
            case "java.lang.Long[]": return getTypeConverter(Long[].class);
            case "boolean[]":
            case "java.lang.Boolean[]": return getTypeConverter(Boolean[].class);
            case "float[]":
            case "java.lang.Float[]": return getTypeConverter(Float[].class);
            case "double[]":
            case "java.lang.Double[]": return getTypeConverter(Double[].class);
            case "void[]":
            case "java.lang.Void[]": return getTypeConverter(Void[].class);
            case "char[]":
            case "java.lang.Character[]": return getTypeConverter(Character[].class);
            case "java.lang.BigInteger[]": return getTypeConverter(BigInteger[].class);
            case "java.lang.BigDecimal[]": return getTypeConverter(BigDecimal[].class);
            default:
                try {
                    final Class<?> loadedNativeType = Class.forName(nativeType);
                    return getTypeConverter(loadedNativeType);
                }
                catch (final ClassNotFoundException e) {
                    return getTypeConverter(String.class);
                }
        }
    }
}
