package com.snamp;

import org.junit.Test;

import java.beans.IntrospectionException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class BeanMapTest extends SnampClassTestSet<BeanMap<?>> {
    public static final class SimpleBean{
        private int propertyStorage;

        public int getValue(){
            return propertyStorage;
        }

        public void setValue(final int v){
            propertyStorage = v;
        }
    }

    @Test
    public final void propertyAccessingTest() throws IntrospectionException {
        final BeanMap<SimpleBean> map = new BeanMap<>(new SimpleBean());
        assertTrue(map.set("value", 42));
        assertEquals(42, map.get("value"));
    }
}
