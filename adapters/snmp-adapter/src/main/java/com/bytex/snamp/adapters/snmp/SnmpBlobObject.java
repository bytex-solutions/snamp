package com.bytex.snamp.adapters.snmp;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.internal.annotations.SpecialUse;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.jmx.WellKnownType;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;

import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import java.lang.reflect.Type;
import java.util.BitSet;

/**
 * Represents SNMP wrapper for array of bytes or booleans.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpBlobObject extends SnmpScalarObject<OctetString> {
    static final int SYNTAX = SMIConstants.SYNTAX_OCTET_STRING;
    private static final OctetString DEFAULT_VALUE = new OctetString();

    @SpecialUse
    SnmpBlobObject(final SnmpAttributeAccessor attribute) {
        super(attribute, DEFAULT_VALUE);
    }

    private static OctetString toOctetString(final boolean[] value){
        final BitSet bits = IOUtils.toBitSet(value);
        return new OctetString(bits.toByteArray());
    }

    private static boolean[] toBooleanArray(final byte[] bits) {
        return IOUtils.fromBitSet(BitSet.valueOf(bits));
    }

    @SpecialUse
    static OctetString toSnmpObject(final Object value){
        if(value instanceof byte[])
            return OctetString.fromByteArray((byte[])value);
        else if(value instanceof Byte[])
            return OctetString.fromByteArray(ArrayUtils.unboxArray((Byte[])value));
        else if(value instanceof boolean[])
            return toOctetString((boolean[])value);
        else if(value instanceof Boolean[])
            return toOctetString(ArrayUtils.unboxArray((Boolean[])value));
        else return DEFAULT_VALUE;
    }



    private static Object fromSnmpObject(final OctetString value,
                                  final Type attributeType) throws InvalidAttributeValueException{
        switch (WellKnownType.getType(attributeType)){
            case BYTE_ARRAY: return value.toByteArray();
            case WRAPPED_BYTE_ARRAY: return ArrayUtils.boxArray(value.toByteArray());
            case BOOL_ARRAY: return toBooleanArray(value.toByteArray());
            case WRAPPED_BOOL_ARRAY: return ArrayUtils.boxArray(toBooleanArray(value.toByteArray()));
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
