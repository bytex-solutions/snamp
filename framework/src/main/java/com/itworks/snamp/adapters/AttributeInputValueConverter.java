package com.itworks.snamp.adapters;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;

/**
 * Represents attribute value converter that can be used to convert
 * attribute value into adapter-specified value.
 * @param <O> Type of the adapter-specific value.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeInputValueConverter<O> {
    /**
     * Gets converter for the specified attribute type.
     * @param attributeType The attribute type.
     * @param <I> Type of the attribute value.
     * @return The adapter-specific value.
     */
    <I> Function<? super I, ? extends O> getConverter(final TypeToken<I> attributeType);
}
