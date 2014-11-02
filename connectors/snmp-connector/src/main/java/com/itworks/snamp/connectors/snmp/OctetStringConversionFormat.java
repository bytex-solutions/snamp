package com.itworks.snamp.connectors.snmp;

import com.google.common.reflect.TypeToken;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.TypeLiterals;
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

    static final TypeToken<OctetString> OCTET_STRING = TypeToken.of(OctetString.class);

    public static OctetStringConversionFormat adviceFormat(final OctetString value){
        return value.isPrintable() ? TEXT : HEX;
    }

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
            case HEX: return new SMITypeProjection<OctetString, String>(OCTET_STRING, TypeLiterals.STRING) {
                @Override
                protected String convertFrom(final OctetString value) throws IllegalArgumentException {
                    return value.toHexString();
                }
            };
            case TEXT: return new SMITypeProjection<OctetString, String>(OCTET_STRING, TypeLiterals.STRING) {
                @Override
                protected String convertFrom(final OctetString value) throws IllegalArgumentException {
                    return new String(value.getValue());
                }
            };
            default: return new SMITypeProjection<OctetString, Object[]>(OCTET_STRING, TypeLiterals.OBJECT_ARRAY) {
                @Override
                protected Byte[] convertFrom(final OctetString value) throws IllegalArgumentException {
                    return ArrayUtils.boxArray(value.getValue());
                }
            };
        }
    }


    /**
     * Returns the name of this enum constant, as contained in the
     * declaration.  This method may be overridden, though it typically
     * isn't necessary or desirable.  An enum type should override this
     * method when a more "programmer-friendly" string form exists.
     *
     * @return the name of this enum constant
     */
    @Override
    public String toString() {
        switch (this){
            case HEX: return "hex";
            case TEXT: return "text";
            default: return "raw";
        }
    }
}
