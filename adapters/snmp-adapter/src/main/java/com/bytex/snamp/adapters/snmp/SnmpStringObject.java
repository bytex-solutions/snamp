package com.bytex.snamp.adapters.snmp;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.jmx.WellKnownType;
import org.snmp4j.smi.AssignableFromByteArray;
import org.snmp4j.smi.OctetString;

import javax.management.InvalidAttributeValueException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Objects;

import static org.snmp4j.smi.SMIConstants.SYNTAX_OCTET_STRING;

final class SnmpStringObject extends SnmpScalarObject<OctetString>{
    static final int SYNTAX = SYNTAX_OCTET_STRING;
    static final String DEFAULT_VALUE = "";

    @SpecialUse
    SnmpStringObject(final SnmpAttributeAccessor connector) {
        super(connector, SnmpHelpers.toOctetString(DEFAULT_VALUE));
    }

    @SpecialUse
    static OctetString toSnmpObject(final Object value) {
        if (value instanceof ObjectName)
            return SnmpHelpers.toOctetString(((ObjectName) value).getCanonicalName());
        else if (value instanceof String)
            return SnmpHelpers.toOctetString((String) value);
        else if (value instanceof Character)
            return SnmpHelpers.toOctetString(new String(new char[]{(char) value}));
        else return SnmpHelpers.toOctetString(Objects.toString(value, DEFAULT_VALUE));
    }

    @SpecialUse
    static Serializable fromSnmpObject(final AssignableFromByteArray value, final Type expectedType) throws InvalidAttributeValueException {
        switch (WellKnownType.getType(expectedType)){
            case STRING: return SnmpHelpers.toString(value);
            case CHAR: return SnmpHelpers.toChar(value.toString());
            case OBJECT_NAME:
                try {
                    return new ObjectName(SnmpHelpers.toString(value));
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