package com.bytex.snamp.adapters.snmp.helpers;

import com.bytex.snamp.io.IOUtils;
import org.snmp4j.smi.AssignableFromByteArray;
import org.snmp4j.smi.OctetString;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Provides helpers for {@link org.snmp4j.smi.OctetString} data type.
 */
public final class OctetStringHelper {
    /**
     * Default charset used to encode strings into SNMP-specific octet strings
     */
    public static final Charset SNMP_ENCODING = IOUtils.DEFAULT_CHARSET;

    private OctetStringHelper(){
        throw new InstantiationError();
    }

    public static OctetString toOctetString(final String value) {
        return new OctetString(value.getBytes(SNMP_ENCODING));
    }

    public static String toString(final AssignableFromByteArray value){
        return new String(value.toByteArray(), SNMP_ENCODING);
    }
}
