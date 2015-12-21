package com.bytex.snamp.jmx;

import com.bytex.snamp.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.ArrayType;
import java.lang.reflect.Array;
import java.nio.Buffer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class WellKnownTypeTest extends Assert {

    @Test
    public void byteTypeTest(){
        assertTrue(WellKnownType.BYTE.isInstance((byte) 2));
        assertEquals(WellKnownType.BYTE, WellKnownType.fromValue((byte)3));
    }

    @Test
    public void allTypesTest(){
        for(final WellKnownType type: WellKnownType.values()){
            assertEquals(type, WellKnownType.getType(type.getJavaType()));
            if(type.isOpenType())
                assertEquals(type, WellKnownType.getType(type.getOpenType()));
            assertEquals(type, WellKnownType.getType(type.getJavaType().getName()));
        }
    }

    @Test
    public void openTypesTest(){
        for(final WellKnownType type: WellKnownType.getOpenTypes()){
            assertTrue(type.isOpenType());
            assertNotNull(type.getOpenType());
        }
    }

    @Test
    public void arrayTypesTest(){
        for(final WellKnownType type: WellKnownType.getArrayTypes()){
            final Object emptyArray = ArrayUtils.emptyArray(type.getJavaType());
            assertTrue(type.isArray());
            assertEquals(0, Array.getLength(emptyArray));
        }
        for(final WellKnownType type: WellKnownType.getArrayOpenTypes()){
            assertTrue("Not array: " + type, type.getOpenType() instanceof ArrayType<?>);
            assertTrue(type.isArray());
            assertTrue(type.isOpenType());
            final Object emptyArray = ArrayUtils.emptyArray((ArrayType<?>) type.getOpenType(), getClass().getClassLoader());
            assertEquals(0, Array.getLength(emptyArray));
        }
    }

    @Test
    public void bufferTypesTest(){
        for(final WellKnownType type: WellKnownType.getBufferTypes()){
            assertTrue(Buffer.class.isAssignableFrom(type.getJavaType()));
            assertTrue(type.getOpenType() == null);
            assertTrue(type.isBuffer());
        }
    }

    @Test
    public void isNumberTest(){
        assertTrue(WellKnownType.BYTE.isNumber());
        assertTrue(WellKnownType.SHORT.isNumber());
        assertTrue(WellKnownType.BIG_INT.isNumber());
        assertTrue(WellKnownType.BIG_DECIMAL.isNumber());
        assertFalse(WellKnownType.BOOL.isNumber());
        assertFalse(WellKnownType.STRING.isNumber());
    }
}
