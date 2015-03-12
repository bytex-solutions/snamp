package com.itworks.snamp.adapters;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;

/**
 * Represents attribute value converter that can be used to convert
 * adapter-specific value into the attribute value.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeOutputValueConverter<I> {
    /**
     * Gets adapter-specific value converter.
     * @param attributeType The type of the attribute.
     * @param <O> The type of the attribute.
     * @return The attribute value converter.
     */
    <O> Function<? super I, ? extends O> getConverter(final TypeToken<O> attributeType);
}
