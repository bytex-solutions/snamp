package com.itworks.snamp.adapters;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

/**
 * Represents attribute output value converter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class AttributeOutputValueConverterBuilder<I> implements Supplier<AttributeOutputValueConverter<I>> {
    private final ImmutableMap.Builder<TypeToken<?>, Function> converters = ImmutableMap.builder();

    /**
     * Registers a new attribute value converter.
     * @param token The well-known attribute type.
     * @param converter The adapter-specific value converter.
     * @param <O> The well-known attribute type.
     * @return This builder.
     */
    public final <O> AttributeOutputValueConverterBuilder<I> register(final TypeToken<O> token,
                                                                      final Function<? super I, ? extends O> converter){
        converters.put(token, converter);
        return this;
    }

    public final AttributeOutputValueConverterBuilder<I> register(final TypeToken<I> token){
        return register(token, Functions.<I>identity());
    }

    private static <I> AttributeOutputValueConverter<I> build(final ImmutableMap<TypeToken<?>, Function> converters){
        return new AttributeOutputValueConverter<I>() {
            @SuppressWarnings("unchecked")
            @Override
            public <O> Function<? super I, ? extends O> getConverter(final TypeToken<O> attributeType) {
                return converters.containsKey(attributeType) ?
                        (Function<I, O>) converters.get(attributeType) :
                        null;
            }
        };
    }

    /**
     * Constructs a new attribute value converter.
     * @return A new attribute value converter.
     */
    public final AttributeOutputValueConverter<I> build(){
        return build(converters.build());
    }

    /**
     * Constructs a new attribute value converter.
     * @return A new attribute value converter.
     */
    @Override
    public final AttributeOutputValueConverter<I> get() {
        return build();
    }
}
