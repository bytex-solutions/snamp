package com.snamp.connectors;

import com.snamp.SnampClassTestSet;
import com.snamp.TimeSpan;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Represents tests for {@link EmbeddedManagementConnector} class.
 * @author roman
 */
public final class ManagementConnectorBeanTest extends SnampClassTestSet<EmbeddedManagementConnector> {

    private static final class TestManagementConnectorBeanTest extends EmbeddedManagementConnector {
        private String field1;
        private int field2;
        private boolean field3;

        public TestManagementConnectorBeanTest() throws IntrospectionException {
            super(new AttributePrimitiveTypeBuilder());
        }

        public final String getProperty1(){
            return field1;
        }

        public final void setProperty1(final String value){
            field1 = value;
        }

        public final int getProperty2(){
            return field2;
        }

        public final void setProperty2(final int value){
            field2 = value;
        }

        public final boolean getProperty3(){
            return field3;
        }

        public final void setProperty3(final boolean value){
            field3 = value;
        }
    }

    @Test
    public final void testConnectorBean() throws IntrospectionException, TimeoutException {
        final TestManagementConnectorBeanTest connector = new TestManagementConnectorBeanTest();
        connector.setProperty1("123");
        connector.connectAttribute("0", "property1", new HashMap<String, String>());
        assertEquals(connector.getProperty1(), connector.getAttribute("0", TimeSpan.INFINITE, ""));
        connector.setAttribute("0", TimeSpan.INFINITE, "1234567890");
        assertEquals(connector.getProperty1(), connector.getAttribute("0", TimeSpan.INFINITE, ""));
        final AttributeMetadata md = connector.getAttributeInfo("0");
        assertTrue(md.canRead());
        assertTrue(md.canWrite());
        assertEquals("property1", md.getAttributeName());
        assertTrue(md.getAttributeType().canConvertFrom(String.class));
    }

    @Test
    public final void testAnonymousBean() throws IntrospectionException, TimeoutException{
        final ManagementConnector mc = EmbeddedManagementConnector.wrap(new Object() {
            private int simpleField;

            public final int getProperty() {
                return simpleField;
            }

            public final void setProperty(int value) {
                simpleField = value;
            }
        }, new AttributePrimitiveTypeBuilder());
        mc.connectAttribute("1", "property", new HashMap<String, String>());
        mc.setAttribute("1", TimeSpan.INFINITE, 42);
        assertEquals(42, mc.getAttribute("1", TimeSpan.INFINITE, 0));
    }
}
