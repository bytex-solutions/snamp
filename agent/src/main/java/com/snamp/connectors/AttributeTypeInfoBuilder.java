package com.snamp.connectors;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.math.*;
import java.util.*;

/**
 * Represents builder for {@link AttributeTypeInfo} class.
 * @author roman
 */
public abstract class AttributeTypeInfoBuilder {
    /**
     * Marks the method with single argument and non-void return value as converter.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected static @interface Converter{

    }

    /**
     * Represents advanced representation of the {@link com.snamp.connectors.AttributeTypeInfoBuilder.AttributeConvertibleTypeInfo} that
     * supports conversion methods.
     */
    public static interface AttributeConvertibleTypeInfo<T> extends AttributeJavaTypeInfo<T>{
        /**
         * Converts the specified value into the
         * @param value The value to convert.
         * @return The value of the attribute.
         */
        public T convertFrom(final Object value) throws IllegalArgumentException;
    }

    /**
     * Initializes a new attribute type info builder.
     */
    protected AttributeTypeInfoBuilder(){

    }

    /**
     * Creates a new {@link com.snamp.connectors.AttributeTypeInfoBuilder.AttributeConvertibleTypeInfo} for the specified native type using the specified builder.
     * @param builderType A type that contains converters (as static methods).
     * @param nativeType The underlying Java type.
     * @return
     */
    public static AttributeConvertibleTypeInfo<?> createTypeInfo(final Class<? extends AttributeTypeInfoBuilder> builderType, final String nativeType){
        switch (nativeType){
            case "byte":
            case "java.lang.Byte": return createTypeInfo(builderType, Byte.class);
            case "short":
            case "java.lang.Short": return createTypeInfo(builderType, Short.class);
            case "int":
            case "java.lang.Integer": return createTypeInfo(builderType, Integer.class);
            case "long":
            case "java.lang.Long": return createTypeInfo(builderType, Long.class);
            case "boolean":
            case "java.lang.Boolean": return createTypeInfo(builderType, Boolean.class);
            case "float":
            case "java.lang.Float": return createTypeInfo(builderType, Float.class);
            case "double":
            case "java.lang.Double": return createTypeInfo(builderType, Double.class);
            case "void":
            case "java.lang.Void": return createTypeInfo(builderType, Void.class);
            case "char":
            case "java.lang.Character": return createTypeInfo(builderType, Character.class);
            case "java.lang.BigInteger": return createTypeInfo(builderType, BigInteger.class);
            case "java.lang.BigDecimal": return createTypeInfo(builderType, BigDecimal.class);
            case "byte[]":
            case "java.lang.Byte[]": return createTypeInfo(builderType, Byte.class);
            case "short[]":
            case "java.lang.Short[]": return createTypeInfo(builderType, Short.class);
            case "int[]":
            case "java.lang.Integer[]": return createTypeInfo(builderType, Integer.class);
            case "long[]":
            case "java.lang.Long[]": return createTypeInfo(builderType, Long.class);
            case "boolean[]":
            case "java.lang.Boolean[]": return createTypeInfo(builderType, Boolean.class);
            case "float[]":
            case "java.lang.Float[]": return createTypeInfo(builderType, Float.class);
            case "double[]":
            case "java.lang.Double[]": return createTypeInfo(builderType, Double.class);
            case "void[]":
            case "java.lang.Void[]": return createTypeInfo(builderType, Void.class);
            case "char[]":
            case "java.lang.Character[]": return createTypeInfo(builderType, Character.class);
            case "java.lang.BigInteger[]": return createTypeInfo(builderType, BigInteger.class);
            case "java.lang.BigDecimal[]": return createTypeInfo(builderType, BigDecimal.class);
            default:
                try {
                    return createTypeInfo(builderType, Class.forName(nativeType));
                }
                catch (final ClassNotFoundException e) {
                    return createTypeInfo(builderType, Object.class);
                }

        }
    }

    /**
     * Creates a new {@link com.snamp.connectors.AttributeTypeInfoBuilder.AttributeConvertibleTypeInfo} for the specified native type using the specified builder.
     * @param builderType A type that contains converters (as static methods).
     * @param nativeType The underlying Java type.
     * @param <T>
     * @return
     */
    public static <T> AttributeConvertibleTypeInfo<T> createTypeInfo(final Class<? extends AttributeTypeInfoBuilder> builderType, final Class<T> nativeType){
        final Method[] candidates = builderType.getMethods();
        return new AttributeConvertibleTypeInfo<T>() {
            //convertersTo: key is an output result of nativeType
            private final Map<Class<?>, Method> convertersToCache = new HashMap<>(20);
            //convertersFrom: key is an input for the to-nativeType conversion
            private final Map<Class<?>, Method> convertersFromCache = new HashMap<>(20);

            private Method getConverterFrom(final Class<?> sourceType){
                if(sourceType == null) return null;
                else if(convertersFromCache.containsKey(sourceType)) return convertersFromCache.get(sourceType);
                else for(final Method m : candidates)
                        if(Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers()) && m.isAnnotationPresent(Converter.class) && m.getReturnType().isAssignableFrom(nativeType)){
                            convertersFromCache.put(m.getParameterTypes()[0], m);
                            return m;
                        }
                return null;
            }

            @Override
            public final T convertFrom(final Object value) throws IllegalArgumentException {
                if(value == null) return null;
                else if(nativeType.isInstance(value)) return nativeType.cast(value);
                final Method converter = getConverterFrom(value.getClass());
                if(converter!=null) try {
                    return nativeType.cast(converter.invoke(null, new Object[]{value}));
                }
                catch (final ReflectiveOperationException e) {
                    throw new IllegalArgumentException(e);
                }
                else throw new IllegalArgumentException(String.format("Could not convert %s to %s", value, nativeType));
            }

            @Override
            public final <G> boolean canConvertFrom(final Class<G> source) {
                if(source != null)
                    if(source.isPrimitive() || source.isArray() && source.getComponentType().isPrimitive()) return canConvertFrom(normalizeClass(source));
                    else if(nativeType.isAssignableFrom(source)) return true;
                final Method converter = getConverterFrom(source);
                return converter != null;
            }

            @Override
            public final Class<T> getNativeClass() {
                return nativeType;
            }

            private <G> Method getConverterTo(final Class<G> target){
                if(target == null) return null;
                else if(convertersToCache.containsKey(target)) return convertersToCache.get(target);
                else for(final Method m : candidates)
                        if(Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers()) && m.isAnnotationPresent(Converter.class) && nativeType.isAssignableFrom(m.getParameterTypes()[0])){
                            convertersToCache.put(m.getReturnType(), m);
                            return m;
                        }
                return null;
            }

            @Override
            public final <G> boolean canConvertTo(final Class<G> target) {
                if(target != null)
                    if(target.isPrimitive() || target.isArray() && target.getComponentType().isPrimitive()) return canConvertTo(normalizeClass(target));
                    else if(target.isAssignableFrom(nativeType)) return true;
                final Method converter = getConverterTo(target);
                return converter != null;
            }

            /**
             * Converts the attribute value to the specified type.
             * @param value The attribute value to convert.
             * @param target The type of the conversion result.
             * @param <G> The conversion result.
             * @return The conversion result.
             * @throws IllegalArgumentException Conversion is not supported.
             */
            @Override
            public final <G> G convertTo(final Object value, final Class<G> target) throws IllegalArgumentException {
                if(target !=null && target.isInstance(value)) return target.cast(value);
                final Method converter = getConverterTo(target);
                if(converter == null) throw new IllegalArgumentException(String.format("Conversion to %s is not supported.", target));
                else try {
                    return target.cast(converter.invoke(null, new Object[]{value}));
                }
                catch(final ReflectiveOperationException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };
    }

    /**
     * Creates a new {@link com.snamp.connectors.AttributeTypeInfoBuilder.AttributeConvertibleTypeInfo} instance.
     * @param nativeType
     * @return
     */
    public final <T> AttributeConvertibleTypeInfo<T> createTypeInfo(final Class<T> nativeType){
        return createTypeInfo(getClass(), nativeType);
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

    public static boolean isTypeOf(final AttributeJavaTypeInfo<?> attributeType, final Class<?> typeToCompare){
        return attributeType!=null && Objects.equals(attributeType.getNativeClass(), typeToCompare);
    }

    public static boolean isTypeOf(final com.snamp.connectors.AttributeTypeInfo attributeType, final Class<?> typeToCompare){
        return attributeType instanceof AttributeJavaTypeInfo && isTypeOf((AttributeJavaTypeInfo<?>)attributeType, typeToCompare);
    }
}
