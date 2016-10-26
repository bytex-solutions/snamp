package com.bytex.snamp.connector.md;

import com.bytex.snamp.parser.NameToken;
import com.bytex.snamp.parser.ParseException;
import com.bytex.snamp.parser.Tokenizer;

/**
 * Represents parser of attributes.
 * @since 2.0
 * @version 2.0
 */
final class AttributeParser {
    private AttributeParser(){
        throw new InstantiationError();
    }

    private static MessageDrivenAttributeFactory parseAttribute(final Tokenizer tokenizer) throws ParseException{
        final NameToken token = tokenizer.nextToken(NameToken.class);
        switch (token.toString()){
            case Gauge64Attribute.NAME:
                return Gauge64Attribute::new;
            default:
                throw new UnrecognizedAttributeTypeException(tokenizer.getSource().toString());
        }
    }

    static MessageDrivenAttributeFactory parseAttribute(final String attributeType) throws ParseException {
        try(final Tokenizer tokenizer = new Tokenizer(attributeType)){
            return parseAttribute(tokenizer);
        }
    }
}
