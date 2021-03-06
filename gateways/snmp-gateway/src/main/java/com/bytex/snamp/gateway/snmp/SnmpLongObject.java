package com.bytex.snamp.gateway.snmp;

import com.bytex.snamp.SpecialUse;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Variable;

import static org.snmp4j.smi.SMIConstants.SYNTAX_COUNTER64;

final class SnmpLongObject extends SnmpScalarObject<Counter64>{
    final static int SYNTAX = SYNTAX_COUNTER64;
    private static final long DEFAULT_VALUE = -1;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    SnmpLongObject(final SnmpAttributeAccessor connector) {
        super(connector, new Counter64(DEFAULT_VALUE));
    }

    @SpecialUse(SpecialUse.Case.REFLECTION)
    static Counter64 toSnmpObject(final Object value){
        if(value instanceof Number)
            return new Counter64(((Number)value).longValue());
        else return new Counter64(DEFAULT_VALUE);
    }

    @SpecialUse(SpecialUse.Case.REFLECTION)
    static long fromSnmpObject(final Variable value){
        return value.toLong();
    }

    @Override
    protected Counter64 convert(final Object value) {
        return toSnmpObject(value);
    }

    @Override
    protected Long convert(final Counter64 value) {
        return fromSnmpObject(value);
    }
}
