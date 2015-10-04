package com.bytex.snamp.connectors.snmp;

import com.bytex.snamp.TimeSpan;
import org.snmp4j.smi.OID;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Arrays;
import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpConnectorHelpers {
    private static final String DISCOVERY_TIMEOUT_PROPERTY = "com.bytex.snamp.connectors.snmp.discoveryTimeout";

    private SnmpConnectorHelpers(){

    }

    private static int[] getPostfix(final int[] prefix, final int[] full){
        return full.length > prefix.length ?
                Arrays.copyOfRange(full, prefix.length, full.length):
                emptyArray(int[].class);
    }

    public static OID getPostfix(final OID prefix, final OID oid){
        return oid.startsWith(prefix) ? new OID(getPostfix(prefix.getValue(), oid.getValue())) : new OID();
    }

    static <T> ArrayType<T[]> arrayType(final SimpleType<T> type,
                                                final boolean primitive) throws ExceptionInInitializerError{
        try {
            return new ArrayType<>(type, primitive);
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static TimeSpan getDiscoveryTimeout(){
        return TimeSpan.ofMillis(System.getProperty(DISCOVERY_TIMEOUT_PROPERTY, "5000"));
    }
}
