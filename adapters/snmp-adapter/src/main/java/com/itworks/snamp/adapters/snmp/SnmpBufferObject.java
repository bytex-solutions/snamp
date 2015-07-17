package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.io.Buffers;
import com.itworks.snamp.jmx.WellKnownType;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;

import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import java.lang.reflect.Type;
import java.nio.*;
import java.text.ParseException;

import com.itworks.snamp.adapters.AttributeAccessor;

/**
 * Represents SNMP wrapper for all buffer types.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpBufferObject extends SnmpScalarObject<OctetString> {
    static final int SYNTAX = SMIConstants.SYNTAX_OCTET_STRING;
    private static final OctetString DEFAULT_VALUE = new OctetString();

    @SpecialUse
    SnmpBufferObject(final SnmpAttributeAccessor attribute) {
        super(attribute, DEFAULT_VALUE);
    }

    @SpecialUse
    static OctetString toSnmpObject(final Object value){
        final ByteBuffer result;
        if(value instanceof ByteBuffer)
            result = (ByteBuffer)value;
        else if(value instanceof CharBuffer)
            result = Buffers.toByteBuffer((CharBuffer)value);
        else if(value instanceof ShortBuffer)
            result = Buffers.toByteBuffer((ShortBuffer)value);
        else if(value instanceof IntBuffer)
            result = Buffers.toByteBuffer((IntBuffer)value);
        else if(value instanceof LongBuffer)
            result = Buffers.toByteBuffer((LongBuffer)value);
        else if(value instanceof FloatBuffer)
            result = Buffers.toByteBuffer((FloatBuffer)value);
        else if(value instanceof DoubleBuffer)
            result = Buffers.toByteBuffer((DoubleBuffer)value);
        else return DEFAULT_VALUE;
        return OctetString.fromByteArray(Buffers.readRemaining(result));
    }

    private static Buffer convert(final OctetString value, final Type attributeType) throws InvalidAttributeValueException {
        final ByteBuffer buffer = Buffers.wrap(value.toByteArray());
        switch (WellKnownType.getType(attributeType)){
            case CHAR_BUFFER: return buffer.asCharBuffer();
            case SHORT_BUFFER: return buffer.asShortBuffer();
            case INT_BUFFER: return buffer.asIntBuffer();
            case LONG_BUFFER: return buffer.asLongBuffer();
            case FLOAT_BUFFER: return buffer.asFloatBuffer();
            case DOUBLE_BUFFER: return buffer.asDoubleBuffer();
            case BYTE_BUFFER: return buffer;
            default: throw unexpectedAttributeType(attributeType);
        }
    }

    @SpecialUse
    static Buffer fromSnmpObject(final Variable value, final Type attributeType) throws InvalidAttributeValueException {
        if(value instanceof OctetString)
            return convert((OctetString)value, attributeType);
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
        return convert(value, getAttributeType());
    }
}
