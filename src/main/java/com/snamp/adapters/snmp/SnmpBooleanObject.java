package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.ManagementConnector;
import org.snmp4j.smi.Integer32;

final class SnmpBooleanObject extends SnmpScalarObject<Integer32>{
    public static final int defaultValue = -1;

    public SnmpBooleanObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new Integer32(defaultValue), timeouts);
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Integer32 convert(final Object value) {
        return new Integer32(attributeTypeInfo.convertTo(value, Integer.class));
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Boolean convert(final Integer32 value) {
        return value.toLong() != 0;
    }
}
