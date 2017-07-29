package com.bytex.snamp.connector.snmp;

import org.snmp4j.smi.OID;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.SimpleType;
import java.util.Arrays;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class SnmpConnectorHelpers {

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
                                                final boolean primitive){
        return callUnchecked(() -> new ArrayType<>(type, primitive));
    }
}
