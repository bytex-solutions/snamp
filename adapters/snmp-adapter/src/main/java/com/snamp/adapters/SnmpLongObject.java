package com.snamp.adapters;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.smi.*;

import static org.snmp4j.smi.SMIConstants.SYNTAX_COUNTER64;

@MOSyntax(SYNTAX_COUNTER64)
final class SnmpLongObject extends SnmpScalarObject<Counter64>{
    public static final long defaultValue = -1;

    public SnmpLongObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new Counter64(defaultValue), timeouts);
    }

    public static Counter64 convert(final Object value, final AttributeTypeInfo attributeTypeInfo){
        return new Counter64(attributeTypeInfo.convertTo(value, Long.class));
    }

    public static Long convert(final Variable value, final AttributeTypeInfo attributeTypeInfo){
        if(attributeTypeInfo.canConvertFrom(Long.class)) return value.toLong();
        else return defaultValue;
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Counter64 convert(final Object value) {
        return convert(value, attributeTypeInfo);
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Long convert(final Counter64 value) {
        return convert(value, attributeTypeInfo);
    }
}
