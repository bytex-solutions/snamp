package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.TypeLiterals;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.Typed;
import org.snmp4j.smi.IpAddress;

import java.util.Map;

import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.SNMP_CONVERSION_FORMAT;

/**
 * Represents {@link org.snmp4j.smi.IpAddress} conversion format.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum IpAddressConversionFormat {
    TEXT,
    BYTE_ARRAY;

    static final Typed<IpAddress> IP_ADDRESS = TypeLiterals.of(IpAddress.class);

    public static IpAddressConversionFormat getFormat(final Map<String, String> options){
        if(options.containsKey(SNMP_CONVERSION_FORMAT))
            return getFormat(options.get(SNMP_CONVERSION_FORMAT));
        else return BYTE_ARRAY;
    }

    public static IpAddressConversionFormat getFormat(final String formatName){
        switch (formatName){
            case "text": return TEXT;
            default: return BYTE_ARRAY;
        }
    }

    public SMITypeProjection<IpAddress, ?> createTypeProjection(){
        switch (this){
            case TEXT: return new SMITypeProjection<IpAddress, String>(IP_ADDRESS, TypeLiterals.STRING) {
                @Override
                protected String convertFrom(final IpAddress value) throws IllegalArgumentException {
                    return value.toString();
                }
            };
            default: return new SMITypeProjection<IpAddress, Object[]>(IP_ADDRESS, TypeLiterals.OBJECT_ARRAY) {
                @Override
                protected Byte[] convertFrom(final IpAddress value) throws IllegalArgumentException {
                    return ArrayUtils.toObject(value.toByteArray());
                }
            };
        }
    }
}
