package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagedEntityType;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.util.Objects;

import static com.itworks.snamp.connectors.ManagedEntityTypeHelper.convertFrom;
import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;

@MOSyntax(SYNTAX_OCTET_STRING)
final class SnmpBigNumberObject extends SnmpScalarObject<OctetString>{
    public static final Number defaultValue = 0;

    public SnmpBigNumberObject(final String oid, final AttributeAccessor attribute){
        super(oid, attribute, new OctetString(defaultValue.toString()));
    }

    public static OctetString convert(final Object value, final ManagedEntityType attributeTypeInfo){
        return new OctetString(Objects.toString(convertFrom(attributeTypeInfo, value, TypeLiterals.NUMBER, TypeLiterals.BIG_INTEGER, TypeLiterals.BIG_DECIMAL), defaultValue.toString()));
    }

    public static Number convert(final Variable value, final ManagedEntityType attributeTypeInfo){
        return convertFrom(attributeTypeInfo, value.toString(), TypeLiterals.NUMBER, fallbackWithDefaultValue(defaultValue, value, attributeTypeInfo), TypeLiterals.BIG_DECIMAL, TypeLiterals.BIG_INTEGER);
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
