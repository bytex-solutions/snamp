package com.snamp;

import com.snamp.internal.BeanPropertyAccessor;
import org.junit.Test;

import java.beans.IntrospectionException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class BeanPropertyAccessorTest extends SnampClassTestSet<BeanPropertyAccessor<?>> {
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
        final BeanPropertyAccessor<SimpleBean> map = new BeanPropertyAccessor<>(new SimpleBean());
        assertTrue(map.set("value", 42));
        assertEquals(42, map.get("value"));
    }
}
