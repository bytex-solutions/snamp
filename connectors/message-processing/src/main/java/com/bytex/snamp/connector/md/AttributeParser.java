package com.bytex.snamp.connector.md;

import com.bytex.snamp.parser.NameToken;
import com.bytex.snamp.parser.ParseException;
import com.bytex.snamp.parser.Tokenizer;

import javax.management.openmbean.CompositeType;

/**
 * Represents parser of attributes.
 * @since 2.0
 * @version 2.0
 */
final class AttributeParser extends Tokenizer {
    private static final NameToken OF_KEYWORD = new NameToken("of");

    private AttributeParser(final String input){
        super(input);
    }

    private static MessageDrivenAttributeFactory createExtractionAttribute(final String fieldName,
                                                                           final String gaugeType,
                                                                           final String sourceAttribute) throws ParseException {
        final CompositeType metricType;
        switch (gaugeType) {
            case Gauge64Attribute.NAME:
                metricType = Gauge64Attribute.TYPE;
                break;
            default:
                throw new UnrecognizedGaugeTypeException(gaugeType);
        }
        if(metricType.containsKey(fieldName))
            return (name, descriptor) -> new DecomposerAttribute(name, sourceAttribute, fieldName, metricType, descriptor);
        else
            throw new IncorrectGaugeOperatorException(gaugeType, fieldName);
    }

    private MessageDrivenAttributeFactory parseExtractionAttribute() throws ParseException {
        //get <extraction-operator> of <gauge-type> <source-attribute>
        final NameToken operator = nextToken(NameToken.class);
        nextToken(OF_KEYWORD::equals);
        final NameToken gaugeType = nextToken(NameToken.class);
        final NameToken sourceAttribute = nextToken(NameToken.class);
        //operator name depends on gauge type
        return createExtractionAttribute(operator.toString(), gaugeType.toString(), sourceAttribute.toString());
    }

    private MessageDrivenAttributeFactory parse() throws ParseException{
        final NameToken token = nextToken(NameToken.class);
        switch (token.toString()){
            case Gauge64Attribute.NAME:
                return Gauge64Attribute::new; //gauge64
            case "get":
                return parseExtractionAttribute();
            default:
                throw new UnrecognizedAttributeTypeException(getSource().toString());
        }
    }

    static MessageDrivenAttributeFactory parseAttribute(final String attributeType) throws ParseException {
        try(final AttributeParser parser = new AttributeParser(attributeType)){
            return parser.parse();
        }
    }
}
