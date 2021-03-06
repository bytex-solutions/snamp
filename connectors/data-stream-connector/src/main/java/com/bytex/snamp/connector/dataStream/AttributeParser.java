package com.bytex.snamp.connector.dataStream;

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
    private static final NameToken FROM_KEYWORD = new NameToken("from");

    private AttributeParser(final String input){
        super(input);
    }

    private static SyntheticAttributeFactory createExtractionAttribute(final String fieldName,
                                                                       final String gaugeType,
                                                                       final String sourceAttribute) throws ParseException {
        final CompositeType metricType;
        switch (gaugeType) {
            case Gauge64Attribute.NAME:
                metricType = Gauge64Attribute.TYPE;
                break;
            case GaugeFPAttribute.NAME:
                metricType = GaugeFPAttribute.TYPE;
                break;
            case FlagAttribute.NAME:
                metricType = FlagAttribute.TYPE;
                break;
            case StringGaugeAttribute.NAME:
                metricType = StringGaugeAttribute.TYPE;
                break;
            case TimerAttribute.NAME:
                metricType = TimerAttribute.TYPE;
                break;
            case RangedGauge64Attribute.NAME:
                metricType = RangedGauge64Attribute.TYPE;
                break;
            case RangedGaugeFPAttribute.NAME:
                metricType = RangedGaugeFPAttribute.TYPE;
                break;
            case RangedTimerAttribute.NAME:
                metricType = RangedTimerAttribute.TYPE;
                break;
            case ArrivalsAttribute.NAME:
                metricType = ArrivalsAttribute.TYPE;
                break;
            case NotificationRateAttribute.NAME:
                metricType = NotificationRateAttribute.TYPE;
                break;
            default:
                throw new UnrecognizedGaugeTypeException(gaugeType);
        }
        if(metricType.containsKey(fieldName))
            return (name, descriptor) -> new DecomposerAttribute(name, sourceAttribute, fieldName, metricType, descriptor);
        else
            throw new IncorrectGaugeOperatorException(gaugeType, fieldName);
    }

    private SyntheticAttributeFactory parseExtractionAttribute() throws ParseException {
        //get <extraction-operator> from <gauge-type> <source-attribute>
        final NameToken operator = nextToken(NameToken.class);
        nextToken(FROM_KEYWORD::equals);
        final NameToken gaugeType = nextToken(NameToken.class);
        //parse attribute name
        final NameToken sourceAttribute = nextToken(NameToken.class);
        //operator name depends on gauge type
        return createExtractionAttribute(operator.toString(), gaugeType.toString(), sourceAttribute.toString());
    }

    private SyntheticAttributeFactory parse() throws ParseException{
        final NameToken token = nextToken(NameToken.class);
        switch (token.toString()){
            case Gauge64Attribute.NAME:
                return Gauge64Attribute::new;   //gauge64
            case GaugeFPAttribute.NAME:
                return GaugeFPAttribute::new;   //gaugeFP
            case FlagAttribute.NAME:
                return FlagAttribute::new;      //flag
            case StringGaugeAttribute.NAME:
                return StringGaugeAttribute::new;   //stringGauge
            case TimerAttribute.NAME:
                return TimerAttribute::new;         //timer
            case RangedGauge64Attribute.NAME:
                return RangedGauge64Attribute::new;     //rangedGauge64
            case RangedGaugeFPAttribute.NAME:
                return RangedGaugeFPAttribute::new;     //rangeGaugeFP
            case RangedTimerAttribute.NAME:
                return RangedTimerAttribute::new;       //rangedTimer
            case ArrivalsAttribute.NAME:
                return ArrivalsAttribute::new;          //arrivals
            case NotificationRateAttribute.NAME:
                return NotificationRateAttribute::new;  //rate of notifications
            case "get":
                return parseExtractionAttribute();
            default:
                throw new UnrecognizedAttributeTypeException(getSource().toString());
        }
    }

    static SyntheticAttributeFactory parse(final String gaugeType) throws ParseException {
        try(final AttributeParser parser = new AttributeParser(gaugeType)){
            return parser.parse();
        }
    }
}
