package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.internal.annotations.SpecialUse;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Variable;

import java.text.ParseException;

import static org.snmp4j.smi.SMIConstants.SYNTAX_COUNTER64;

final class SnmpLongObject extends SnmpScalarObject<Counter64>{
    final static int SYNTAX = SYNTAX_COUNTER64;
    static final long DEFAULT_VALUE = -1;

    @SpecialUse
    SnmpLongObject(final AttributeAccessor connector) throws ParseException {
        super(connector, new Counter64(DEFAULT_VALUE));
    }

    @SpecialUse
    static Counter64 toSnmpObject(final Object value){
        if(value instanceof Number)
            return new Counter64(((Number)value).longValue());
        else return new Counter64(DEFAULT_VALUE);
    }

    @SpecialUse
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
