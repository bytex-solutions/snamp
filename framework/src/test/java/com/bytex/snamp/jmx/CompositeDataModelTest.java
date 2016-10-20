package com.bytex.snamp.jmx;

import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class CompositeDataModelTest extends Assert {
    public static final class SimpleCompositeData implements ISimpleCompositeData{
        private int key1;
        private double key2;
        private String key3;

        @Override
        public int getKey1() {
            return key1;
        }

        @Override
        public void setKey1(final int value) {
            key1 = value;
        }

        @Override
        public double getKey2() {
            return key2;
        }

        @Override
        public void setKey2(final double value) {
            key2 = value;
        }

        @Override
        public String getKey3() {
            return key3;
        }

        @Override
        public void setKey3(final String value) {
            key3 = value;
        }
    }

    @Test
    public void proxyTest() throws OpenDataException, ReflectiveOperationException {
        CompositeData dict = new CompositeDataBuilder()
                .setTypeName("dict")
                .setTypeDescription("dict")
                .put(ISimpleCompositeData.KEY1_NAME, "Dummy key", 42)
                .put(ISimpleCompositeData.KEY2_NAME, "Dummy key", 3.14)
                .put(ISimpleCompositeData.KEY3_NAME, "Dummy key", "Hello, world")
                .call();
        final ISimpleCompositeData data = CompositeDataUtils.convert(ISimpleCompositeData.class, dict);
        assertEquals(42, data.getKey1());
        assertEquals(3.14, data.getKey2(), 0.001);
        assertEquals("Hello, world", data.getKey3());
        data.setKey1(43);
        data.setKey2(2.42);
        data.setKey3("Empty");
        dict = CompositeDataUtils.convert(data, dict.getCompositeType());
        assertEquals(43, data.getKey1());
        assertEquals(43, dict.get(ISimpleCompositeData.KEY1_NAME));
        assertEquals(2.42, data.getKey2(), 0.001);
        assertEquals(2.42, dict.get(ISimpleCompositeData.KEY2_NAME));
        assertEquals("Empty", data.getKey3());
        assertEquals("Empty", dict.get(ISimpleCompositeData.KEY3_NAME));
    }

    @Test
    public void beanTest() throws OpenDataException, ReflectiveOperationException {
        SimpleCompositeData bean = new SimpleCompositeData();
        bean.setKey1(42);
        bean.setKey2(3.14);
        bean.setKey3("Hello, world");
        CompositeData dict = CompositeDataUtils.convert(bean, new CompositeTypeBuilder()
                        .setTypeName("dict")
                        .setDescription("dict")
                        .addItem(SimpleCompositeData.KEY1_NAME, "Dummy item", SimpleCompositeData.KEY1_TYPE)
                        .addItem(SimpleCompositeData.KEY2_NAME, "Dummy item", SimpleCompositeData.KEY2_TYPE)
                        .addItem(SimpleCompositeData.KEY3_NAME, "Dummy item", SimpleCompositeData.KEY3_TYPE)
                        .call()
        );
        assertEquals(42, dict.get(SimpleCompositeData.KEY1_NAME));
        assertEquals(3.14, (double)dict.get(SimpleCompositeData.KEY2_NAME), 0.001);
        assertEquals("Hello, world", dict.get(SimpleCompositeData.KEY3_NAME));
        bean.setKey2(2.42);
        dict = CompositeDataUtils.convert(bean, dict.getCompositeType());
        assertEquals(2.42, (double)dict.get(SimpleCompositeData.KEY2_NAME), 0.001);
    }
}
