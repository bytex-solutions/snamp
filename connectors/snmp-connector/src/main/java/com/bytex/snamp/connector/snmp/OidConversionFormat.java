package com.bytex.snamp.connector.snmp;

import org.snmp4j.smi.OID;

import javax.management.Descriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import static com.bytex.snamp.connector.snmp.SnmpConnectorDescriptionProvider.SNMP_CONVERSION_FORMAT_PARAM;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.hasField;

/**
 * Represents {@link org.snmp4j.smi.OID} conversion format.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
enum OidConversionFormat implements SnmpObjectConverter<OID> {
    TEXT(SimpleType.STRING) {
        @Override
        public String convert(final OID value) {
            return value.toDottedString();
        }
    },
    INT_ARRAY(SnmpConnectorHelpers.arrayType(SimpleType.INTEGER, true)) {
        @Override
        public int[] convert(final OID value) {
            return value.toIntArray();
        }
    };

    private final OpenType<?> openType;

    OidConversionFormat(final OpenType<?> type){
        this.openType = type;
    }

    @Override
    public OID convert(final Object value) throws InvalidAttributeValueException {
        if(value instanceof int[])
            return new OID((int[])value);
        else if(value instanceof String)
            return new OID((String)value);
        else throw new InvalidAttributeValueException(String.format("Unable convert %s to OID", value));
    }

    static OidConversionFormat getFormat(final Descriptor options){
        if(hasField(options, SNMP_CONVERSION_FORMAT_PARAM))
            return getFormat(getField(options, SNMP_CONVERSION_FORMAT_PARAM, String.class));
        else return INT_ARRAY;
    }

    static OidConversionFormat getFormat(final String formatName){
        switch (formatName){
            case "text": return TEXT;
            default: return INT_ARRAY;
        }
    }

    @Override
    public final OpenType<?> getOpenType() {
        return openType;
    }
}
