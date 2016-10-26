package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import java.util.function.Function;

/**
 * Represents parser of attributes.
 * @since 2.0
 */
final class AttributeParser {
    private AttributeParser(){
        throw new InstantiationError();
    }

    static Function<? super String, ? extends MessageDrivenAttribute<?>> parseAttribute(final AttributeDescriptor descriptor){
        return null;
    }
}
