package com.itworks.snamp;

import org.apache.commons.collections4.Transformer;

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
    private final Map<Class<?>, TypeConverterImpl<?>> converters;

    /**
     * Initializes a new type converter factory.
     */
    protected AbstractTypeConverterProvider(){
        converters = new HashMap<>(10);
        registerConverter(Object.class, String.class,
                new Transformer<Object, String>() {
                    @Override
                    public String transform(final Object input) {
                        return Objects.toString(input, "");
                    }
                });
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
    protected final <I, O> void registerConverter(final Class<I> inputType,
                                                  final Class<O> outputType,
                                                  final Transformer<I, O> converter){
        final TypeConverterImpl<O> impl;
        if(converters.containsKey(outputType))
            impl = (TypeConverterImpl<O>)converters.get(outputType);
        else converters.put(outputType, impl = new TypeConverterImpl<>(outputType));
        impl.registerConverter(inputType, converter);
    }

    private static final class TypeConverterImpl<T> extends HashMap<Class<?>, Transformer> implements TypeConverter<T> {
        private final Class<T> returnType;

        private TypeConverterImpl(final Class<T> type) {
            super(5);
            returnType = type;
        }

        /**
         * Returns the type for which this converter is instantiated.
         *
         * @return The type for which this converter is instantiated.
         */
        @Override
        public final Class<T> getType() {
            return returnType;
        }

        private Transformer getConverterFrom(final Class<?> source) {
            //cache hit
            if (containsKey(source)) return get(source);
            //long-time search
            for (final Class<?> cls : keySet())
                if (cls.isAssignableFrom(source)) {
                    final Transformer converter = get(cls);
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
        public final boolean canConvertFrom(final Class<?> source) {
            return returnType.isAssignableFrom(source) || getConverterFrom(source) != null;
        }

        /**
         * Converts the value into an instance of {@link T} class.
         *
         * @param value The value to convert.
         * @return The value of the original type described by this converter.
         * @throws IllegalArgumentException Unsupported type of the source value. Check the type with {@link #canConvertFrom(Class)} method.
         */
        @SuppressWarnings("unchecked")
        @Override
        public final T convertFrom(final Object value) throws IllegalArgumentException {
            if (value == null) return null;
            else if (returnType.isInstance(value)) return returnType.cast(value);
            else {
                final Transformer converter = getConverterFrom(value.getClass());
                if (converter == null)
                    throw new IllegalArgumentException(String.format("Cannot convert %s to %s.", value, returnType));
                return returnType.cast(converter.transform(value));
            }
        }

        private <I> void registerConverter(final Class<I> inputType,
                                           final Transformer<I, T> converter) {
            put(inputType, converter);
        }
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
    public final <T> TypeConverterImpl<T> getTypeConverter(final Class<T> t) {
        if (t == null) return null;
        else if (shouldNormalize(t)) return (TypeConverterImpl<T>) getTypeConverter(normalizeClass(t));
        else return (TypeConverterImpl<T>) converters.get(t);
    }

    /**
     * Converts the input value into the specified type.
     * @param outputType  The type of the conversion result.
     * @param input The value to be converted.
     * @param <O> Type definition of the conversion result.
     * @return The conversion result.
     * @throws java.lang.IllegalArgumentException Conversion is not supported.
     */
    public final <O> O convert(final Class<O> outputType,
                                final Object input) throws IllegalArgumentException {
        final TypeConverterImpl<O> converter = getTypeConverter(outputType);
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
    public final <I, O> Transformer<I, O> getPlainConverter(final Class<I> inputType, final Class<O> outputType) {
        final TypeConverterImpl<O> converter = getTypeConverter(outputType);
        return converter != null && converter.canConvertFrom(inputType) ?
                converter.getConverterFrom(inputType) :
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
