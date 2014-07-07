package com.itworks.snamp.connectors.snmp;

import org.apache.commons.lang3.ArrayUtils;
import org.snmp4j.smi.OID;

import java.util.Map;

import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.SNMP_CONVERSION_FORMAT;

/**
 * Represents {@link org.snmp4j.smi.OID} conversion format.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum OidConversionFormat {
    TEXT,
    INT_ARRAY;

    public static OidConversionFormat getFormat(final Map<String, String> options){
        if(options.containsKey(SNMP_CONVERSION_FORMAT))
            return getFormat(options.get(SNMP_CONVERSION_FORMAT));
        else return INT_ARRAY;
    }

    public static OidConversionFormat getFormat(final String formatName){
        switch (formatName){
            case "text": return TEXT;
            default: return INT_ARRAY;
        }
    }

    public SMITypeProjection<OID, ?> createTypeProjection(){
        switch (this){
            case TEXT: return new SMITypeProjection<OID, String>(OID.class, String.class) {
                @Override
                protected String convertFrom(final OID value) throws IllegalArgumentException {
                    return value.toDottedString();
                }
            };
            default: return new SMITypeProjection<OID, Object[]>(OID.class, Object[].class) {
                @Override
                protected Integer[] convertFrom(final OID value) throws IllegalArgumentException {
                    return ArrayUtils.toObject(value.getValue());
                }
            };
        }
    }
}
