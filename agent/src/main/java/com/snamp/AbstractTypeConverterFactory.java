package com.snamp;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.math.*;
import java.util.*;

/**
 * Represents the base class for building type converter factory.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractTypeConverterFactory implements TypeConverterFactory {
    private final Wrapper<Map<Class<?>, TypeConverter<?>>> converters;

    /**
     * Initializes a new type converter factory.
     */
    protected AbstractTypeConverterFactory(){
        converters = new SoftMap<>(10);
    }

    /**
     * Marks the method with single argument and non-void return value as converter.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected static @interface Converter{

    }

    /**
     * Converts {@link Object} to {@link String}.
     * @param o An object to convert.
     * @return Conversion result.
     */
    @Converter
    public static String objectToString(final Object o){
        return Objects.toString(o, "");
    }

    private static final boolean isPublicStatic(final Method m){
        return (m.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) != 0;
    }

    private static final class TypeConverterImpl<T> extends HashMap<Class<?>, Method> implements TypeConverter<T> {
        private final Class<T> type;
        private final Method[] methods;

        public TypeConverterImpl(final Class<T> type, final List<Method> methods){
            super(5);
            this.type = type;
            this.methods = new Method[methods.size()];
            for(int i = 0; i < methods.size(); i++)
                this.methods[i] = methods.get(i);
        }

        /**
         * Returns the type for which this converter is instantiated.
         *
         * @return The type for which this converter is instantiated.
         */
        @Override
        public final Class<T> getType() {
            return type;
        }

        private final Method getConverterFrom(final Class<?> source){
            if(containsKey(source)) return get(source);
            for(final Method m: methods){
                final Class<?>[] params = m.getParameterTypes();
                if(params.length == 1 && params[0].isAssignableFrom(source)){
                    put(source, m);
                    return m;
                }
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
            return type.isAssignableFrom(source) || getConverterFrom(source) != null;
        }

        /**
         * Converts the value into an instance of {@link T} class.
         *
         * @param value The value to convert.
         * @return The value of the original type described by this converter.
         * @throws IllegalArgumentException Unsupported type of the source value. Check the type with {@link #canConvertFrom(Class)} method.
         */
        @Override
        public final T convertFrom(final Object value) throws IllegalArgumentException {
            if(value == null) return null;
            else if(type.isInstance(value)) return type.cast(value);
            else{
                final Method converter = getConverterFrom(value.getClass());
                if(converter == null) throw new IllegalArgumentException(String.format("Cannot convert %s to %s.", value, type));
                else if(!converter.isAccessible()) converter.setAccessible(true);
                try {
                    return type.cast(converter.invoke(null, value));
                }
                catch (final ReflectiveOperationException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
    }

    /**
     * Returns the converter for the specified type constructed from the
     * public static methods annotated with {@link com.snamp.AbstractTypeConverterFactory.Converter}
     * and declared in the specified converter factory.
     * @param factory The class that contains declaration of converters. Cannot be {@literal null}.
     * @param t The type for which the converter should be constructed. Cannot be {@literal null}.
     * @param <T> Conversion result type.
     * @return An instance of the converter; or {@literal null}, if conversion is not supported.
     */
    protected static <T> TypeConverter<T> getTypeConverter(final Class<? extends AbstractTypeConverterFactory> factory, final Class<T> t){
        final List<Method> methods = new ArrayList<>();
        for(final Method m: factory.getMethods())
            if(m.isAnnotationPresent(Converter.class) && isPublicStatic(m) && t.isAssignableFrom(m.getReturnType()))
                methods.add(m);
        return methods.size() > 0 ? new TypeConverterImpl<>(t, methods) : null;
    }

    /**
     * Returns the converter for the specified type.
     *
     * @param t   The type for which the converter should be created.
     * @param <T> The type for which the converter should be created.
     * @return The converter for the specified type; or {@literal null}, if the converter for the specified type
     *         is not supported.
     */
    @Override
    public synchronized final <T> TypeConverter<T> getTypeConverter(final Class<T> t) {
        if(t == null) return null;
        else if(shouldNormalize(t)) return (TypeConverter<T>)getTypeConverter(normalizeClass(t));
        final Class<? extends AbstractTypeConverterFactory> thisClass = getClass();
        return converters.handle(new Wrapper.WrappedObjectHandler<Map<Class<?>, TypeConverter<?>>, TypeConverter<T>>() {
            @Override
            public final TypeConverter<T> invoke(final Map<Class<?>, TypeConverter<?>> converters) {
                TypeConverter<T> converter = (TypeConverter<T>)converters.get(t);
                if(converter != null) return converter;
                converter = getTypeConverter(thisClass, t);
                if(converter != null)
                    converters.put(t, converter);
                return converter;
            }
        });
    }

    /**
     * Determines whether the specified Java type should be normalized.
     * @param classInfo The Java type.
     * @return The normalized Java type (if it is primitive type or array of primitive types).
     */
    protected static final boolean shouldNormalize(final Class<?> classInfo){
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
