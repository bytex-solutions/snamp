package com.snamp.adapters;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.smi.*;
import static com.snamp.connectors.util.ManagementEntityTypeHelper.*;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;

@MOSyntax(SYNTAX_OCTET_STRING)
final class SnmpFloatObject extends SnmpScalarObject<OctetString>{
    public static final Number defaultValue = -1.0F;

    public SnmpFloatObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        super(oid, connector, new OctetString(defaultValue.toString()), timeouts);
    }

    public static OctetString convert(final Object value, final ManagementEntityType attributeTypeInfo){
        return new OctetString(convertFrom(attributeTypeInfo, value, String.class));
    }

    public static Object convert(final Variable value, final ManagementEntityType attributeTypeInfo){
        return convertFrom(attributeTypeInfo, value.toString(), Number.class, fallbackWithDefaultValue(defaultValue, value, attributeTypeInfo), Float.class, Double.class);
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
    protected Object convert(final OctetString value) {
        return convert(value, attributeTypeInfo);
    }
}
