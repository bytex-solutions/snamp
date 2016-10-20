package com.bytex.snamp.jmx;

import org.junit.Assert;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Roman Sakno
 * @version 2.0
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

    @Test
    public void compositeTypeTest() throws OpenDataException, MalformedObjectNameException {
        final CompositeType type = new CompositeTypeBuilder("Dummy", "DummyDesc")
                .addItem("x", "dummy", SimpleType.STRING)
                .addItem("y", "dummy", SimpleType.OBJECTNAME)
                .addItem("z", "dummy", SimpleType.BIGDECIMAL)
                .addItem("j", "dummy", SimpleType.DATE)
                .addItem("i", "dummy", SimpleType.INTEGER)
                .call();
        final CompositeData data = DefaultValues.get(type);
        assertEquals("", data.get("x"));
        assertEquals(new ObjectName(""), data.get("y"));
        assertEquals(BigDecimal.ZERO, data.get("z"));
        assertEquals(new Date(0L), data.get("j"));
        assertEquals(0, data.get("i"));
    }
}
