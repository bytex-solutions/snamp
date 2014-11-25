package com.itworks.snamp.connectors.snmp;

import com.google.common.reflect.TypeToken;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.TypeLiterals;
import org.snmp4j.smi.OID;

import java.util.Map;

import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.SNMP_CONVERSION_FORMAT_PARAM;

/**
 * Represents {@link org.snmp4j.smi.OID} conversion format.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum OidConversionFormat {
    TEXT,
    INT_ARRAY;

    static final TypeToken<OID> OBJ_ID = TypeToken.of(OID.class);

    public static OidConversionFormat getFormat(final Map<String, String> options){
        if(options.containsKey(SNMP_CONVERSION_FORMAT_PARAM))
            return getFormat(options.get(SNMP_CONVERSION_FORMAT_PARAM));
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
            case TEXT: return new SMITypeProjection<OID, String>(OBJ_ID, TypeLiterals.STRING) {
                @Override
                protected String convertFrom(final OID value) throws IllegalArgumentException {
                    return value.toDottedString();
                }
            };
            default: return new SMITypeProjection<OID, Object[]>(OBJ_ID, TypeLiterals.OBJECT_ARRAY) {
                @Override
                protected Integer[] convertFrom(final OID value) throws IllegalArgumentException {
                    return ArrayUtils.boxArray(value.getValue());
                }
            };
        }
    }
}
