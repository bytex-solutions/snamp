package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.ManagementConnector;
import org.snmp4j.smi.Integer32;

/**
 * Represents Integer SNMP mapping,
 */
final class SnmpIntegerObject extends SnmpScalarObject<Integer32>{
    public static final int defaultValue = -1;

    public SnmpIntegerObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new Integer32(defaultValue), timeouts);
    }

    @Override
    protected Integer32 convert(final Object value){
        return new Integer32(attributeTypeInfo.convertTo(value, Integer.class));
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Object convert(final Integer32 value) {
        if(attributeTypeInfo.canConvertFrom(Long.class)) return value.toLong();
        else if(attributeTypeInfo.canConvertFrom(Integer.class)) return value.toInt();
        else if(attributeTypeInfo.canConvertFrom(String.class)) return value.toString();
        else return null;
    }
}
