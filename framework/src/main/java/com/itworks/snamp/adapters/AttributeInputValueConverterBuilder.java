package com.itworks.snamp.adapters;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import java.util.Objects;

/**
 * Represents attribute input value converter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class AttributeInputValueConverterBuilder<O> implements Supplier<AttributeInputValueConverter<O>> {
    private final ImmutableMap.Builder<TypeToken<?>, Function> converters = ImmutableMap.builder();

    /**
     * Constructs a new attribute value converter.
     * @return A new attribute value converter.
     */
    public final AttributeInputValueConverter<O> build(){
        return new AttributeInputValueConverter<O>() {
            private final ImmutableMap<TypeToken<?>, Function> converters = AttributeInputValueConverterBuilder.this.converters.build();

            @SuppressWarnings("unchecked")
            @Override
            public <I> Function<? super I, ? extends O> getConverter(final TypeToken<I> attributeType) {
                return converters.containsKey(attributeType)?
                        (Function<I, O>) converters.get(attributeType):
                        null;
            }
        };
    }

    /**
     * Registers a new converter.
     * @param attributeType The attribute type to convert.
     * @param converter The attribute converter.
     * @param <I> Type of the attribute value.
     * @return This builder.
     */
    public final  <I> AttributeInputValueConverterBuilder<O> register(final TypeToken<I> attributeType,
                                                           final Function<? super I, ? extends O> converter){
        converters.put(Objects.requireNonNull(attributeType, "attributeType is null."),
                Objects.requireNonNull(converter, "converter is null."));
        return this;
    }

    /**
     * Registers a new identity converter.
     * @param attributeType The attribute type.
     * @return This builder.
     */
    public final AttributeInputValueConverterBuilder<O> register(final TypeToken<O> attributeType){
        return register(attributeType, Functions.<O>identity());
    }

    /**
     * Constructs a new attribute value converter.
     * @return A new attribute value converter.
     */
    @Override
    public final AttributeInputValueConverter<O> get() {
        return build();
    }
}
