package com.bytex.snamp.jmx;

import org.junit.Assert;
import org.junit.Test;

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
    public void memoryLeakTest(){
        //load all type tokens into cache
        for(final WellKnownType type: WellKnownType.values()) {
            assertEquals(type, WellKnownType.getType(type.getJavaType()));
        }
        final long cacheSize = WellKnownType.cacheSize();
        System.gc();
        //load all type tokens again
        for(final WellKnownType type: WellKnownType.values()) {
            assertEquals(type, WellKnownType.getType(type.getTypeToken()));
        }
        assertEquals(cacheSize, WellKnownType.cacheSize());
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
    public void isNumberTest(){
        assertTrue(WellKnownType.BYTE.isNumber());
        assertTrue(WellKnownType.SHORT.isNumber());
        assertTrue(WellKnownType.BIG_INT.isNumber());
        assertTrue(WellKnownType.BIG_DECIMAL.isNumber());
        assertFalse(WellKnownType.BOOL.isNumber());
        assertFalse(WellKnownType.STRING.isNumber());
    }
}
