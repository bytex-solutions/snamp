package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilerConfiguration;

import javax.jms.*;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Represents JMS data converted used to serialize/deserialize JMS messages into SNAMP type system.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public abstract class JMSDataConverter extends Script implements JMSMessageConverter, JMSAttributeConverter, JMSNotificationConverter {
    private static final String STORAGE_KEY_HEADER = "snampStorageKey";
    private static final String NOTIF_MESSAGE_HEADER = "snampMessage";
    private static final String CATEGORY_HEADER = "snampCategory";
    private static final String SEQNUM_HEADER = "snampSequenceNumber";

    protected JMSDataConverter(){

    }

    private static byte[] readByteArray(final BytesMessage message) throws JMSException {
        final byte[] buffer = new byte[512];
        try(final ByteArrayOutputStream output = new ByteArrayOutputStream(1024)){
            int count;
            while ((count = message.readBytes(buffer)) > 0)
                output.write(buffer, 0, count);
            return output.toByteArray();
        }catch (final IOException e){
            throw new JMSException(e.getMessage());
        }
    }

    public static JMSDataConverter createDefault(){
        return new JMSDataConverter() {
            @Override
            public Object run() {
                return null;
            }
        };
    }

    public static JMSDataConverter loadFrom(final File scriptFile, final ClassLoader caller) throws IOException, ResourceException, ScriptException {
        final Path path = Paths.get(scriptFile.toURI());
        final GroovyScriptEngine engine = new GroovyScriptEngine(new URL[]{path.getParent().toUri().toURL()}, caller);
        engine.getConfig().setScriptBaseClass(JMSDataConverter.class.getName());
        engine.getConfig().getOptimizationOptions().put("indy", true);
        setupClassPath(engine.getConfig());
        //setup classloader
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previousClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(engine.getGroovyClassLoader());
        final JMSDataConverter converter;
        try{
            converter = (JMSDataConverter) engine.createScript(scriptFile.getName(), new Binding());
        }
        finally {
            currentThread.setContextClassLoader(previousClassLoader);
        }
        converter.run();
        return converter;
    }

    private static void setupClassPath(final CompilerConfiguration config) {
        final List<String> classPath = config.getClasspath();
        final String javaClassPath = StandardSystemProperty.JAVA_CLASS_PATH.value();
        if (!Strings.isNullOrEmpty(javaClassPath)) {
            StringTokenizer tokenizer = new StringTokenizer(javaClassPath, File.pathSeparator);
            while (tokenizer.hasMoreTokens())
                classPath.add(tokenizer.nextToken());
        }
    }

    private static OpenDataException cannotConvert(final Message msg, final OpenType<?> type){
        return new OpenDataException(String.format("Message %s cannot be converted to %s", msg, type));
    }

    /**
     * Converts JMS message to {@link Byte}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public byte toByte(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return Byte.parseByte(((TextMessage)msg).getText());
        else if(msg instanceof BytesMessage)
            return ((BytesMessage)msg).readByte();
        else if(msg instanceof StreamMessage)
            return ((StreamMessage)msg).readByte();
        else throw cannotConvert(msg, SimpleType.BYTE);
    }

    /**
     * Converts JMS message to {@link Byte}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public boolean toBoolean(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return Boolean.parseBoolean(((TextMessage)msg).getText());
        else if(msg instanceof BytesMessage)
            return ((BytesMessage)msg).readBoolean();
        else if(msg instanceof StreamMessage)
            return ((StreamMessage)msg).readBoolean();
        else throw cannotConvert(msg, SimpleType.BOOLEAN);
    }

    /**
     * Converts JMS message to {@link String}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public String toString(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return ((TextMessage)msg).getText();
        else if(msg instanceof BytesMessage)
            return ((BytesMessage)msg).readUTF();
        else if(msg instanceof StreamMessage)
            return ((StreamMessage)msg).readString();
        else throw cannotConvert(msg, SimpleType.STRING);
    }

    /**
     * Converts JMS message to {@link Short}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public short toShort(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return Short.parseShort(((TextMessage)msg).getText());
        else if(msg instanceof BytesMessage)
            return ((BytesMessage)msg).readShort();
        else if(msg instanceof StreamMessage)
            return ((StreamMessage)msg).readShort();
        else throw cannotConvert(msg, SimpleType.SHORT);
    }

    /**
     * Converts JMS message to {@link Short}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public int toInteger(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return Integer.parseInt(((TextMessage)msg).getText());
        else if(msg instanceof BytesMessage)
            return ((BytesMessage)msg).readInt();
        else if(msg instanceof StreamMessage)
            return ((StreamMessage)msg).readInt();
        else throw cannotConvert(msg, SimpleType.INTEGER);
    }

    /**
     * Converts JMS message to {@link Long}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public long toLong(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return Long.parseLong(((TextMessage)msg).getText());
        else if(msg instanceof BytesMessage)
            return ((BytesMessage)msg).readLong();
        else if(msg instanceof StreamMessage)
            return ((StreamMessage)msg).readLong();
        else throw cannotConvert(msg, SimpleType.LONG);
    }

    /**
     * Converts JMS message to {@link Character}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public char toChar(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return ((TextMessage)msg).getText().charAt(0);
        else if(msg instanceof BytesMessage)
            return ((BytesMessage)msg).readChar();
        else if(msg instanceof StreamMessage)
            return ((StreamMessage)msg).readChar();
        else throw cannotConvert(msg, SimpleType.CHARACTER);
    }

    /**
     * Converts JMS message to {@link Date}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public Date toDate(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return new Date(Long.parseLong(((TextMessage)msg).getText()));
        else if(msg instanceof BytesMessage)
            return new Date(((BytesMessage)msg).readLong());
        else if(msg instanceof StreamMessage)
            return new Date(((StreamMessage)msg).readLong());
        else throw cannotConvert(msg, SimpleType.DATE);
    }

    /**
     * Converts JMS message to {@link Date}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public BigInteger toBigInt(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return new BigInteger(((TextMessage)msg).getText());
        else if(msg instanceof BytesMessage)
            return new BigInteger(readByteArray((BytesMessage)msg));
        else throw cannotConvert(msg, SimpleType.BIGINTEGER);
    }

    /**
     * Converts JMS message to byte[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public byte[] toByteArray(final Message msg) throws JMSException, OpenDataException{
        if(msg instanceof BytesMessage)
            return readByteArray((BytesMessage)msg);
        else throw cannotConvert(msg, WellKnownType.BYTE_ARRAY.getOpenType());
    }

    /**
     * Converts JMS message to {@link Date}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public BigDecimal toBigDecimal(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return new BigDecimal(((TextMessage)msg).getText());
        else throw cannotConvert(msg, SimpleType.BIGDECIMAL);
    }

    /**
     * Converts JMS message to {@link String}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public ObjectName toObjectName(final Message msg) throws JMSException, OpenDataException {
        try {
            return new ObjectName(toString(msg));
        } catch (final MalformedObjectNameException e) {
            throw new OpenDataException(e.getMessage());
        }
    }

    /**
     * Converts JMS message to int[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public int[] toIntArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toIntArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to {@link Integer}[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public Integer[] toWrappedIntArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toWrappedIntArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to short[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public short[] toShortArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toShortArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to {@link Short}[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public Short[] toWrappedShortArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toWrappedShortArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to short[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public char[] toCharArray(final Message msg) throws JMSException, OpenDataException{
        if(msg instanceof TextMessage)
            return ((TextMessage)msg).getText().toCharArray();
        else if(msg instanceof BytesMessage)
            return ((BytesMessage)msg).readUTF().toCharArray();
        else if(msg instanceof StreamMessage)
            return ((StreamMessage)msg).readString().toCharArray();
        else throw cannotConvert(msg, ArrayType.getPrimitiveArrayType(char[].class));
    }

    /**
     * Converts JMS message to {@link Short}[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public Character[] toWrappedCharArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.wrapArray(toCharArray(msg));
    }

    /**
     * Converts JMS message to {@link Long}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public float toFloat(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return Float.parseFloat(((TextMessage)msg).getText());
        else if(msg instanceof BytesMessage)
            return ((BytesMessage)msg).readFloat();
        else if(msg instanceof StreamMessage)
            return ((StreamMessage)msg).readFloat();
        else throw cannotConvert(msg, SimpleType.FLOAT);
    }

    /**
     * Converts JMS message to {@link Long}.
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public double toDouble(final Message msg) throws JMSException, OpenDataException {
        if(msg instanceof TextMessage)
            return Double.parseDouble(((TextMessage)msg).getText());
        else if(msg instanceof BytesMessage)
            return ((BytesMessage)msg).readDouble();
        else if(msg instanceof StreamMessage)
            return ((StreamMessage)msg).readDouble();
        else throw cannotConvert(msg, SimpleType.DOUBLE);
    }

    /**
     * Converts JMS message to long[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public long[] toLongArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toLongArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to {@link Long}[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public Long[] toWrappedLongArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toWrappedLongArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to float[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public float[] toFloatArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toFloatArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to {@link Float}[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public Float[] toWrappedFloatArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toWrappedFloatArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to double[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public double[] toDoubleArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toDoubleArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to {@link Double}[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public Double[] toWrappedDoubleArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toWrappedDoubleArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to boolean[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public boolean[] toBoolArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toBoolArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to {@link Boolean}[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public Boolean[] toWrappedBoolArray(final Message msg) throws JMSException, OpenDataException{
        return ArrayUtils.toWrappedBoolArray(toByteArray(msg));
    }

    /**
     * Converts JMS message to {@link Date}[].
     * @param msg A message to deserialize.
     * @return Conversion result.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Unable to convert message.
     */
    public Date[] toDateArray(final Message msg) throws JMSException, OpenDataException{
        final long[] input = ArrayUtils.toLongArray(toByteArray(msg));
        final Date[] result = new Date[input.length];
        for(int i = 0; i < input.length; i++)
            result[i] = new Date(input[i]);
        return result;
    }

    public CompositeData toCompositeData(final Message msg, final CompositeType type) throws OpenDataException, JMSException {
        if (msg instanceof MapMessage) {
            final MapMessage map = (MapMessage) msg;
            final Map<String, Object> result = Maps.newHashMap();
            for (final String itemName : type.keySet())
                result.put(itemName, map.getObject(itemName));
            return new CompositeDataSupport(type, result);
        } else throw cannotConvert(msg, type);
    }

    @Override
    public final Object deserialize(final Message message, final OpenType<?> type) throws JMSException, OpenDataException{
        if(message instanceof ObjectMessage)
            return ((ObjectMessage)message).getObject();
        else switch (WellKnownType.getType(type)){
            case STRING:
                return toString(message);
            case BYTE:
                return toByte(message);
            case SHORT:
                return toShort(message);
            case INT:
                return toInteger(message);
            case LONG:
                return toLong(message);
            case CHAR:
                return toChar(message);
            case DATE:
                return toDate(message);
            case BOOL:
                return toBoolean(message);
            case FLOAT:
                return toFloat(message);
            case DOUBLE:
                return toDouble(message);
            case BIG_INT:
                return toBigInt(message);
            case BIG_DECIMAL:
                return toBigDecimal(message);
            case OBJECT_NAME:
                return toObjectName(message);
            case BYTE_ARRAY:
                return toByteArray(message);
            case WRAPPED_BYTE_ARRAY:
                return ArrayUtils.wrapArray(toByteArray(message));
            case INT_ARRAY:
                return toIntArray(message);
            case WRAPPED_INT_ARRAY:
                return toWrappedIntArray(message);
            case SHORT_ARRAY:
                return toShortArray(message);
            case WRAPPED_SHORT_ARRAY:
                return toWrappedShortArray(message);
            case CHAR_ARRAY:
                return toCharArray(message);
            case WRAPPED_CHAR_ARRAY:
                return toWrappedCharArray(message);
            case LONG_ARRAY:
                return toLongArray(message);
            case WRAPPED_LONG_ARRAY:
                return toWrappedLongArray(message);
            case FLOAT_ARRAY:
                return toFloatArray(message);
            case WRAPPED_FLOAT_ARRAY:
                return toWrappedFloatArray(message);
            case DOUBLE_ARRAY:
                return toDoubleArray(message);
            case WRAPPED_DOUBLE_ARRAY:
                return toWrappedDoubleArray(message);
            case BOOL_ARRAY:
                return toBoolArray(message);
            case WRAPPED_BOOL_ARRAY:
                return toWrappedBoolArray(message);
            case DATE_ARRAY:
                return toDateArray(message);
            case DICTIONARY:
                return toCompositeData(message, (CompositeType)type);
            default:
                throw cannotConvert(message, type);
        }
    }

    /**
     * Converts {@link Byte} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromByte(final byte value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeByte(value);
        return result;
    }

    /**
     * Converts {@link Integer} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromInt(final int value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeInt(value);
        return result;
    }

    /**
     * Converts {@link Long} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromLong(final long value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeLong(value);
        return result;
    }

    /**
     * Converts {@link Short} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromShort(final short value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeShort(value);
        return result;
    }

    /**
     * Converts {@link Character} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromChar(final char value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeChar(value);
        return result;
    }

    /**
     * Converts {@link Boolean} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromBoolean(final boolean value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBoolean(value);
        return result;
    }

    /**
     * Converts {@link Float} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromFloat(final float value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeFloat(value);
        return result;
    }

    /**
     * Converts {@link Double} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromDouble(final double value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeDouble(value);
        return result;
    }

    /**
     * Converts {@link Byte}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromByteArray(final byte[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(value);
        return result;
    }

    /**
     * Converts {@link String}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromString(final String value, final Session session) throws JMSException {
        return session.createTextMessage(value);
    }

    /**
     * Converts {@link ObjectName} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromObjectName(final ObjectName value, final Session session) throws JMSException{
        return session.createTextMessage(value.getCanonicalName());
    }

    /**
     * Converts {@link Integer}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromIntArray(final int[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Integer}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromIntArray(final Integer[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link CompositeData}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromCompositeData(final CompositeData value, final Session session) throws JMSException{
        final MapMessage result = session.createMapMessage();
        for(final String itemName: value.getCompositeType().keySet())
            result.setObject(itemName, value.get(itemName));
        return result;
    }

    /**
     * Converts {@link Date} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromDate(final Date value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeLong(value.getTime());
        return result;
    }

    /**
     * Converts {@link BigInteger} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromBigInt(final BigInteger value, final Session session) throws JMSException{
        return session.createTextMessage(value.toString());
    }

    /**
     * Converts {@link BigDecimal} to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromBigDecimal(final BigDecimal value, final Session session) throws JMSException{
        return session.createTextMessage(value.toString());
    }

    /**
     * Converts {@link Short}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromShortArray(final short[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Integer}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromShortArray(final Short[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Long}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromLongArray(final long[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Long}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromLongArray(final Long[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Boolean}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromBoolArray(final boolean[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Boolean}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromBoolArray(final Boolean[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Character}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromCharArray(final char[] value, final Session session) throws JMSException{
        return session.createTextMessage(new String(value));
    }

    /**
     * Converts {@link Character}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromCharArray(final Character[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Float}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromFloatArray(final float[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Float}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromFloatArray(final Float[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Double}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromDoubleArray(final double[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Double}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromDoubleArray(final Double[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        result.writeBytes(ArrayUtils.toByteArray(value));
        return result;
    }

    /**
     * Converts {@link Date}[] to message.
     * @param value The value to convert.
     * @param session Message factory.
     * @return A message with serialized payload.
     * @throws JMSException Internal JMS error.
     */
    public Message fromDateArray(final Date[] value, final Session session) throws JMSException{
        final BytesMessage result = session.createBytesMessage();
        final long[] values = new long[value.length];
        for(int i = 0; i < values.length; i++)
            values[i] = value[i].getTime();
        result.writeBytes(ArrayUtils.toByteArray(values));
        return result;
    }

    @Override
    public final Message serialize(final Object value, final Session session) throws JMSException{
        switch (WellKnownType.fromValue(value)){
            case STRING:
                return fromString((String)value, session);
            case BYTE:
                return fromByte((Byte)value, session);
            case SHORT:
                return fromShort((Short)value, session);
            case INT:
                return fromInt((Integer)value, session);
            case LONG:
                return fromLong((Long)value, session);
            case CHAR:
                return fromChar((Character)value, session);
            case BOOL:
                return fromBoolean((Boolean)value, session);
            case FLOAT:
                return fromFloat((Float)value, session);
            case DOUBLE:
                return fromDouble((Double)value, session);
            case BIG_INT:
                return fromBigInt((BigInteger)value, session);
            case BIG_DECIMAL:
                return fromBigDecimal((BigDecimal)value, session);
            case OBJECT_NAME:
                return fromObjectName((ObjectName)value, session);
            case DICTIONARY:
                return fromCompositeData((CompositeData)value, session);
            case DATE:
                return fromDate((Date)value, session);
            case BYTE_ARRAY:
                return fromByteArray((byte[])value, session);
            case WRAPPED_BYTE_ARRAY:
                return fromByteArray(ArrayUtils.unwrapArray((Byte[])value), session);
            case INT_ARRAY:
                return fromIntArray((int[])value, session);
            case WRAPPED_INT_ARRAY:
                return fromIntArray((Integer[])value, session);
            case SHORT_ARRAY:
                return fromShortArray((short[])value, session);
            case WRAPPED_SHORT_ARRAY:
                return fromShortArray((Short[])value, session);
            case LONG_ARRAY:
                return fromLongArray((long[])value, session);
            case WRAPPED_LONG_ARRAY:
                return fromLongArray((Long[])value, session);
            case BOOL_ARRAY:
                return fromBoolArray((boolean[])value, session);
            case WRAPPED_BOOL_ARRAY:
                return fromBoolArray((Boolean[])value, session);
            case CHAR_ARRAY:
                return fromCharArray((char[])value, session);
            case WRAPPED_CHAR_ARRAY:
                return fromCharArray((Character[])value, session);
            case FLOAT_ARRAY:
                return fromFloatArray((float[])value, session);
            case WRAPPED_FLOAT_ARRAY:
                return fromFloatArray((Float[])value, session);
            case DOUBLE_ARRAY:
                return fromDoubleArray((double[])value, session);
            case WRAPPED_DOUBLE_ARRAY:
                return fromDoubleArray((Double[])value, session);
            case DATE_ARRAY:
                return fromDateArray((Date[])value, session);
            default:
                return value instanceof Serializable ?
                        session.createObjectMessage((Serializable)value) :
                        session.createTextMessage(Objects.toString(value));
        }
    }

    /**
     * Detects message type.
     * @param message A message received from the queue
     * @return SNAMP-specific message type.
     * @throws JMSException
     */
    @Override
    public SnampMessageType getMessageType(final Message message) throws JMSException{
        final String messageType = message.getJMSType();
        if(Strings.isNullOrEmpty(messageType)) return SnampMessageType.WRITE;
        else switch (messageType) {
            default:
            case "write":
                return SnampMessageType.WRITE;
            case "notify":
                return SnampMessageType.NOTIFICATION;
            case "attributeChanged":
                return SnampMessageType.ATTRIBUTE_CHANGED;
        }
    }

    @Override
    public void setMessageType(final Message message, final SnampMessageType messageType) throws JMSException{
        switch (messageType){
            case ATTRIBUTE_CHANGED:
                message.setJMSType("attributeChanged");
                return;
            case WRITE:
                message.setJMSType("write");
                return;
            case NOTIFICATION:
                message.setJMSType("notify");
        }
    }

    @Override
    public String getStorageKey(final Message message) throws JMSException{
        return message.getStringProperty(STORAGE_KEY_HEADER);
    }

    @Override
    public void setStorageKey(final Message message, final String storageKey) throws JMSException{
        message.setStringProperty(STORAGE_KEY_HEADER, storageKey);
    }

    /**
     * Parses message payload.
     *
     * @param message JMS message to convert.
     * @return Human-readable message associated with notification.
     * @throws JMSException Internal JMS error.
     */
    @Override
    public String getMessage(final Message message) throws JMSException {
        return message.getStringProperty(NOTIF_MESSAGE_HEADER);
    }

    @Override
    public String getCategory(final Message message) throws JMSException {
        return message.getStringProperty(CATEGORY_HEADER);
    }

    @Override
    public long getSequenceNumber(final Message message) throws JMSException {
        return message.getLongProperty(SEQNUM_HEADER);
    }
}
