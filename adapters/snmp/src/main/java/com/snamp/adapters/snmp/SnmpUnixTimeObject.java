package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.smi.*;

import java.util.Date;

final class SnmpUnixTimeObject extends SnmpScalarObject<TimeTicks>{
    public static final long defaultValue = -1;

    public SnmpUnixTimeObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new TimeTicks(defaultValue), timeouts);
    }

    public static TimeTicks convert(final Object value, final AttributeTypeInfo attributeTypeInfo){
        return new TimeTicks(attributeTypeInfo.convertTo(value, Long.class));
    }

    public static Object convert(final Variable value, final AttributeTypeInfo attributeTypeInfo){
        if(attributeTypeInfo.canConvertFrom(Long.class)) return value.toLong();
        else if(attributeTypeInfo.canConvertFrom(Date.class)) return new Date(value.toLong());
        else return new Date();
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected TimeTicks convert(final Object value) {
        return convert(value, attributeTypeInfo);
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Object convert(final TimeTicks value) {
        return convert(value, attributeTypeInfo);
    }
}
