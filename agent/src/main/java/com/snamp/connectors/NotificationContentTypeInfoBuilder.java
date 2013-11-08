package com.snamp.connectors;

/**
 * Represents a builder for notification content types.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class NotificationContentTypeInfoBuilder extends EntityTypeInfoBuilder {
    /**
     * Initializes a new instance of the notification content type builder.
     */
    protected NotificationContentTypeInfoBuilder(){

    }

    /**
     * Creates a new {@link com.snamp.connectors.EntityTypeInfoBuilder.AttributeConvertibleTypeInfo} for the specified native type using the specified builder.
     * @param builderType A type that contains converters (as static methods).
     * @param nativeType The underlying Java type.
     * @return
     */
    public static NotificationContentJavaTypeInfo<?> createTypeInfo(final Class<? extends AttributeTypeInfoBuilder> builderType, final String nativeType){
        return createTypeInfo(builderType, NotificationContentJavaTypeInfo.class, nativeType);
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
