package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.ManagementConnector;
import org.snmp4j.smi.OctetString;

final class SnmpBigNumberObject extends SnmpScalarObject<OctetString>{
    public static final Number defaultValue = 0;

    public SnmpBigNumberObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new OctetString(defaultValue.toString()), timeouts);
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected OctetString convert(final Object value) {
        return new OctetString(attributeTypeInfo.convertTo(value, String.class));
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected Object convert(final OctetString value) {
        if(attributeTypeInfo.canConvertFrom(String.class)) return value.toString();
        else return defaultValue;
    }
}
