package com.bytex.snamp.json;

import com.bytex.snamp.io.Buffers;
import com.bytex.snamp.jmx.CompositeDataBuilder;
import com.bytex.snamp.jmx.TabularDataBuilder;
import com.bytex.snamp.jmx.TabularTypeBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.*;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class JsonFormattingTests extends Assert {
    private final ObjectMapper mapper;

    public JsonFormattingTests(){
        mapper = new ObjectMapper();
        mapper.registerModule(new JsonUtils());
    }

    @Test
    public void byteBufferTest() throws IOException {
        ByteBuffer buffer = Buffers.wrap((byte) 1, (byte) 2, (byte) 3);
        final String json = mapper.writeValueAsString(buffer);
        assertNotNull(json);
        buffer = mapper.readValue(json, ByteBuffer.class);
        assertNotNull(buffer);
        assertEquals(3, buffer.capacity());
    }

    @Test
    public void charBufferTest() throws IOException {
        CharBuffer buffer = Buffers.wrap('a', 'b', 'c');
        final String json = mapper.writeValueAsString(buffer);
        assertNotNull(json);
        buffer = mapper.readValue(json, CharBuffer.class);
        assertNotNull(buffer);
        assertEquals(3, buffer.capacity());
    }

    @Test
    public void shortBufferTest() throws IOException {
        ShortBuffer buffer = Buffers.wrap((short) 1, (short) 2, (short) 3);
        final String json = mapper.writeValueAsString(buffer);
        assertNotNull(json);
        buffer = mapper.readValue(json, ShortBuffer.class);
        assertNotNull(buffer);
        assertEquals(3, buffer.capacity());
    }

    @Test
    public void intBufferTest() throws IOException {
        IntBuffer buffer = Buffers.wrap(1,  2,  3);
        final String json = mapper.writeValueAsString(buffer);
        assertNotNull(json);
        buffer = mapper.readValue(json, IntBuffer.class);
        assertNotNull(buffer);
        assertEquals(3, buffer.capacity());
    }

    @Test
    public void longBufferTest() throws IOException {
        LongBuffer buffer = Buffers.wrap(1L,  2L,  3L);
        final String json = mapper.writeValueAsString(buffer);
        assertNotNull(json);
        buffer = mapper.readValue(json, LongBuffer.class);
        assertNotNull(buffer);
        assertEquals(3, buffer.capacity());
    }

    @Test
    public void objectNameTest() throws IOException, MalformedObjectNameException {
        ObjectName name = new ObjectName("com.bytex.snamp.testing:type=TestOpenMBean");
        String json = mapper.writeValueAsString(name);
        assertNotNull(json);
        assertEquals(name, mapper.readValue(json, ObjectName.class));
    }

    @Test
    public void compositeDataTest() throws OpenDataException, IOException {
        final CompositeData data = new CompositeDataBuilder()
                .setTypeName("dict")
                .setTypeDescription("dict")
                .put("item1", "Dummy item", 2)
                .build();
        final String json = mapper.writeValueAsString(data);
        assertNotNull(json);
        assertEquals(data, mapper.readValue(json, CompositeData.class));
    }

    @Test
    public void simpleTypeFormattingTest() throws IllegalAccessException, IOException {
        for (final Field fld : SimpleType.class.getDeclaredFields())
            if (Modifier.isStatic(fld.getModifiers()) && Modifier.isPublic(fld.getModifiers()) && fld.getDeclaringClass().equals(fld.getType())) {
                final SimpleType<?> type = (SimpleType<?>) fld.get(null);
                final String json = mapper.writeValueAsString(type);
                assertEquals(type, mapper.readValue(json, OpenType.class));
            }
    }

    @Test
    public void arrayTypeFormattingTest() throws OpenDataException, IOException {
        final ArrayType<boolean[]> arrayType = new ArrayType<>(SimpleType.BOOLEAN, true);
        final String json = mapper.writeValueAsString(arrayType);
        assertNotNull(json);
        assertEquals(arrayType, mapper.readValue(json, OpenType.class));
    }

    @Test
    public void tabularTypeFormattingTest() throws OpenDataException, IOException {
        final TabularType type = new TabularTypeBuilder()
                .setTypeName("DummyTable", true)
                .setDescription("Descr", true)
                .addColumn("column1", "desc", SimpleType.STRING, true)
                .addColumn("column2", "desc", SimpleType.BOOLEAN, true)
                .addColumn("column3", "desc", SimpleType.OBJECTNAME, false)
                .build();
        final String json = mapper.writeValueAsString(type);
        assertEquals(type, mapper.readValue(json, OpenType.class));
    }

    @Test
    public void tabularDataTest() throws OpenDataException, IOException {
        final TabularData data = new TabularDataBuilder()
                .setTypeName("TestTable", true)
                .setTypeDescription("Descr", true)
                .declareColumns(columns -> columns
                    .addColumn("column1", "column1 descr", SimpleType.INTEGER, true)
                    .addColumn("column2", "column2 descr", SimpleType.STRING, false)
                )
                .add(42, "String1")
                .add(43, "String2")
                .build();
        final String json = mapper.writeValueAsString(data);
        assertNotNull(json);
        assertEquals(data, mapper.readValue(json, TabularData.class));
    }

    @Test
    public void notificationTest() throws OpenDataException, IOException {
        final Notification notif = new Notification(AttributeChangeNotification.ATTRIBUTE_CHANGE,
                "java-app-server",
                5L,
                System.currentTimeMillis(),
                "Log level changed");
        notif.setUserData(new CompositeDataBuilder()
                .setTypeName("dict")
                .setTypeDescription("dict")
                .put("item1", "Dummy item", 2)
                .build());
        final String json = mapper.writeValueAsString(notif);
        assertNotNull(json);
    }
}
