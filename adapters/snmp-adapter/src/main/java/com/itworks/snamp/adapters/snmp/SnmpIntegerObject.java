package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.jmx.WellKnownType;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import java.lang.reflect.Type;

import static org.snmp4j.smi.SMIConstants.SYNTAX_INTEGER32;

/**
 * Represents Integer SNMP mapping,
 */
final class SnmpIntegerObject extends SnmpScalarObject<Integer32>{
    static final int SYNTAX = SYNTAX_INTEGER32;
    static final int DEFAULT_VALUE = -1;

    @SpecialUse
    SnmpIntegerObject(final AttributeAccessor connector){
        super(connector, new Integer32(DEFAULT_VALUE));
    }

    @SpecialUse
    static Integer32 toSnmpObject(final Object value) {
        if(value instanceof Boolean)
            return SnmpBooleanObject.toSnmpObject(value);
        else if(value instanceof Number)
            return new Integer32(((Number)value).intValue());
        else return new Integer32(DEFAULT_VALUE);
    }

    @SpecialUse
    static Object fromSnmpObject(final Variable value, final Type attributeTypeInfo) throws InvalidAttributeValueException {
        switch (WellKnownType.getType(attributeTypeInfo)){
            case BYTE:
                return SnmpHelpers.toByte(value.toLong());
            case SHORT:
                return SnmpHelpers.toShort(value.toLong());
            case INT:
                return value.toInt();
            case BOOL:
                return value.toInt() != 0;
            default:
                throw unexpectedAttributeType(attributeTypeInfo);
        }
    }

    @Override
    protected Integer32 convert(final Object value){
        return toSnmpObject(value);
    }

    @Override
    protected Object convert(final Integer32 value) throws ReflectionException, InvalidAttributeValueException {
        return fromSnmpObject(value, getAttributeType());
    }
}
