package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilerConfiguration;

import javax.jms.*;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Represents JMS data converted used to serialize/deserialize JMS messages into SNAMP type system.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class JMSDataConverter extends Script {

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

    final Object deserialize(final Message message, final OpenType<?> type) throws JMSException, OpenDataException{
        if(message instanceof ObjectMessage)
            return ((ObjectMessage)message).getObject();
        else switch (WellKnownType.getType(type)){
            case STRING:
                return toString(message);
            case BYTE:
                return toByte(message);
            case BOOL:
                return toBoolean(message);
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

    final Message serialize(final Object value, final Session session) throws JMSException{
        switch (WellKnownType.fromValue(value)){
            case STRING:
                return fromString((String)value, session);
            case BYTE:
                return fromByte((Byte)value, session);
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
                return fromIntArray(ArrayUtils.unwrapArray((Integer[])value), session);
            default:
                return session.createTextMessage(Objects.toString(value));
        }
    }

    /**
     * Detects message type.
     * @param message A message received from the queue
     * @return SNAMP-specific message type.
     * @throws JMSException
     */
    public SnampMessageType getMessageType(final Message message) throws JMSException{
        final String messageType = message.getJMSType();
        if(Strings.isNullOrEmpty(messageType)) return SnampMessageType.WRITE;
        else switch (messageType) {
            default:
            case "write":
                return SnampMessageType.WRITE;
            case "notify":
                return SnampMessageType.NOTIFICATION;
        }
    }
}
