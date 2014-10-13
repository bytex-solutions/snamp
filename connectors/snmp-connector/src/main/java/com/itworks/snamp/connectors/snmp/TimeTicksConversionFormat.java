package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.TypeLiterals;
import org.apache.commons.lang3.reflect.Typed;
import org.snmp4j.smi.TimeTicks;

import java.util.Map;

import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.SNMP_CONVERSION_FORMAT;

/**
 * Represents {@link org.snmp4j.smi.TimeTicks} conversion format.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum TimeTicksConversionFormat {
    TEXT,
    LONG;

    static final Typed<TimeTicks> TIME_TICKS = TypeLiterals.of(TimeTicks.class);

    public static TimeTicksConversionFormat getFormat(final Map<String, String> options){
        if(options.containsKey(SNMP_CONVERSION_FORMAT))
            return getFormat(options.get(SNMP_CONVERSION_FORMAT));
        else return LONG;
    }

    public static TimeTicksConversionFormat getFormat(final String formatName){
        switch (formatName){
            case "text": return TEXT;
            default: return LONG;
        }
    }

    public SMITypeProjection<TimeTicks, ?> createTypeProjection(){
        switch (this){
            case TEXT: return new SMITypeProjection<TimeTicks, String>(TIME_TICKS, TypeLiterals.STRING) {
                @Override
                protected String convertFrom(final TimeTicks value) throws IllegalArgumentException {
                    return value.toString();
                }
            };
            default: return new SMITypeProjection<TimeTicks, Long>(TIME_TICKS, TypeLiterals.LONG) {
                @Override
                protected Long convertFrom(final TimeTicks value) throws IllegalArgumentException {
                    return value.toLong();
                }
            };
        }
    }
}
