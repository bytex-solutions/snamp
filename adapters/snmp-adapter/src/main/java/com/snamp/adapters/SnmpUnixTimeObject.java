package com.snamp.adapters;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.smi.*;
import static com.snamp.connectors.util.ManagementEntityTypeHelper.*;

import java.util.Date;

final class SnmpUnixTimeObject extends SnmpScalarObject<TimeTicks>{
    public static final long defaultValue = -1;

    public SnmpUnixTimeObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new TimeTicks(defaultValue), timeouts);
    }

    public static TimeTicks convert(final Object value, final ManagementEntityType attributeTypeInfo){
        return new TimeTicks(convertFrom(attributeTypeInfo, value, Long.class));
    }

    public static Object convert(final Variable value, final ManagementEntityType attributeTypeInfo){
        if(supportsProjection(attributeTypeInfo, Long.class)) return value.toLong();
        else if(supportsProjection(attributeTypeInfo, Date.class)) return new Date(value.toLong());
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
