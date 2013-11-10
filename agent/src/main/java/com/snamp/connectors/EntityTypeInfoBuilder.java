package com.snamp.connectors;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.math.*;
import java.util.*;
import java.util.logging.*;

/**
 * Represents a base class for building management entity types, such as attributes or notifications.
 * @param <E> Type of the management entity.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class EntityTypeInfoBuilder<E extends EntityTypeInfo> implements EntityTypeInfoFactory<E> {
    private static final Logger logger = Logger.getLogger("com.snamp");
    private final Class<E> entityType;

    /**
     * Initializes a new empty entity type builder.
     * @param entityType Type of the management entity. Cannot be {@literal null}.
     * @throws IllegalArgumentException entityType is {@literal null}.
     */
    protected EntityTypeInfoBuilder(final Class<E> entityType){
        if(entityType == null) throw new IllegalArgumentException("entityType is null.");
        this.entityType = entityType;
    }

    /**
     * Marks the method with single argument and non-void return value as converter.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected static @interface Converter{

    }

    /**
     * Represents advanced representation of the {@link com.snamp.connectors.EntityTypeInfoBuilder.AttributeTypeConverter} that
     * supports conversion methods.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface AttributeTypeConverter extends AttributeTypeInfo {
        /**
         * Converts the specified value into the
         * @param value The value to convert.
         * @return The value of the attribute.
         * @throws IllegalArgumentException Conversion from the specified value is not supported.
         */
        public Object convertFrom(final Object value) throws IllegalArgumentException;
    }

    /**
     * Represents type information about universal entity.
     */
    private static interface EntityTypeConverter extends AttributeTypeConverter, NotificationContentTypeInfo {

    }

    private static EntityTypeConverter createTypeInfo(final Class<? extends EntityTypeInfoBuilder> builderType, final String nativeType){
        switch (nativeType){
            case "byte":
            case "java.lang.Byte": return createTypeInfo(builderType, Byte.class, Byte.class);
            case "short":
            case "java.lang.Short": return createTypeInfo(builderType, Short.class, Short.class);
            case "int":
            case "java.lang.Integer": return createTypeInfo(builderType, Integer.class, Integer.class);
            case "long":
            case "java.lang.Long": return createTypeInfo(builderType, Long.class, Long.class);
            case "boolean":
            case "java.lang.Boolean": return createTypeInfo(builderType, Boolean.class, Boolean.class);
            case "float":
            case "java.lang.Float": return createTypeInfo(builderType, Float.class, Float.class);
            case "double":
            case "java.lang.Double": return createTypeInfo(builderType, Double.class, Double.class);
            case "void":
            case "java.lang.Void": return createTypeInfo(builderType, Void.class, Void.class);
            case "char":
            case "java.lang.Character": return createTypeInfo(builderType, Character.class, Character.class);
            case "java.lang.BigInteger": return createTypeInfo(builderType, BigInteger.class, BigInteger.class);
            case "java.lang.BigDecimal": return createTypeInfo(builderType, BigDecimal.class, BigDecimal.class);
            case "byte[]":
            case "java.lang.Byte[]": return createTypeInfo(builderType, Byte[].class, Byte[].class);
            case "short[]":
            case "java.lang.Short[]": return createTypeInfo(builderType, Short[].class, Short[].class);
            case "int[]":
            case "java.lang.Integer[]": return createTypeInfo(builderType, Integer[].class, Integer[].class);
            case "long[]":
            case "java.lang.Long[]": return createTypeInfo(builderType, Long[].class, Long[].class);
            case "boolean[]":
            case "java.lang.Boolean[]": return createTypeInfo(builderType, Boolean[].class, Boolean[].class);
            case "float[]":
            case "java.lang.Float[]": return createTypeInfo(builderType, Float[].class, Float[].class);
            case "double[]":
            case "java.lang.Double[]": return createTypeInfo(builderType, Double[].class, Double[].class);
            case "void[]":
            case "java.lang.Void[]": return createTypeInfo(builderType, Void[].class, Void[].class);
            case "char[]":
            case "java.lang.Character[]": return createTypeInfo(builderType, Character[].class, Character[].class);
            case "java.lang.BigInteger[]": return createTypeInfo(builderType, BigInteger[].class, BigInteger[].class);
            case "java.lang.BigDecimal[]": return createTypeInfo(builderType, BigDecimal[].class, BigDecimal[].class);
            default:
                try {
                    final Class<?> loadedNativeType = Class.forName(nativeType);
                    return createTypeInfo(builderType, loadedNativeType, loadedNativeType);
                }
                catch (final ClassNotFoundException e) {
                    return createTypeInfo(builderType, Object.class, String.class);
                }

        }
    }

    /**
     * Creates a new management entity type for the specified source-specific type.
     *
     * @param sourceType      The source-specific type.
     * @param destinationType The SNAMP-compliant type.
     * @return A new management entity type.
     */
    @Override
    public final E createTypeInfo(final Class<?> sourceType, final Class<?> destinationType) {
        return createTypeInfo(getClass(), entityType, sourceType, destinationType);
    }

    /**
     * Builds a new type converter for the specified management entity.
     * @param builderType Type of the type system. Cannot be {@literal null}.
     * @param entityType Type of the management entity for which the type converter should be constructed.
     * @param nativeType The native type of management entity in the management information base.
     * @param <E> Type of the management entity for which the type converter should be constructed.
     * @return A new type converter that can be used to convert entity values between MIB-specific and SNAMP ecosystem.
     */
    protected final static <E extends EntityTypeInfo> E createTypeInfo(final Class<? extends EntityTypeInfoBuilder> builderType, final Class<E> entityType, final String nativeType){
        return entityType.cast(createTypeInfo(builderType, nativeType));
    }

    /**
     * Creates a new type descriptor for the specified management entity.
     * @param builderType The type that holds conversion methods. Cannot be {@literal null}.
     * @param entityType The type of the management entity.
     * @param sourceType Source-specific attribute value type.
     * @param destinationType SNAMP-compliant attribute value type.
     * @param <E> Type of the management entity.
     * @return A new instance of the type descriptor.
     */
    protected final static <E extends EntityTypeInfo> E createTypeInfo(final Class<? extends EntityTypeInfoBuilder> builderType, final Class<E> entityType, final Class<?> sourceType, final Class<?> destinationType){
        return entityType.cast(createTypeInfo(builderType, sourceType, destinationType));
    }

    private static abstract class AbstractEntityTypeConverter implements EntityTypeConverter{
        /**
         * Represents an array of potential conversion methods.
         */
        private final Method[] converters;
        protected final Class<?> sourceType;
        private final Map<Class<?>, Method> convertersToCache;
        private final Map<Class<?>, Method> convertersFromCache;

        protected AbstractEntityTypeConverter(final Class<?> sourceType, final Method[] converters){
            this.converters = converters != null ? converters : new Method[0];
            this.sourceType = sourceType;
            this.convertersToCache = new HashMap<>(5);
            this.convertersFromCache = new HashMap<>(5);
        }

        private static final boolean isPublicStatic(final Method m){
            return (m.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) != 0;
        }

        private static final Object emptyConverter(final Object value){
            return value;
        }

        protected synchronized final Method getConverterTo(final Class<?> destinationType){
            if(convertersToCache.containsKey(destinationType))
                return convertersToCache.get(destinationType);
            else for(final Method candidate: converters)
                if(candidate.isAnnotationPresent(Converter.class) && isPublicStatic(candidate)){
                    final Class<?>[] parameters = candidate.getParameterTypes();
                    if(parameters.length == 1 &&
                            parameters[0].isAssignableFrom(sourceType) &&
                            destinationType.isAssignableFrom(candidate.getReturnType())){
                        convertersToCache.put(destinationType, candidate);
                        return candidate;
                    }
                }
            return null;
        }

        protected synchronized final Method getConverterFrom(final Class<?> fromType){
            if(convertersFromCache.containsKey(fromType))
                return convertersFromCache.get(fromType);
            else for(final Method candidate: converters)
                if(candidate.isAnnotationPresent(Converter.class) && isPublicStatic(candidate)){
                    final Class<?>[] parameters = candidate.getParameterTypes();
                    if(parameters.length == 1 &&
                            parameters[0].isAssignableFrom(fromType) &&
                            sourceType.isAssignableFrom(candidate.getReturnType())){
                        convertersFromCache.put(fromType, candidate);
                        return candidate;
                    }
                }
            return null;
        }

        /**
         * Determines whether the value of the specified type can be passed as attribute value.
         *
         * @param fromType The type of the value that can be converted to the attribute value.
         * @param <T>    The type of the value.
         * @return {@literal true}, if conversion from the specified type is supported; otherwise, {@literal false}.
         */
        @Override
        public final <T> boolean canConvertFrom(final Class<T> fromType) {
            return sourceType.isAssignableFrom(fromType) ||  getConverterFrom(fromType) != null;
        }

        /**
         * Converts the specified value into the
         *
         * @param value The value to convert.
         * @return The value of the attribute.
         * @throws IllegalArgumentException Conversion from the specified value is not supported.
         */
        @Override
        public final Object convertFrom(final Object value) throws IllegalArgumentException {
            if(value == null) throw new IllegalArgumentException(String.format("Cannot convert null to %s", sourceType));
            else if(sourceType.isInstance(value)) return value;
            final Method converter = getConverterFrom(value.getClass());
            if(converter != null)
                try {
                    return converter.invoke(null, value);
                }
                catch (final ReflectiveOperationException e) {
                    throw new IllegalArgumentException(e);
                }
            else throw new IllegalArgumentException(String.format("Unable to convert %s to %s", value, sourceType));
        }
    }

    private final static EntityTypeConverter createTypeInfo(final Class<? extends EntityTypeInfoBuilder> builderType, final Class<?> sourceType, final Class<?> destinationType){
        if(shouldNormalize(sourceType)) return createTypeInfo(builderType, normalizeClass(sourceType), destinationType);
        else if(shouldNormalize(destinationType)) return createTypeInfo(builderType, sourceType, normalizeClass(destinationType));
        //when source type is the same as conversion result type
        else if(destinationType.isAssignableFrom(sourceType))
            return new AbstractEntityTypeConverter(sourceType, builderType.getMethods()) {
                @Override
                public final <T> boolean canConvertTo(final Class<T> target) {
                    if(shouldNormalize(target)) return canConvertTo(normalizeClass(target));
                    return target.isAssignableFrom(super.sourceType) || destinationType.isAssignableFrom(target);
                }

                @Override
                public final <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException{
                    if(shouldNormalize(target))
                        return (T)convertTo(value, normalizeClass(target));
                    if(target.isInstance(value))
                        return target.cast(value);
                    else if(sourceType.isInstance(value))
                        if(destinationType.isAssignableFrom(target))
                            return target.cast(value);
                        else throw new IllegalArgumentException(String.format("Actual type % is not compatible with expected type %s", target, destinationType));
                    else throw new IllegalArgumentException(String.format("The value %s is not of %s type.", value, sourceType));
                }
            };
         //when source type differs from destination type
        else return new AbstractEntityTypeConverter(sourceType, builderType.getMethods()) {
                @Override
                public final  <T> boolean canConvertTo(final Class<T> target) {
                    if(target == null) return false;
                    else if(target.isAssignableFrom(super.sourceType))
                        return true;
                    else if(getConverterTo(target) != null)
                        return true;
                    else return false;
                }

                @Override
                public final  <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException {
                    if(target == null) throw new IllegalArgumentException("target is null.");
                    else if(shouldNormalize(target)) return (T)convertTo(value, normalizeClass(target));
                    else if(target.isInstance(value)) return target.cast(value);
                    final Method converter = getConverterTo(target);
                    if(converter != null)
                        try {
                            return target.cast(converter.invoke(null, value));
                        }
                        catch (final ReflectiveOperationException e) {
                            throw  new IllegalArgumentException(e);
                        }
                    else throw new IllegalArgumentException(String.format("Cannot convert %s to %s", value, target));
                }
            };
    }

    private static final boolean shouldNormalize(final Class<?> classInfo){
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
     * Determines whether the specified entity type describes the Java type.
     * @param entityType The type of the management entity that is convertible into JVM-compliant type.
     * @param typeToCompare JVM-compliant type.
     * @return {@literal true} if underlying entity type is equal to the specified Java type; otherwise, {@literal false}.
     */
    public static boolean isTypeOf(final EntityTypeInfo entityType, final Class<?> typeToCompare){
        return entityType != null && entityType.canConvertTo(typeToCompare);
    }
}
