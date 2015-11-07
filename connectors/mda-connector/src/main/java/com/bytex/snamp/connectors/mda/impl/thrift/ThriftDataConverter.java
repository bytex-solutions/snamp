package com.bytex.snamp.connectors.mda.impl.thrift;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.io.Buffers;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ThriftDataConverter {
    private ThriftDataConverter(){

    }

    private static byte getType(final WellKnownType type){
        switch (type){
            case DICTIONARY:
                return TType.STRUCT;
            case BYTE:
                return TType.BYTE;
            case BOOL:
                return TType.BOOL;
            case SHORT:
                return TType.I16;
            case INT:
                return TType.I32;
            case FLOAT:
            case DOUBLE:
                return TType.DOUBLE;
            case DATE:
            case LONG:
                return TType.I64;
            case SHORT_BUFFER:
            case SHORT_ARRAY:
            case WRAPPED_SHORT_ARRAY:
            case INT_BUFFER:
            case INT_ARRAY:
            case WRAPPED_INT_ARRAY:
            case LONG_BUFFER:
            case LONG_ARRAY:
            case WRAPPED_LONG_ARRAY:
            case FLOAT_BUFFER:
            case FLOAT_ARRAY:
            case WRAPPED_FLOAT_ARRAY:
            case DOUBLE_ARRAY:
            case WRAPPED_DOUBLE_ARRAY:
            case DOUBLE_BUFFER:
            case WRAPPED_BOOL_ARRAY:
            case BOOL_ARRAY:
            case STRING_ARRAY:
            case OBJECT_NAME_ARRAY:
            case BIG_INT_ARRAY:
            case BIG_DECIMAL_ARRAY:
            case DATE_ARRAY:
                return TType.LIST;
            case STRING:
            case BIG_INT:
            case BIG_DECIMAL:
            case CHAR:
            case OBJECT_NAME:
            case CHAR_ARRAY:
            case CHAR_BUFFER:
            case WRAPPED_CHAR_ARRAY:
            case BYTE_ARRAY:
            case BYTE_BUFFER:
            case WRAPPED_BYTE_ARRAY:
            default:
                return TType.STRING;
        }
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

    private static String[] getSortedItems(final CompositeType type){
        final String[] sortedItems = ArrayUtils.toArray(type.keySet(), String.class);
        Arrays.sort(sortedItems);
        return sortedItems;
    }

    private static void serialize(final CompositeData input, final TProtocol output) throws TException{
        final TStruct struct = new TStruct(input.getCompositeType().getTypeName());
        final String[] sortedItems = getSortedItems(input.getCompositeType());
        Arrays.sort(sortedItems);
        short index = 1;
        output.writeStructBegin(struct);
        for(final String itemName: sortedItems){
            output.writeFieldBegin(new TField(itemName, getType(input.getCompositeType().getType(itemName)), index++));
            serialize(input.get(itemName), output);
            output.writeFieldEnd();
        }
        output.writeFieldStop();
        output.writeStructEnd();
    }

    static byte getType(final OpenType<?> type){
        return getType(WellKnownType.getType(type));
    }

    static void serialize(final Object input, final TProtocol output) throws TException {
        switch (WellKnownType.fromValue(input)){
            case DICTIONARY:
                serialize((CompositeData)input, output);
                return;
            case BOOL:
                output.writeBool((boolean) input);
                return;
            case BYTE:
                output.writeByte((byte) input);
                return;
            case SHORT:
                output.writeI16((short) input);
                return;
            case INT:
                output.writeI32((int) input);
                return;
            case LONG:
                output.writeI64((long) input);
                return;
            case FLOAT:
                output.writeDouble((float) input);
                return;
            case DOUBLE:
                output.writeDouble((double) input);
                return;
            case DATE:
                output.writeI64(((Date) input).getTime());
                return;
            case BYTE_ARRAY:
                output.writeBinary(Buffers.wrap((byte[]) input));
                return;
            case WRAPPED_BYTE_ARRAY:
                output.writeBinary(Buffers.wrap(ArrayUtils.unboxArray((Byte[]) input)));
                return;
            case BYTE_BUFFER:
                output.writeBinary((ByteBuffer) input);
                return;
            case SHORT_ARRAY:
                serialize((short[]) input, output);
                return;
            case WRAPPED_SHORT_ARRAY:
                serialize(ArrayUtils.unboxArray((Short[]) input), output);
                return;
            case SHORT_BUFFER:
                serialize((ShortBuffer) input, output);
                return;
            case INT_ARRAY:
                serialize((int[]) input, output);
                return;
            case WRAPPED_INT_ARRAY:
                serialize(ArrayUtils.unboxArray((Integer[]) input), output);
                return;
            case INT_BUFFER:
                serialize((IntBuffer) input, output);
                return;
            case LONG_ARRAY:
                serialize((long[]) input, output);
                return;
            case WRAPPED_LONG_ARRAY:
                serialize(ArrayUtils.unboxArray((Long[]) input), output);
                return;
            case LONG_BUFFER:
                serialize((LongBuffer) input, output);
                return;
            case FLOAT_ARRAY:
                serialize((float[]) input, output);
                return;
            case WRAPPED_FLOAT_ARRAY:
                serialize(ArrayUtils.unboxArray((Float[]) input), output);
                return;
            case FLOAT_BUFFER:
                serialize((FloatBuffer) input, output);
                return;
            case DOUBLE_ARRAY:
                serialize((double[]) input, output);
                return;
            case WRAPPED_DOUBLE_ARRAY:
                serialize(ArrayUtils.unboxArray((Double[]) input), output);
                return;
            case DOUBLE_BUFFER:
                serialize((DoubleBuffer) input, output);
                return;
            case CHAR_ARRAY:
                serialize((char[]) input, output);
                return;
            case WRAPPED_CHAR_ARRAY:
                serialize(ArrayUtils.unboxArray((Character[]) input), output);
                return;
            case CHAR_BUFFER:
                serialize((CharBuffer) input, output);
                return;
            case BOOL_ARRAY:
                serialize((boolean[]) input, output);
                return;
            case WRAPPED_BOOL_ARRAY:
                serialize(ArrayUtils.unboxArray((Boolean[]) input), output);
                return;
            case STRING_ARRAY:
                serialize((String[]) input, output);
                return;
            case OBJECT_NAME_ARRAY:
                serialize((ObjectName[]) input, output);
                return;
            case DATE_ARRAY:
                serialize((Date[]) input, output);
                return;
            case BIG_INT_ARRAY:
                serialize((BigInteger[]) input, output);
                return;
            case BIG_DECIMAL_ARRAY:
                serialize((BigDecimal[]) input, output);
                return;
            case OBJECT_NAME:
                output.writeString(((ObjectName) input).getCanonicalName());
                return;
            case STRING:
            case BIG_INT:
            case BIG_DECIMAL:
            case CHAR:
            default:
                output.writeString(String.valueOf(input));
        }
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

    private static CompositeData deserialize(final CompositeType type, final TProtocol input) throws TException {
        final String[] sortedNames = getSortedItems(type);
        final Map<String, Object> items = Maps.newHashMapWithExpectedSize(sortedNames.length);
        input.readStructBegin();
        while (true){
            final TField field = input.readFieldBegin();
            if(field.type == TType.STOP) break;
            final String itemName = sortedNames[field.id - 1];
            items.put(itemName, deserialize(type.getType(itemName), input));
            input.readFieldEnd();
        }
        input.readStructEnd();

        try {
            return new CompositeDataSupport(type, items);
        } catch (final OpenDataException e) {
            throw new TException(e);
        }
    }

    static Object deserialize(final OpenType<?> type, final TProtocol input) throws TException {
        switch (WellKnownType.getType(type)) {
            case DICTIONARY:
                return deserialize((CompositeType)type, input);
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
}
