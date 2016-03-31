package com.bytex.snamp.jmx;

import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class DefaultValuesTest extends Assert {
    @Test
    public void primitivesTest(){
        for(final WellKnownType primitive: WellKnownType.getPrimitiveTypes()) {
            if(primitive.equals(WellKnownType.VOID)) continue;
            final Object defval = DefaultValues.get(primitive.getOpenType());
            assertTrue(String.format("Prim: %s, value: %s", primitive, defval), primitive.isInstance(defval));
        }
    }

    @Test
    public void arrayTest() throws OpenDataException {
        final short[] array1 = DefaultValues.get(new ArrayType<short[]>(SimpleType.SHORT, true));
        assertTrue(array1.length == 0);
        final Short[] array2 = DefaultValues.get(new ArrayType<Short[]>(SimpleType.SHORT, false));
        assertTrue(array2.length == 0);
    }
}
