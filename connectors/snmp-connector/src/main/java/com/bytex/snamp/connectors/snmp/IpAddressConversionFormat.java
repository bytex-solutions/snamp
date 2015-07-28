package com.bytex.snamp.connectors.snmp;

import org.snmp4j.smi.IpAddress;

import javax.management.Descriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import static com.bytex.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.SNMP_CONVERSION_FORMAT_PARAM;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.hasField;

/**
 * Represents {@link org.snmp4j.smi.IpAddress} conversion format.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum IpAddressConversionFormat implements SnmpObjectConverter<IpAddress> {
    TEXT(SimpleType.STRING) {

        @Override
        public Object convert(final IpAddress value) {
            return value.toString();
        }
    },
    BYTE_ARRAY(SnmpConnectorHelpers.arrayType(SimpleType.BYTE, true)) {
        @Override
        public Object convert(final IpAddress value) {
            return value.toByteArray();
        }
    };

    private final OpenType<?> openType;

    IpAddressConversionFormat(final OpenType<?> type){
        this.openType = type;
    }

    @Override
    public IpAddress convert(final Object value) throws InvalidAttributeValueException {
        if(value instanceof String)
            return new IpAddress((String)value);
        else if(value instanceof byte[])
            return new IpAddress((byte[])value);
        else throw new InvalidAttributeValueException(String.format("Unable convert %s to IP Address", value));
    }

    @Override
    public final OpenType<?> getOpenType() {
        return openType;
    }

    static IpAddressConversionFormat getFormat(final Descriptor options){
        if(hasField(options, SNMP_CONVERSION_FORMAT_PARAM))
            return getFormat(getField(options, SNMP_CONVERSION_FORMAT_PARAM, String.class));
        else return BYTE_ARRAY;
    }

    static IpAddressConversionFormat getFormat(final String formatName){
        switch (formatName){
            case "text": return TEXT;
            default: return BYTE_ARRAY;
        }
    }

}