package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.jmx.WellKnownType;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import java.lang.reflect.Type;
import java.util.Objects;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;

final class SnmpFloatObject extends SnmpScalarObject<OctetString>{
    static final Number DEFAULT_VALUE = -1.0F;
    static final int SYNTAX = SYNTAX_OCTET_STRING;

    @SpecialUse
    SnmpFloatObject(final AttributeAccessor connector){
        super(connector, new OctetString(DEFAULT_VALUE.toString()));
    }

    @SpecialUse
    static OctetString toSnmpObject(final Object value){
        return new OctetString(Objects.toString(value, "0"));
    }

    @SpecialUse
    static Number fromSnmpObject(final Variable value, final Type attributeTypeInfo) throws InvalidAttributeValueException {
        switch (WellKnownType.getType(attributeTypeInfo)){
            case FLOAT: return Float.valueOf(value.toString());
            case DOUBLE: return Double.valueOf(value.toString());
            default: throw unexpectedAttributeType(attributeTypeInfo);
        }
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     *
     * @param value The value to convert.
     * @return SNMP-compliant representation of the specified value.
     */
    @Override
    protected OctetString convert(final Object value) {
        return toSnmpObject(value);
    }

    /**
     * Converts SNMP-compliant value to the resource-specific native value.
     *
     * @param value The value to convert.
     * @return Resource-specific representation of SNMP-compliant value.
     */
    @Override
    protected Object convert(final OctetString value) throws ReflectionException, InvalidAttributeValueException {
        return fromSnmpObject(value, getAttributeType());
    }
}
