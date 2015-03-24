package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.jmx.WellKnownType;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import javax.management.*;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Objects;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;

final class SnmpStringObject extends SnmpScalarObject<OctetString>{
    static int SYNTAX = SYNTAX_OCTET_STRING;
    static final String DEFAULT_VALUE = "";

    @SpecialUse
    SnmpStringObject(final AttributeAccessor connector){
        super(connector, new OctetString(DEFAULT_VALUE));
    }

    @SpecialUse
    static OctetString toSnmpObject(final Object value){
        if(value instanceof ObjectName)
            return new OctetString(((ObjectName)value).getCanonicalName());
        else if(value instanceof String)
            return new OctetString((String)value);
        else if(value instanceof Character)
            return new OctetString(String.valueOf((char)value));
        else return new OctetString(Objects.toString(value, DEFAULT_VALUE));
    }

    @SpecialUse
    static Serializable fromSnmpObject(final Variable value, final Type expectedType) throws InvalidAttributeValueException {
        switch (WellKnownType.getType(expectedType)){
            case STRING: return value.toString();
            case CHAR: return SnmpHelpers.toChar(value.toString());
            case OBJECT_NAME:
                try {
                    return new ObjectName(value.toString());
                } catch (final MalformedObjectNameException e) {
                    throw new InvalidAttributeValueException(e.getMessage());
                }
            default: throw unexpectedAttributeType(expectedType);
        }
    }

    @Override
    protected OctetString convert(final Object value) {
        return toSnmpObject(value);
    }

    @Override
    protected Serializable convert(final OctetString value) throws ReflectionException, InvalidAttributeValueException {
        return fromSnmpObject(value, getAttributeType());
    }
}