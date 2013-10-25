package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.smi.*;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;

@MOSyntax(SYNTAX_OCTET_STRING)
final class SnmpStringObject extends SnmpScalarObject<OctetString>{
    public static final String defaultValue = "";

    public SnmpStringObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new OctetString(defaultValue), timeouts);
    }

    public static OctetString convert(final Object value, final AttributeTypeInfo attributeTypeInfo){
        return new OctetString(attributeTypeInfo.convertTo(value, String.class));
    }

    public static String convert(final Variable value, final AttributeTypeInfo attributeTypeInfo){
        return value.toString();
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected OctetString convert(final Object value) {
        return convert(value, attributeTypeInfo);
    }

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     *
     * @param value The value to convert.
     * @return
     */
    @Override
    protected String convert(final OctetString value) {
        return convert(value, attributeTypeInfo);
    }
}