package com.snamp.connectors;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.math.*;
import java.util.*;

/**
 * Represents builder for {@link AttributeTypeInfo} instances.
 * <p>
 * This class represents a bridge between management information base data types and MIB-free
 * type system that can be interpreted in right way by SNAMP connector and adapter both.
 * </p>
 * <p>
 * Attribute type information is a set of converters that provides conversion between MIB-specific
 * data types and universal data types. This class provides set of converters between these data types
 * in the form of static public unary methods annotated with {@link Converter} interface. Typically,
 * each custom SNAMP connector contains its own type system converter, inherited from this class.
 * The following example demonstrates your own type system converter:
 * <pre>{@code
 * public final class CustomTypeInfoBuilder extends AttributeTypeInfoBuilder{
 *     @Converter
 *     public static byte[] stringToByteArray(final String str){
 *         return str.getBytes("UTF-8");
 *     }
 *
 *     @Converter
 *     public static String byteArrayToString(final byte[] b){
 *         return new String(b, "UTF-8");
 *     }
 *
 *     public static final AttributeConvertibleTypeInfo<byte[]> createByteArrayType(final Class<? extends AttributeTypeInfoBuilder> ts){
 *       return createTypeInfo(ts, byte[].class);
 *     }
 *
 *     public AttributeConvertibleTypeInfo<byte[]> createByteArrayType(){
 *       return createByteArrayType(getClass());
 *     }
 * }
 * }</pre>
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AttributeTypeInfoBuilder extends EntityTypeInfoBuilder {
    /**
     * Initializes a new attribute type info builder.
     */
    protected AttributeTypeInfoBuilder(){

    }

    /**
     * Creates a new {@link com.snamp.connectors.EntityTypeInfoBuilder.AttributeConvertibleTypeInfo} for the specified
     * native type using the specified builder.
     * @param builderType A type that contains converters (as static methods).
     * @param nativeType The name of the underlying Java type.
     * @return A new instance of the attribute type descriptor.
     */
    public final static AttributeConvertibleTypeInfo<?> createTypeInfo(final Class<? extends AttributeTypeInfoBuilder> builderType, final String nativeType){
        return createTypeInfo(builderType, AttributeConvertibleTypeInfo.class, nativeType);
    }

    /**
     * Creates a new {@link com.snamp.connectors.EntityTypeInfoBuilder.AttributeConvertibleTypeInfo} for the specified
     * native type using the specified builder.
     * @param builderType
     * @param nativeType
     * @param <T> Underlying Java type.
     * @return A new instance of the attribute type descriptor.
     */
    public final static <T> AttributeConvertibleTypeInfo<T> createTypeInfo(final Class<? extends EntityTypeInfoBuilder> builderType, final Class<T> nativeType){
        return createTypeInfo(builderType, AttributeConvertibleTypeInfo.class, nativeType);
    }

        /**
         * Creates a new {@link com.snamp.connectors.AttributeTypeInfoBuilder.AttributeConvertibleTypeInfo} instance.
         * @param nativeType The Java type that should be wrapped into attribute type.
         * @return An instance of the attribute type converter.
         */
    public final <T> AttributeConvertibleTypeInfo<T> createTypeInfo(final Class<T> nativeType){
        return createTypeInfo(getClass(), AttributeConvertibleTypeInfo.class, nativeType);
    }


}
