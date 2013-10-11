package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.ManagementConnector;
import org.snmp4j.smi.OctetString;

final class SnmpStringObject extends SnmpScalarObject<OctetString>{
    public static final String defaultValue = "";

    public SnmpStringObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new OctetString(defaultValue), timeouts);
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
    protected String convert(final OctetString value) {
        return value.toString();
    }
}