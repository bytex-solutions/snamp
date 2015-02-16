package com.itworks.snamp.testing.management.jmx;

import com.itworks.snamp.jmx.OpenMBean;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import javax.management.*;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class OpenMBeanTest extends AbstractUnitTest<OpenMBean> {
    private static final class DummyOpenMBean extends OpenMBean{
        private static final class TemperatureAttribute extends OpenAttribute<Integer, SimpleType<Integer>>{
            private int value = 42;

            public TemperatureAttribute(){
                super("Temperature", SimpleType.INTEGER);
            }

            @Override
            public Integer getValue() {
                return value;
            }

            @Override
            public void setValue(final Integer value) throws Exception {
                this.value = value;
            }
        }

        private static final class FlagAttribute extends OpenAttribute<Boolean, SimpleType<Boolean>>{
            public FlagAttribute(){
                super("Flag", SimpleType.BOOLEAN);
            }

            @Override
            public Boolean getValue() throws Exception {
                return true;
            }

            @Override
            public boolean isIs() {
                return true;
            }
        }

        public DummyOpenMBean(){
            super(new TemperatureAttribute(), new FlagAttribute());
        }
    }

    @Test
    public void openAttributeTest() throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        final DynamicMBean mbean = new DummyOpenMBean();
        mbean.setAttribute(new Attribute("Temperature", 52));
        assertEquals(52, mbean.getAttribute("Temperature"));
        assertEquals(2, mbean.getMBeanInfo().getAttributes().length);
        MBeanAttributeInfo info = mbean.getMBeanInfo().getAttributes()[0];
        assertEquals("Temperature", info.getName());
        assertTrue(info instanceof OpenMBeanAttributeInfo);
        assertTrue(info.isReadable());
        assertTrue(info.isWritable());
        assertFalse(info.isIs());
        info = mbean.getMBeanInfo().getAttributes()[1];
        assertTrue(info instanceof OpenMBeanAttributeInfo);
        assertEquals("Flag", info.getName());
        assertTrue(info.isReadable());
        assertFalse(info.isWritable());
        assertTrue(info.isIs());
    }
}
