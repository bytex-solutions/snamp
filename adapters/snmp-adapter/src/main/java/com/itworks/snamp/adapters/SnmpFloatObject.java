package com.itworks.snamp.adapters;

import com.itworks.snamp.connectors.AttributeSupport;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.TimeSpan;
import org.snmp4j.smi.*;
import static com.itworks.snamp.connectors.util.ManagementEntityTypeHelper.*;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;

@MOSyntax(SYNTAX_OCTET_STRING)
final class SnmpFloatObject extends SnmpScalarObject<OctetString>{
    public static final Number defaultValue = -1.0F;

    public SnmpFloatObject(final String oid, final AttributeSupport connector, final TimeSpan timeouts){
        super(oid, connector, new OctetString(defaultValue.toString()), timeouts);
    }

    public static OctetString convert(final Object value, final ManagementEntityType attributeTypeInfo){
        final Number convertedValue = convertFrom(attributeTypeInfo, value, Number.class, Float.class, Double.class);
        return new OctetString(convertedValue.toString());
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
