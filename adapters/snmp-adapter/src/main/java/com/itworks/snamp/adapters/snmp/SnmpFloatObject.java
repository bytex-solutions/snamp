package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagedEntityType;
import org.snmp4j.smi.*;
import static com.itworks.snamp.connectors.ManagedEntityTypeHelper.*;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;

@MOSyntax(SYNTAX_OCTET_STRING)
final class SnmpFloatObject extends SnmpScalarObject<OctetString>{
    public static final Number defaultValue = -1.0F;

    public SnmpFloatObject(final String oid, final AttributeAccessor connector){
        super(oid, connector, new OctetString(defaultValue.toString()));
    }

    public static OctetString convert(final Object value, final ManagedEntityType attributeTypeInfo){
        final Number convertedValue = convertFrom(attributeTypeInfo, value, TypeLiterals.NUMBER, TypeLiterals.FLOAT, TypeLiterals.DOUBLE);
        return new OctetString(convertedValue.toString());
    }

    public static Object convert(final Variable value, final ManagedEntityType attributeTypeInfo){
        return convertFrom(attributeTypeInfo, value.toString(), TypeLiterals.NUMBER, fallbackWithDefaultValue(defaultValue, value, attributeTypeInfo), TypeLiterals.FLOAT, TypeLiterals.DOUBLE);
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return SNMP-compliant representation of the specified value.
     */
    @Override
    protected OctetString convert(final Object value) {
        return convert(value, getMetadata().getType());
    }

    /**
     * Converts SNMP-compliant value to the resource-specific native value.
     *
     * @param value The value to convert.
     * @return Resource-specific representation of SNMP-compliant value.
     */
    @Override
    protected Object convert(final OctetString value) {
        return convert(value, getMetadata().getType());
    }
}
