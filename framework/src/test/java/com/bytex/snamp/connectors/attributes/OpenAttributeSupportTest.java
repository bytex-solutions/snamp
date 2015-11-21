package com.bytex.snamp.connectors.attributes;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.configuration.ConfigParameters;
import com.bytex.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;
import org.junit.Assert;
import org.junit.Test;

import javax.management.Attribute;
import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class OpenAttributeSupportTest extends Assert {
    private static final class StringAttribute extends OpenMBeanAttributeAccessor<String> {
        private static final String NAME = "str";
        private static final long serialVersionUID = -8503919173459779384L;
        private String value = "";

        private StringAttribute(final String attributeID,
                                final AttributeDescriptor descriptor) {
            super(attributeID,
                    "String attr",
                    SimpleType.STRING,
                    AttributeSpecifier.READ_WRITE,
                    descriptor);
        }

        @Override
        protected String getValue() {
            return value;
        }

        @Override
        protected void setValue(final String value) {
            this.value = value;
        }
    }

    private static final class Attributes extends OpenAttributeRepository {
        @SuppressWarnings("unchecked")
        private Attributes() {
            super("TEST RESOURCE", OpenMBeanAttributeAccessor.class);
        }

        @Override
        protected OpenMBeanAttributeAccessor<?> connectAttribute(final String attributeID, final AttributeDescriptor descriptor) throws Exception {
            switch (descriptor.getAttributeName()){
                case StringAttribute.NAME: return new StringAttribute(attributeID, descriptor);
                default: throw new Exception("Unable to connect attribute");
            }
        }

        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            fail(e.getMessage());
        }

        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            fail(e.getMessage());
        }

        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void stringAttributeTest() throws Exception {
        final Attributes support = new Attributes();
        support.addAttribute("a", "str", TimeSpan.INFINITE, new ConfigParameters(new SerializableAttributeConfiguration()));
        support.addAttribute("b", "str", TimeSpan.INFINITE, new ConfigParameters(new SerializableAttributeConfiguration()));
        support.setAttribute(new Attribute("a", "1"));
        support.setAttribute(new Attribute("b", "2"));
        assertEquals("1", support.getAttribute("a"));
        assertEquals("2", support.getAttribute("b"));
    }
}
