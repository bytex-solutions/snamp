package com.snamp.connectors.util;

import com.snamp.*;
import com.snamp.connectors.AttributeTypeInfo;

/**
 * Represents utility class that represents raw attribute value and its type.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public final class AttributeValue<T extends AttributeTypeInfo> {
    /**
     * Represents attribute value.
     */
    public final Object rawValue;

    /**
     * Represents attribute type.
     */
    public final T type;

    /**
     * Initializes a new attribute value.
     * @param attributeValue The value of the management attribute.
     * @param attributeType The type of the management attribute.
     * @throws IllegalArgumentException attributeType is {@literal null}.
     */
    public AttributeValue(final Object attributeValue, final T attributeType){
        if(attributeType == null) throw new IllegalArgumentException("attributeType is null.");
        rawValue = attributeValue;
        type = attributeType;
    }


    /**
     * Determines whether the attribute value can be converted into the specified type.
     *
     * @param target The result of the conversion.
     * @param <G>    The type of the conversion result.
     * @return {@literal true}, if conversion to the specified type is supported.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final <G> boolean canConvertTo(final Class<G> target) {
        return type.canConvertTo(target);
    }

    /**
     * Converts the attribute value to the specified type.
     *
     * @param target The type of the conversion result.
     * @param <G>    Type of the conversion result.
     * @return The conversion result.
     * @throws IllegalArgumentException The target type is not supported.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final <G> G convertTo(final Class<G> target) throws IllegalArgumentException {
        return type.convertTo(rawValue, target);
    }

    /**
     * Determines whether the value of the specified type can be passed as attribute value.
     *
     * @param source The type of the value that can be converted to the attribute value.
     * @param <G>    The type of the value.
     * @return {@literal true}, if conversion from the specified type is supported; otherwise, {@literal false}.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final <G> boolean canConvertFrom(final Class<G> source) {
        return type.canConvertFrom(source);
    }

    /**
     * Determines whether the current attribute type is compliant with the specified attribute type.
     * @param attributeType The attribute type to check.
     * @return {@literal true}, if the current attribute type is compliant with the specified attribute type; otherwise, {@literal false}.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final boolean isTypeOf(final Class<? extends AttributeTypeInfo> attributeType){
        return attributeType.isInstance(type);
    }

    /**
     * Casts this attribute value into the new instance with new attribute
     * @param attributeType A new attribute type for upper type casting.
     * @param <G> A new attribute type for upper type casting.
     * @return A new instance of attribute value. Field {@link #rawValue} will not be changed.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final <G extends AttributeTypeInfo> AttributeValue<G> cast(final Class<G> attributeType){
        return new AttributeValue<G>(rawValue, attributeType.cast(type));
    }
}
