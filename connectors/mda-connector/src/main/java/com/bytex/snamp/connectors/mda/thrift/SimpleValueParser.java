package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.io.Buffers;
import com.bytex.snamp.jmx.DefaultValues;
import com.bytex.snamp.jmx.WellKnownType;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.*;
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

    private static void serialize(final short[] input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.I16, input.length));
        for (final short item : input)
            output.writeI16(item);
        output.writeListEnd();
    }

    private static void serialize(final ShortBuffer input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.I16, input.remaining()));
        while (input.hasRemaining())
            output.writeI16(input.get());
        output.writeListEnd();
    }

    private static void serialize(final char[] input,
                                  final TProtocol output) throws TException {
        output.writeString(new String(input));
    }

    private static void serialize(final CharBuffer input,
                                  final TProtocol output) throws TException {
        serialize(Buffers.readRemaining(input), output);
    }

    private static void serialize(final int[] input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.I32, input.length));
        for (final int item : input)
            output.writeI32(item);
        output.writeListEnd();
    }

    private static void serialize(final IntBuffer input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.I32, input.remaining()));
        while (input.hasRemaining())
            output.writeI32(input.get());
        output.writeListEnd();
    }

    private static void serialize(final long[] input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.I64, input.length));
        for (final long item : input)
            output.writeI64(item);
        output.writeListEnd();
    }

    private static void serialize(final LongBuffer input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.I64, input.remaining()));
        while (input.hasRemaining())
            output.writeI64(input.get());
        output.writeListEnd();
    }

    private static void serialize(final boolean[] input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.BOOL, input.length));
        for (final boolean item : input)
            output.writeBool(item);
        output.writeListEnd();
    }

    private static void serialize(final String[] input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.STRING, input.length));
        for (final String item : input)
            output.writeString(item);
        output.writeListEnd();
    }

    private static void serialize(final Date[] input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.I64, input.length));
        for (final Date item : input)
            output.writeI64(item.getTime());
        output.writeListEnd();
    }

    private static void serialize(final Number[] input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.STRING, input.length));
        for (final Number item : input)
            output.writeString(item.toString());
        output.writeListEnd();
    }

    private static void serialize(final ObjectName[] input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.STRING, input.length));
        for (final ObjectName item : input)
            output.writeString(item.getCanonicalName());
        output.writeListEnd();
    }

    private static void serialize(final float[] input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.DOUBLE, input.length));
        for (final float item : input)
            output.writeDouble(item);
        output.writeListEnd();
    }

    private static void serialize(final FloatBuffer input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.DOUBLE, input.remaining()));
        while (input.hasRemaining())
            output.writeDouble(input.get());
        output.writeListEnd();
    }

    private static void serialize(final double[] input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.DOUBLE, input.length));
        for (final double item : input)
            output.writeDouble(item);
        output.writeListEnd();
    }

    private static void serialize(final DoubleBuffer input,
                                  final TProtocol output) throws TException {
        output.writeListBegin(new TList(TType.DOUBLE, input.remaining()));
        while (input.hasRemaining())
            output.writeDouble(input.get());
        output.writeListEnd();
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
            case SHORT_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((short[]) input, output);
                output.writeFieldEnd();
                return;
            case WRAPPED_SHORT_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize(ArrayUtils.unboxArray((Short[]) input), output);
                output.writeFieldEnd();
                return;
            case SHORT_BUFFER:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((ShortBuffer) input, output);
                output.writeFieldEnd();
                return;
            case INT_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((int[])input, output);
                output.writeFieldEnd();
                return;
            case WRAPPED_INT_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize(ArrayUtils.unboxArray((Integer[])input), output);
                output.writeFieldEnd();
                return;
            case INT_BUFFER:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((IntBuffer) input, output);
                output.writeFieldEnd();
                return;
            case LONG_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((long[])input, output);
                output.writeFieldEnd();
                return;
            case WRAPPED_LONG_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize(ArrayUtils.unboxArray((Long[])input), output);
                output.writeFieldEnd();
                return;
            case LONG_BUFFER:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((LongBuffer) input, output);
                output.writeFieldEnd();
                return;
            case FLOAT_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((float[])input, output);
                output.writeFieldEnd();
                return;
            case WRAPPED_FLOAT_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize(ArrayUtils.unboxArray((Float[])input), output);
                output.writeFieldEnd();
                return;
            case FLOAT_BUFFER:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((FloatBuffer) input, output);
                output.writeFieldEnd();
                return;
            case DOUBLE_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((double[])input, output);
                output.writeFieldEnd();
                return;
            case WRAPPED_DOUBLE_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize(ArrayUtils.unboxArray((Double[])input), output);
                output.writeFieldEnd();
                return;
            case DOUBLE_BUFFER:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((DoubleBuffer) input, output);
                output.writeFieldEnd();
                return;
            case CHAR_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((char[]) input, output);
                output.writeFieldEnd();
                return;
            case WRAPPED_CHAR_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize(ArrayUtils.unboxArray((Character[])input), output);
                output.writeFieldEnd();
                return;
            case CHAR_BUFFER:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((CharBuffer) input, output);
                output.writeFieldEnd();
                return;
            case BOOL_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((boolean[]) input, output);
                output.writeFieldEnd();
                return;
            case WRAPPED_BOOL_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize(ArrayUtils.unboxArray((Boolean[]) input), output);
                output.writeFieldEnd();
                return;
            case STRING_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((String[]) input, output);
                output.writeFieldEnd();
                return;
            case OBJECT_NAME_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((ObjectName[]) input, output);
                output.writeFieldEnd();
                return;
            case DATE_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((Date[]) input, output);
                output.writeFieldEnd();
                return;
            case BIG_INT_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((BigInteger[]) input, output);
                output.writeFieldEnd();
                return;
            case BIG_DECIMAL_ARRAY:
                output.writeFieldBegin(new TField(fieldName, TType.LIST, index));
                serialize((BigDecimal[]) input, output);
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

    private static String[] deserializeStringArray(final TProtocol input) throws TException {
        final String[] result = new String[input.readListBegin().size];
        for(int i = 0; i < result.length; i++)
            result[i] = input.readString();
        input.readListEnd();
        return result;
    }

    private static Date[] deserializeDateArray(final TProtocol input) throws TException {
        final Date[] result = new Date[input.readListBegin().size];
        for(int i = 0; i < result.length; i++)
            result[i] = new Date(input.readI64());
        input.readListEnd();
        return result;
    }

    private static ObjectName[] deserializeObjectNameArray(final TProtocol input) throws TException {
        final ObjectName[] result = new ObjectName[input.readListBegin().size];
        for(int i = 0; i < result.length; i++)
            try {
                result[i] = new ObjectName(input.readString());
            } catch (final MalformedObjectNameException e) {
                throw new TException(e);
            }
        input.readListEnd();
        return result;
    }

    private static boolean[] deserializeBoolArray(final TProtocol input) throws TException {
        final boolean[] result = new boolean[input.readListBegin().size];
        for(int i = 0; i < result.length; i++)
            result[i] = input.readBool();
        input.readListEnd();
        return result;
    }

    private static short[] deserializeShortArray(final TProtocol input) throws TException {
        final short[] result = new short[input.readListBegin().size];
        for(int i = 0; i < result.length; i++)
            result[i] = input.readI16();
        input.readListEnd();
        return result;
    }

    private static ShortBuffer deserializeShortBuffer(final TProtocol input) throws TException {
        final int count;
        final ShortBuffer result = Buffers.allocShortBuffer(count = input.readListBegin().size, false);
        for(int i = 0; i < count; i++)
            result.put(input.readI16());
        input.readListEnd();
        return result;
    }

    private static char[] deserializeCharArray(final TProtocol input) throws TException {
        return input.readString().toCharArray();
    }

    private static CharBuffer deserializeCharBuffer(final TProtocol input) throws TException {
        return Buffers.wrap(input.readString().toCharArray());
    }

    private static int[] deserializeIntArray(final TProtocol input) throws TException {
        final int[] result = new int[input.readListBegin().size];
        for(int i = 0; i < result.length; i++)
            result[i] = input.readI32();
        input.readListEnd();
        return result;
    }

    private static IntBuffer deserializeIntBuffer(final TProtocol input) throws TException {
        final int count;
        final IntBuffer result = Buffers.allocIntBuffer(count = input.readListBegin().size, false);
        for(int i = 0; i < count; i++)
            result.put(input.readI32());
        input.readListEnd();
        return result;
    }

    private static long[] deserializeLongArray(final TProtocol input) throws TException {
        final long[] result = new long[input.readListBegin().size];
        for(int i = 0; i < result.length; i++)
            result[i] = input.readI64();
        input.readListEnd();
        return result;
    }

    private static LongBuffer deserializeLongBuffer(final TProtocol input) throws TException {
        final int count;
        final LongBuffer result = Buffers.allocLongBuffer(count = input.readListBegin().size, false);
        for(int i = 0; i < count; i++)
            result.put(input.readI64());
        input.readListEnd();
        return result;
    }

    private static float[] deserializeFloatArray(final TProtocol input) throws TException {
        final float[] result = new float[input.readListBegin().size];
        for(int i = 0; i < result.length; i++)
            result[i] = (float)input.readDouble();
        input.readListEnd();
        return result;
    }

    private static FloatBuffer deserializeFloatBuffer(final TProtocol input) throws TException {
        final int count;
        final FloatBuffer result = Buffers.allocFloatBuffer(count = input.readListBegin().size, false);
        for(int i = 0; i < count; i++)
            result.put((float)input.readDouble());
        input.readListEnd();
        return result;
    }

    private static double[] deserializeDoubleArray(final TProtocol input) throws TException {
        final double[] result = new double[input.readListBegin().size];
        for(int i = 0; i < result.length; i++)
            result[i] = input.readDouble();
        input.readListEnd();
        return result;
    }

    private static DoubleBuffer deserializeDoubleBuffer(final TProtocol input) throws TException {
        final int count;
        final DoubleBuffer result = Buffers.allocDoubleBuffer(count = input.readListBegin().size, false);
        for(int i = 0; i < count; i++)
            result.put(input.readDouble());
        input.readListEnd();
        return result;
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
            case OBJECT_NAME:
                try {
                    return new ObjectName(input.readString());
                } catch (final MalformedObjectNameException e) {
                    throw new TException(e);
                }
            case SHORT_ARRAY:
                return deserializeShortArray(input);
            case WRAPPED_SHORT_ARRAY:
                return ArrayUtils.boxArray(deserializeShortArray(input));
            case SHORT_BUFFER:
                return deserializeShortBuffer(input);
            case CHAR_ARRAY:
                return deserializeCharArray(input);
            case WRAPPED_CHAR_ARRAY:
                return ArrayUtils.boxArray(deserializeCharArray(input));
            case CHAR_BUFFER:
                return deserializeCharBuffer(input);
            case INT_ARRAY:
                return deserializeIntArray(input);
            case INT_BUFFER:
                return deserializeIntBuffer(input);
            case WRAPPED_INT_ARRAY:
                return ArrayUtils.boxArray(deserializeIntArray(input));
            case LONG_ARRAY:
                return deserializeLongArray(input);
            case WRAPPED_LONG_ARRAY:
                return ArrayUtils.boxArray(deserializeLongArray(input));
            case LONG_BUFFER:
                return deserializeLongBuffer(input);
            case FLOAT_ARRAY:
                return deserializeFloatArray(input);
            case WRAPPED_FLOAT_ARRAY:
                return ArrayUtils.boxArray(deserializeFloatArray(input));
            case FLOAT_BUFFER:
                return deserializeFloatBuffer(input);
            case DOUBLE_ARRAY:
                return deserializeDoubleArray(input);
            case WRAPPED_DOUBLE_ARRAY:
                return ArrayUtils.boxArray(deserializeDoubleArray(input));
            case DOUBLE_BUFFER:
                return deserializeDoubleBuffer(input);
            case BOOL_ARRAY:
                return deserializeBoolArray(input);
            case WRAPPED_BOOL_ARRAY:
                return ArrayUtils.boxArray(deserializeBoolArray(input));
            case STRING_ARRAY:
                return deserializeStringArray(input);
            case DATE_ARRAY:
                return deserializeDateArray(input);
            case OBJECT_NAME_ARRAY:
                return deserializeObjectNameArray(input);
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
