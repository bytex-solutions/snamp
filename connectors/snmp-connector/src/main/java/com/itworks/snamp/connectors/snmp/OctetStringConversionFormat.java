package com.itworks.snamp.connectors.snmp;

import org.apache.commons.lang3.ArrayUtils;
import org.snmp4j.smi.OctetString;

import java.util.Map;

import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.SNMP_CONVERSION_FORMAT;

/**
 * Represents {@link org.snmp4j.smi.OctetString} format type.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum OctetStringConversionFormat {
    /**
     * Represents octet string as text.
     */
    TEXT,

    /**
     * Hexadecimal representation of the octet string.
     */
    HEX,

    /**
     * As byte array.
     */
    BYTE_ARRAY;

    public static OctetStringConversionFormat getFormat(final OctetString value, final Map<String, String> options){
        if(options.containsKey(SNMP_CONVERSION_FORMAT))
            return getFormat(options.get(SNMP_CONVERSION_FORMAT));
        else if(value.isPrintable()) return TEXT;
        else return BYTE_ARRAY;
    }

    public static OctetStringConversionFormat getFormat(final String formatName){
        switch (formatName){
            case "text": return TEXT;
            case "hex": return HEX;
            default: return BYTE_ARRAY;
        }
    }

    public SMITypeProjection<OctetString, ?> createTypeProjection(){
        switch (this){
            case HEX: return new SMITypeProjection<OctetString, String>(OctetString.class, String.class) {
                @Override
                protected String convertFrom(final OctetString value) throws IllegalArgumentException {
                    return value.toHexString();
                }
            };
            case TEXT: return new SMITypeProjection<OctetString, String>(OctetString.class, String.class) {
                @Override
                protected String convertFrom(final OctetString value) throws IllegalArgumentException {
                    return new String(value.getValue());
                }
            };
            default: return new SMITypeProjection<OctetString, Object[]>(OctetString.class, Object[].class) {
                @Override
                protected Byte[] convertFrom(final OctetString value) throws IllegalArgumentException {
                    return ArrayUtils.toObject(value.getValue());
                }
            };
        }
    }
}
