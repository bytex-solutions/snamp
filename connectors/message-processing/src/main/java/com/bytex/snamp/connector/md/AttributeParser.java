package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.parser.ParseException;
import com.bytex.snamp.parser.Tokenizer;

import java.util.function.Function;

/**
 * Represents parser of attributes.
 * @since 2.0
 */
final class AttributeParser {
    private AttributeParser(){
        throw new InstantiationError();
    }

    private static Function<? super String, ? extends MessageDrivenAttribute<?>> parseAttribute(final Tokenizer tokenizer) throws ParseException{
        return null;
    }

    private static Function<? super String, ? extends MessageDrivenAttribute<?>> parseAttribute(final String attributeType) throws ParseException {
        try(final Tokenizer tokenizer = new Tokenizer(attributeType)){
            return parseAttribute(tokenizer);
        }
    }

    static Function<? super String, ? extends MessageDrivenAttribute<?>> parseAttribute(final MessageDrivenConnectorConfigurationDescriptor descriptionProvider,
                                                                                        final AttributeDescriptor descriptor) throws MessageDrivenConnectorAbsentConfigurationParameterException, ParseException {
        return parseAttribute(descriptionProvider.parseAttributeType(descriptor));
    }
}
