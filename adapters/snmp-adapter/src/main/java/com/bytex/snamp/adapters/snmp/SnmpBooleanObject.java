package com.bytex.snamp.adapters.snmp;

import com.bytex.snamp.internal.annotations.SpecialUse;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

import static org.snmp4j.smi.SMIConstants.SYNTAX_INTEGER32;

final class SnmpBooleanObject extends SnmpScalarObject<Integer32>{
    static final int SYNTAX = SYNTAX_INTEGER32;
    static final int defaultValue = -1;
    private static final Integer32 TRUE = new Integer32(1);
    private static final Integer32 FALSE = new Integer32(0);

    @SpecialUse
    SnmpBooleanObject(final SnmpAttributeAccessor connector) {
        super(connector, new Integer32(defaultValue));
    }

    @SpecialUse
    static Integer32 toSnmpObject(final Object value){
        return value instanceof Boolean && (Boolean)value ? TRUE : FALSE;
    }

    @SpecialUse
    static boolean fromSnmpObject(final Variable value){
        return value.toLong() != 0;
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return SNMP-compliant representation of the specified value.
     */
    @Override
    protected Integer32 convert(final Object value) {
        return toSnmpObject(value);
    }

    /**
     * Converts SNMP-compliant value to the resource-specific native value.
     *
     * @param value The value to convert.
     * @return Resource-specific representation of SNMP-compliant value.
     */
    @Override
    protected Boolean convert(final Integer32 value) {
        return fromSnmpObject(value);
    }
}
