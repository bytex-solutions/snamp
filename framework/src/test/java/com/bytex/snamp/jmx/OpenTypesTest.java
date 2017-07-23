package com.bytex.snamp.jmx;

import org.junit.Assert;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class OpenTypesTest extends Assert {
    @Test
    public void primitivesTest(){
        for(final WellKnownType primitive: WellKnownType.getSimpleTypes()) {
            if(primitive.equals(WellKnownType.VOID)) continue;
            final Object defval = OpenTypes.defaultValue(primitive.getOpenType());
            assertNotNull(defval);
            assertTrue(String.format("Prim: %s, value: %s", primitive, defval), primitive.test(defval));
        }
    }

    @Test
    public void typeTest() throws OpenDataException {
        final CompositeType type = new CompositeTypeBuilder("t", "d")
                .addItem("f", "d", SimpleType.INTEGER)
                .addItem("f2", "d", SimpleType.DOUBLE)
                .build();
        assertEquals(CompositeData.class, OpenTypes.getType(type));
        assertEquals(byte[].class, OpenTypes.getType(new ArrayType<byte[]>(SimpleType.BYTE, true)));
        assertEquals(Byte[].class, OpenTypes.getType(new ArrayType<Byte[]>(SimpleType.BYTE, false)));
    }

    @Test
    public void openTypeArrayTest() throws OpenDataException {
        Object array = OpenTypes.newArray(new ArrayType<boolean[]>(SimpleType.BOOLEAN, true), 10);
        assertTrue(array != null);
        assertEquals(10, Array.getLength(array));
        array = OpenTypes.newArray(new ArrayType<Boolean[]>(SimpleType.BOOLEAN, false), 11);
        assertTrue(array != null);
        assertEquals(11, Array.getLength(array));
        array = OpenTypes.newArray(new ArrayType<String[]>(SimpleType.STRING, false), 5);
        assertTrue(array != null);
        assertEquals(5, Array.getLength(array));
        final CompositeType ct = new CompositeTypeBuilder("dummyType", "dummy")
                .addItem("x", "X coordinate", SimpleType.LONG)
                .addItem("y", "Y coordinate", SimpleType.LONG)
                .build();
        array = OpenTypes.newArray(new ArrayType<CompositeData[]>(1, ct), 7);
        assertTrue(array != null);
        assertEquals(7, Array.getLength(array));
    }

    @Test
    public void arrayTest() throws OpenDataException {
        final short[] array1 = OpenTypes.defaultValue(new ArrayType<short[]>(SimpleType.SHORT, true));
        assertTrue(array1.length == 0);
        final Short[] array2 = OpenTypes.defaultValue(new ArrayType<Short[]>(SimpleType.SHORT, false));
        assertTrue(array2.length == 0);
    }

    @Test
    public void compositeTypeTest() throws OpenDataException, MalformedObjectNameException {
        final CompositeType type = new CompositeTypeBuilder("Dummy", "DummyDesc")
                .addItem("x", "dummy", SimpleType.STRING)
                .addItem("y", "dummy", SimpleType.OBJECTNAME)
                .addItem("z", "dummy", SimpleType.BIGDECIMAL)
                .addItem("j", "dummy", SimpleType.DATE)
                .addItem("i", "dummy", SimpleType.INTEGER)
                .build();
        final CompositeData data = OpenTypes.defaultValue(type);
        assertEquals("", data.get("x"));
        assertEquals(new ObjectName(""), data.get("y"));
        assertEquals(BigDecimal.ZERO, data.get("z"));
        assertEquals(new Date(0L), data.get("j"));
        assertEquals(0, data.get("i"));
    }
}
