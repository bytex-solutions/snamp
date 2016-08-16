package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigParameters;
import org.junit.Assert;
import org.junit.Test;

import javax.management.Attribute;
import javax.management.openmbean.SimpleType;

import static com.bytex.snamp.configuration.impl.SerializableAgentConfiguration.newEntityConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
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
        protected OpenMBeanAttributeAccessor<?> connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
            switch (descriptor.getName(attributeName)){
                case StringAttribute.NAME: return new StringAttribute(attributeName, descriptor);
                default: throw new Exception("Unable to connect attribute");
            }
        }

        @Override
        protected void failedToConnectAttribute(final String attributeName, final Exception e) {
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
        final AttributeConfiguration attributeConfig = newEntityConfiguration(AttributeConfiguration.class);
        assertNotNull(attributeConfig);
        attributeConfig.setAlternativeName("str");
        support.addAttribute("a", null, new ConfigParameters(attributeConfig));
        support.addAttribute("b", null, new ConfigParameters(attributeConfig));
        support.setAttribute(new Attribute("a", "1"));
        support.setAttribute(new Attribute("b", "2"));
        assertEquals("1", support.getAttribute("a"));
        assertEquals("2", support.getAttribute("b"));
    }
}
