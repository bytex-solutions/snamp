package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.jmx.WellKnownType;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;

import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import java.lang.reflect.Type;

import com.itworks.snamp.adapters.AttributeAccessor;

/**
 * Represents SNMP wrapper for byte array.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpByteArrayObject extends SnmpScalarObject<OctetString> {
    static final int SYNTAX = SMIConstants.SYNTAX_OCTET_STRING;
    private static final OctetString DEFAULT_VALUE = new OctetString();

    @SpecialUse
    SnmpByteArrayObject(final AttributeAccessor attribute) {
        super(attribute, DEFAULT_VALUE);
    }

    @SpecialUse
    static OctetString toSnmpObject(final Object value){
        if(value instanceof byte[])
            return OctetString.fromByteArray((byte[])value);
        else if(value instanceof Byte[])
            return OctetString.fromByteArray(ArrayUtils.unboxArray((Byte[])value));
        else return DEFAULT_VALUE;
    }

    private static Object fromSnmpObject(final OctetString value,
                                  final Type attributeType) throws InvalidAttributeValueException{
        switch (WellKnownType.getType(attributeType)){
            case BYTE_ARRAY: return value.toByteArray();
            case WRAPPED_BYTE_ARRAY: return ArrayUtils.boxArray(value.toByteArray());
            default: throw unexpectedAttributeType(attributeType);
        }
    }

    @SpecialUse
    static Object fromSnmpObject(final Variable value,
                                 final Type attributeType) throws InvalidAttributeValueException{
        if(value instanceof OctetString)
            return fromSnmpObject((OctetString)value, attributeType);
        else throw unexpectedSnmpType(OctetString.class);
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
