package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.io.Buffers;
import com.bytex.snamp.jmx.DefaultValues;
import com.bytex.snamp.jmx.WellKnownType;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

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
final class SimpleValueParser implements ThriftValueParser {
    private final WellKnownType attributeType;
    private final TStruct struct;

    protected SimpleValueParser(final WellKnownType type, final String structName) {
        this.attributeType = type;
        this.struct = new TStruct(structName.concat("_struct"));
    }

    @Override
    public Object getDefaultValue() {
        return DefaultValues.get(attributeType.getOpenType());
    }

    static void serialize(final Object input,
                          final WellKnownType attributeType,
                          final TProtocol output,
                          final short index,
                          final String fieldName) throws TException{
        switch (attributeType){
            case BOOL:
                output.writeFieldBegin(new TField(fieldName, TType.BOOL, index));
                output.writeBool((boolean) input);
                output.writeFieldEnd();
                return;
            case BYTE:
                output.writeFieldBegin(new TField(fieldName, TType.BYTE, index));
                output.writeByte((byte) input);
                output.writeFieldEnd();
                return;
            case SHORT:
                output.writeFieldBegin(new TField(fieldName, TType.I16, index));
                output.writeI16((short) input);
                output.writeFieldEnd();
                return;
            case INT:
                output.writeFieldBegin(new TField(fieldName, TType.I32, index));
                output.writeI32((int) input);
                output.writeFieldEnd();
                return;
            case LONG:
                output.writeFieldBegin(new TField(fieldName, TType.I64, index));
                output.writeI64((long) input);
                output.writeFieldEnd();
                return;
            case FLOAT:
                output.writeFieldBegin(new TField(fieldName, TType.DOUBLE, index));
                output.writeDouble((float) input);
                output.writeFieldEnd();
                return;
            case DOUBLE:
                output.writeFieldBegin(new TField(fieldName, TType.DOUBLE, index));
                output.writeDouble((double) input);
                output.writeFieldEnd();
                return;
            case DATE:
                output.writeFieldBegin(new TField(fieldName, TType.I64, index));
                output.writeI64(((Date) input).getTime());
                output.writeFieldEnd();
                return;
            case BYTE_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.STRING, index));
                output.writeBinary(Buffers.wrap((byte[]) input));
                output.writeFieldEnd();
                return;
            case WRAPPED_BYTE_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.STRING, index));
                output.writeBinary(Buffers.wrap(ArrayUtils.unboxArray((Byte[]) input)));
                output.writeFieldEnd();
                return;
            case BYTE_BUFFER:
                output.writeFieldBegin(new TField(fieldName, TType.STRING, index));
                output.writeBinary((ByteBuffer) input);
                output.writeFieldEnd();
                return;
            case OBJECT_NAME:
                output.writeFieldBegin(new TField(fieldName, TType.STRING, index));
                output.writeString(((ObjectName) input).getCanonicalName());
                output.writeFieldEnd();
                return;
            case STRING:
            case BIG_INT:
            case BIG_DECIMAL:
            case CHAR:
            default:
                output.writeFieldBegin(new TField(fieldName, TType.STRING, index));
                output.writeString(String.valueOf(input));
                output.writeFieldEnd();
        }
    }

    @Override
    public void serialize(final Object input, final TProtocol output) throws TException {
        output.writeStructBegin(struct);
        serialize(input, attributeType, output, (short) 0, "value");
        output.writeFieldStop();
        output.writeStructEnd();
    }

    private static char toChar(final String value){
        return value.isEmpty() ? '\0' : value.charAt(0);
    }

    static Object deserializeNaked(final TProtocol input,
                              final WellKnownType attributeType) throws TException {
        switch (attributeType) {
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
                return (float) input.readDouble();
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
    public Object deserialize(final TProtocol input) throws TException {
        input.readStructBegin();
        input.readFieldBegin();
        try {
            return deserializeNaked(input, attributeType);
        }
        finally {
            ThriftUtils.skipStopField(input);
            input.readFieldEnd();
            input.readStructEnd();
        }
    }
}
