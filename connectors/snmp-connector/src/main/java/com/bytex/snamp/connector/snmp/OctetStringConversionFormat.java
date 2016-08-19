package com.bytex.snamp.connector.snmp;

import org.snmp4j.smi.OctetString;

import javax.management.Descriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.Objects;

import static com.bytex.snamp.connector.snmp.SnmpConnectorDescriptionProvider.SNMP_CONVERSION_FORMAT_PARAM;
import static com.bytex.snamp.jmx.DescriptorUtils.parseStringField;

/**
 * Represents {@link org.snmp4j.smi.OctetString} format type.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
enum OctetStringConversionFormat implements SnmpObjectConverter<OctetString> {
    /**
     * Represents octet string as text.
     */
    TEXT(SimpleType.STRING) {
        @Override
        public OctetString convert(final Object value) {
            return new OctetString(Objects.toString(value, "").getBytes(SNMP_ENCODING));
        }

        @Override
        public String convert(final OctetString value) {
            return new String(value.toByteArray(), SNMP_ENCODING);
        }
    },

    /**
     * Hexadecimal representation of the octet string.
     */
    HEX(SimpleType.STRING) {
        @Override
        public OctetString convert(final Object value) {
            return OctetString.fromHexString(Objects.toString(value, ""));
        }

        @Override
        public Object convert(final OctetString value) {
            return value.toHexString();
        }
    },

    /**
     * As byte array.
     */
    BYTE_ARRAY(SnmpConnectorHelpers.arrayType(SimpleType.BYTE, true)) {
        @Override
        public OctetString convert(final Object value) throws InvalidAttributeValueException {
            return value instanceof byte[] ?
                OctetString.fromByteArray((byte[])value):
                new OctetString(Objects.toString(value, ""));
        }

        @Override
        public Object convert(final OctetString value) {
            return value.toByteArray();
        }
    };

    private final OpenType<?> openType;

    OctetStringConversionFormat(final OpenType<?> type){
        this.openType = type;
    }

    static OctetStringConversionFormat getFormat(final OctetString value, final Descriptor options) {
        return parseStringField(options, SNMP_CONVERSION_FORMAT_PARAM, OctetStringConversionFormat::getFormat, () -> value.isPrintable() ? TEXT : BYTE_ARRAY);
    }

    static OctetStringConversionFormat getFormat(final String formatName){
        switch (formatName){
            case "text": return TEXT;
            case "hex": return HEX;
            default: return BYTE_ARRAY;
        }
    }

    @Override
    public final OpenType<?> getOpenType(){
        return openType;
    }

    static String adviceFormat(final OctetString value) {
        return value.isPrintable() ? "text" : "hex";
    }
}
