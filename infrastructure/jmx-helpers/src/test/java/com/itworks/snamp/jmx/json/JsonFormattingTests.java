package com.itworks.snamp.jmx.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itworks.snamp.io.Buffers;
import com.itworks.snamp.jmx.CompositeDataBuilder;
import com.itworks.snamp.jmx.TabularTypeBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JsonFormattingTests extends Assert {
    @Test
    public void byteBufferTest(){
        final ByteBuffer buffer = Buffers.wrap((byte) 1, (byte) 2, (byte) 3);
        final Gson formatter = new GsonBuilder()
                .registerTypeHierarchyAdapter(ByteBuffer.class, new ByteBufferFormatter())
                .create();
        final JsonElement bytes = formatter.toJsonTree(buffer);
        assertTrue(bytes.isJsonArray());
        assertEquals(3, bytes.getAsJsonArray().size());
        assertEquals(2, bytes.getAsJsonArray().get(1).getAsByte());
    }

    @Test
    public void longBufferTest(){
        LongBuffer buffer = Buffers.wrap(1L, 2L, 3L);
        final Gson formatter = new GsonBuilder()
                .registerTypeHierarchyAdapter(LongBuffer.class, new LongBufferFormatter())
                .create();
        final JsonElement items = formatter.toJsonTree(buffer);
        assertTrue(items.isJsonArray());
        assertEquals(3, items.getAsJsonArray().size());
        assertEquals(2L, items.getAsJsonArray().get(1).getAsLong());
        buffer.rewind();
        buffer = formatter.fromJson(items, LongBuffer.class);
        assertArrayEquals(new long[]{1, 2, 3}, Buffers.readRemaining(buffer));
    }

    @Test
    public void objectNameTest() throws MalformedObjectNameException {
        final Gson formatter = new GsonBuilder()
                .registerTypeAdapter(ObjectName.class, new ObjectNameFormatter())
                .create();
        final ObjectName name = new ObjectName("com.itworks.snamp.testing:type=TestOpenMBean");
        final JsonElement element = formatter.toJsonTree(name);
        assertTrue(element.isJsonPrimitive());
        assertEquals("com.itworks.snamp.testing:type=TestOpenMBean", element.getAsString());
        assertEquals(name, formatter.fromJson(element, ObjectName.class));
    }

    @Test
    public void compositeDataTest() throws OpenDataException {
        CompositeData data = new CompositeDataBuilder()
                .setTypeName("dict")
                .setTypeDescription("dict")
                .put("item1", "Dummy item", 2)
                .build();
        final Gson formatter = new GsonBuilder()
                .registerTypeHierarchyAdapter(CompositeData.class, new CompositeDataFormatter())
                .create();
        final JsonElement elem = formatter.toJsonTree(data);
        assertTrue(elem.isJsonObject());
        assertEquals(2, elem.getAsJsonObject().getAsJsonObject("value").getAsJsonPrimitive("item1").getAsInt());
        data = formatter.fromJson(elem, CompositeData.class);
        assertNotNull(data);
    }

    @Test
    public void simpleTypeFormattingTest() throws IllegalAccessException {
        for(final Field fld: SimpleType.class.getDeclaredFields())
            if(Modifier.isStatic(fld.getModifiers()) && Modifier.isPublic(fld.getModifiers()) && fld.getDeclaringClass().equals(fld.getType())){
                final SimpleType<?> type = (SimpleType<?>) fld.get(null);
                final JsonElement json = OpenTypeFormatter.serialize(type);
                assertTrue("Invalid JSON for" + type, json.isJsonPrimitive());
                assertEquals(type, OpenTypeFormatter.deserialize(json));
            }
    }

    @Test
    public void arrayTypeFormattingTest() throws OpenDataException {
        ArrayType<boolean[]> arrayType = new ArrayType<>(SimpleType.BOOLEAN, true);
        JsonObject json = OpenTypeFormatter.serialize(arrayType);
        assertEquals(arrayType, OpenTypeFormatter.deserialize(json));
        arrayType = new ArrayType<>(SimpleType.BOOLEAN, false);
        json = OpenTypeFormatter.serialize(arrayType);
        assertEquals(arrayType, OpenTypeFormatter.deserialize(json));
    }

    @Test
    public void tabularTypeFormattingTest() throws OpenDataException{
        final TabularType type = new TabularTypeBuilder()
                .setTypeName("DummyTable", true)
                .setDescription("Descr", true)
                .addColumn("column1", "desc", SimpleType.STRING, true)
                .addColumn("column2", "desc", SimpleType.BOOLEAN, true)
                .addColumn("column3", "desc", SimpleType.OBJECTNAME, false)
                .build();
        final JsonObject json = OpenTypeFormatter.serialize(type);
        assertEquals(type, OpenTypeFormatter.deserialize(json));
    }
}