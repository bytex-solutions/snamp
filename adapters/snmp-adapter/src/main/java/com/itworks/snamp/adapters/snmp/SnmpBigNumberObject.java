package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.jmx.WellKnownType;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;

final class SnmpBigNumberObject extends SnmpScalarObject<OctetString>{
    static final int SYNTAX = SYNTAX_OCTET_STRING;
    static final Number DEFAULT_VALUE = 0;

    @SpecialUse
    SnmpBigNumberObject(final AttributeAccessor attribute){
        super(attribute, new OctetString(DEFAULT_VALUE.toString()));
    }

    @SpecialUse
    static OctetString toSnmpObject(final Object value){
        final String result;
        if(value instanceof BigDecimal)
            result = ((BigDecimal)value).toPlainString();
        else if(value instanceof BigInteger)
            result = value.toString();
        else result = Objects.toString(value, "0");
        return new OctetString(result);
    }

    @SpecialUse
    static Number fromSnmpObject(final Variable value, final Type attributeTypeInfo) throws InvalidAttributeValueException {
        switch (WellKnownType.getType(attributeTypeInfo)){
            case BIG_DECIMAL:
                try {
                    return new BigDecimal(value.toString());
                }
                catch (final NumberFormatException e){
                    throw new InvalidAttributeValueException(e.getMessage());
                }
            case BIG_INT:
                try {
                    return new BigInteger(value.toString());
                }
                catch (final NumberFormatException e){
                    throw new InvalidAttributeValueException(e.getMessage());
                }
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
