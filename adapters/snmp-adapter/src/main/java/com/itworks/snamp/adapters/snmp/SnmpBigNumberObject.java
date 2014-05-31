package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagementEntityType;
import org.snmp4j.smi.*;

import java.math.*;
import java.util.Objects;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;
import static com.itworks.snamp.connectors.ManagementEntityTypeHelper.*;

@MOSyntax(SYNTAX_OCTET_STRING)
final class SnmpBigNumberObject extends SnmpScalarObject<OctetString>{
    public static final Number defaultValue = 0;

    public SnmpBigNumberObject(final String oid, final AttributeAccessor attribute){
        super(oid, attribute, new OctetString(defaultValue.toString()));
    }

    public static OctetString convert(final Object value, final ManagementEntityType attributeTypeInfo){
        return new OctetString(Objects.toString(convertFrom(attributeTypeInfo, value, Number.class, BigInteger.class, BigDecimal.class), defaultValue.toString()));
    }

    public static Number convert(final Variable value, final ManagementEntityType attributeTypeInfo){
        return convertFrom(attributeTypeInfo, value.toString(), Number.class, fallbackWithDefaultValue(defaultValue, value, attributeTypeInfo), BigDecimal.class, BigInteger.class);
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
