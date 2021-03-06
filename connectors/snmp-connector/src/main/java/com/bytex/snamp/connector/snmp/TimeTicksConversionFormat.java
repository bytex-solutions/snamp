package com.bytex.snamp.connector.snmp;

import org.snmp4j.smi.TimeTicks;

import javax.management.Descriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.Objects;

import static com.bytex.snamp.connector.snmp.SnmpConnectorDescriptionProvider.SNMP_CONVERSION_FORMAT_PARAM;
import static com.bytex.snamp.jmx.DescriptorUtils.parseStringField;

/**
 * Represents {@link org.snmp4j.smi.TimeTicks} conversion format.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
enum TimeTicksConversionFormat implements SnmpObjectConverter<TimeTicks> {
    TEXT(SimpleType.STRING) {
        @Override
        public String convert(final TimeTicks value) {
            return value.toString();
        }
    },
    LONG(SimpleType.LONG) {
        @Override
        public Long convert(final TimeTicks value) {
            return value.toLong();
        }
    };

    private final OpenType<?> openType;

    TimeTicksConversionFormat(final OpenType<?> type){
        this.openType = type;
    }




    static TimeTicksConversionFormat getFormat(final Descriptor options){
        return parseStringField(options, SNMP_CONVERSION_FORMAT_PARAM, TimeTicksConversionFormat::getFormat).orElse(LONG);
    }

    static TimeTicksConversionFormat getFormat(final String formatName){
        switch (formatName){
            case "text": return TEXT;
            default: return LONG;
        }
    }

    @Override
    public TimeTicks convert(final Object value) throws InvalidAttributeValueException {
        if(value instanceof Long)
            return new TimeTicks((Long)value);
        else{
            final TimeTicks result = new TimeTicks();
            result.setValue(Objects.toString(value, "0"));
            return result;
        }
    }

    @Override
    public final OpenType<?> getOpenType() {
        return openType;
    }
}
