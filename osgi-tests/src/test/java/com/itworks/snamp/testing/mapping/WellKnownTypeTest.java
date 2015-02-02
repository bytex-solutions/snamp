package com.itworks.snamp.testing.mapping;

import com.itworks.snamp.mapping.WellKnownType;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class WellKnownTypeTest extends AbstractUnitTest<WellKnownType> {

    @Test
    public void byteTypeTest(){
        assertTrue(WellKnownType.BYTE.isInstance((byte) 2));
        assertEquals(WellKnownType.BYTE, WellKnownType.getType((byte)3));
    }

    @Test
    public void allTypesTest(){
        for(final WellKnownType type: WellKnownType.values()){
            assertEquals(type, WellKnownType.getType(type.getType()));
            if(type.isOpenType())
                assertEquals(type, WellKnownType.getType(type.getOpenType()));
        }
    }
}
