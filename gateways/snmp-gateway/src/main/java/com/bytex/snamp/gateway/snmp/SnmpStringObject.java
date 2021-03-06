package com.bytex.snamp.gateway.snmp;

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
    private static final String DEFAULT_VALUE = "";

    SnmpStringObject(final SnmpAttributeAccessor connector) {
        super(connector, OctetStringHelper.toOctetString(DEFAULT_VALUE));
    }

    static OctetString toSnmpObject(final Object value) {
        if (value instanceof ObjectName)
            return OctetStringHelper.toOctetString(((ObjectName) value).getCanonicalName());
        else if (value instanceof String)
            return OctetStringHelper.toOctetString((String) value);
        else if (value instanceof Character)
            return OctetStringHelper.toOctetString(new String(new char[]{(char) value}));
        else return OctetStringHelper.toOctetString(Objects.toString(value, DEFAULT_VALUE));
    }

    static Serializable fromSnmpObject(final AssignableFromByteArray value, final Type expectedType) throws InvalidAttributeValueException {
        switch (WellKnownType.getType(expectedType)){
            case STRING: return OctetStringHelper.toString(value);
            case CHAR: return SnmpHelpers.toChar(value.toString());
            case OBJECT_NAME:
                try {
                    return new ObjectName(OctetStringHelper.toString(value));
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