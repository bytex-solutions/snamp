package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.ManagementConnector;
import org.snmp4j.smi.Counter64;

final class SnmpLongObject extends SnmpScalarObject<Counter64>{
    public static final long defaultValue = -1;

    public SnmpLongObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new Counter64(defaultValue), timeouts);
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Counter64 convert(final Object value) {
        return new Counter64(attributeTypeInfo.convertTo(value, Long.class));
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Long convert(final Counter64 value) {
        if(attributeTypeInfo.canConvertFrom(Long.class)) return value.toLong();
        else return defaultValue;
    }
}
