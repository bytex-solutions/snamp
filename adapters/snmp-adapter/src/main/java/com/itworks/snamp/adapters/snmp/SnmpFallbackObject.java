package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.internal.annotations.SpecialUse;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;

import javax.management.InvalidAttributeValueException;
import java.util.Objects;

/**
 * Represents SNMP wrapper for attribute with unknown type.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpFallbackObject extends SnmpScalarObject<OctetString> {
    static final int SYNTAX = SMIConstants.SYNTAX_OCTET_STRING;
    private static final String DEFAULT_VALUE = "";

    @SpecialUse
    SnmpFallbackObject(final AttributeAccessor attribute) {
        super(attribute, true, SnmpHelpers.toOctetString(DEFAULT_VALUE));
    }

    @SpecialUse
    static OctetString toSnmpObject(final Object value){
        return SnmpHelpers.toOctetString(Objects.toString(value, DEFAULT_VALUE));
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
    protected Object convert(final OctetString value) throws InvalidAttributeValueException {
        throw readOnlyException();
    }

    static InvalidAttributeValueException readOnlyException(){
        return new InvalidAttributeValueException("Attribute is read-only");
    }
}
