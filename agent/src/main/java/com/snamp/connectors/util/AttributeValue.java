package com.snamp.connectors.util;

import com.snamp.*;
import com.snamp.connectors.ManagementEntityType;

/**
 * Represents utility class that represents raw attribute value and its type.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public final class AttributeValue<T extends ManagementEntityType> {
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
        return type.getProjection(target) != null;
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
        if(target == null) throw new IllegalArgumentException("target is null.");
        else if(target.isInstance(rawValue)) return target.cast(rawValue);
        else {
            final TypeConverter<G> converter = type.getProjection(target);
            if(converter == null) throw new IllegalArgumentException(String.format("Type %s is not supported", target));
            return converter.convertFrom(rawValue);
        }
    }

    /**
     * Determines whether the current attribute type is compliant with the specified attribute type.
     * @param attributeType The attribute type to check.
     * @return {@literal true}, if the current attribute type is compliant with the specified attribute type; otherwise, {@literal false}.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final boolean isTypeOf(final Class<? extends ManagementEntityType> attributeType){
        return attributeType.isInstance(type);
    }

    /**
     * Casts this attribute value into the new instance with new attribute
     * @param attributeType A new attribute type for upper type casting.
     * @param <G> A new attribute type for upper type casting.
     * @return A new instance of attribute value. Field {@link #rawValue} will not be changed.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final <G extends ManagementEntityType> AttributeValue<G> cast(final Class<G> attributeType){
        return new AttributeValue<>(rawValue, attributeType.cast(type));
    }
}
