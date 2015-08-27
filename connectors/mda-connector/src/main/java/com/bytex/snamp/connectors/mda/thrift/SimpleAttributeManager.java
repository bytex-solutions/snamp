package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.io.Buffers;
import com.bytex.snamp.jmx.DefaultValues;
import com.bytex.snamp.jmx.WellKnownType;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;

import javax.management.ObjectName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SimpleAttributeManager extends ThriftAttributeManager {
    private final WellKnownType attributeType;

    protected SimpleAttributeManager(final WellKnownType type, final String slotName) {
        super(type.getOpenType(), slotName);
        this.attributeType = type;
    }

    @Override
    Object getDefaultValue() {
        return DefaultValues.get(attributeType.getOpenType());
    }

    static void serialize(final Object input, final WellKnownType attributeType, final TProtocol output) throws TException{
        switch (attributeType){
            case BOOL:
                output.writeBool((boolean)input);
                return;
            case BYTE:
                output.writeByte((byte)input);
                return;
            case SHORT:
                output.writeI16((short)input);
                return;
            case INT:
                output.writeI32((int)input);
                return;
            case LONG:
                output.writeI64((long)input);
                return;
            case FLOAT:
                output.writeDouble((float)input);
                return;
            case DOUBLE:
                output.writeDouble((double)input);
                return;
            case DATE:
                output.writeI64(((Date)input).getTime());
                return;
            case BYTE_ARRAY:
                output.writeBinary(Buffers.wrap((byte[])input));
                return;
            case WRAPPED_BYTE_ARRAY:
                output.writeBinary(Buffers.wrap(ArrayUtils.unboxArray((Byte[])input)));
                return;
            case BYTE_BUFFER:
                output.writeBinary((ByteBuffer)input);
                return;
            case OBJECT_NAME:
                output.writeString(((ObjectName)input).getCanonicalName());
                return;
            case STRING:
            case BIG_INT:
            case BIG_DECIMAL:
            case CHAR:
            default:
                output.writeString(String.valueOf(input));
        }
    }

    @Override
    protected void serialize(final Object input, final TProtocol output) throws TException {
        serialize(input, attributeType, output);
    }

    private static char toChar(final String value){
        return value.isEmpty() ? '\0' : value.charAt(0);
    }

    static Object deserialize(final TProtocol input, final WellKnownType attributeType) throws TException {
        switch (attributeType){
            case BOOL:
                return input.readBool();
            case BYTE:
                return input.readByte();
            case SHORT:
                return input.readI16();
            case INT:
                return input.readI32();
            case LONG:
                return input.readI64();
            case FLOAT:
                return (float)input.readDouble();
            case DOUBLE:
                return input.readDouble();
            case DATE:
                return new Date(input.readI64());
            case STRING:
                return input.readString();
            case BIG_INT:
                return new BigInteger(input.readString());
            case BIG_DECIMAL:
                return new BigDecimal(input.readString());
            case CHAR:
                return toChar(input.readString());
            case BYTE_BUFFER:
                return input.readBinary();
            case BYTE_ARRAY:
                return Buffers.readRemaining(input.readBinary());
            case WRAPPED_BYTE_ARRAY:
                return ArrayUtils.boxArray(Buffers.readRemaining(input.readBinary()));
            default:
                return null;
        }
    }

    @Override
    protected Object deserialize(final TProtocol input) throws TException {
        return deserialize(input, attributeType);
    }
}
