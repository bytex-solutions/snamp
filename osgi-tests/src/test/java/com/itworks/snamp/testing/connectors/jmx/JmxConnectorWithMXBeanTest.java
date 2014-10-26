package com.itworks.snamp.testing.connectors.jmx;

import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;
import org.apache.commons.collections4.Factory;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ExamReactorStrategy(PerMethod.class)
public final class JmxConnectorWithMXBeanTest extends AbstractJmxConnectorTest<MemoryMXBean> {

    public JmxConnectorWithMXBeanTest() throws MalformedObjectNameException {
        super(ManagementFactory.getMemoryMXBean(), new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME));
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Factory<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.create();
        attribute.setAttributeName("ObjectPendingFinalizationCount");
        attribute.getParameters().put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);
        attributes.put("1", attribute);
        attribute = attributeFactory.create();
        attribute.setAttributeName("HeapMemoryUsage");
        attribute.getParameters().put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);
        attributes.put("2", attribute);
    }

    @Test
    public void testForAttributes() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        testAttribute("1", "ObjectPendingFinalizationCount", TypeLiterals.INTEGER, 0, true);
        testAttribute("2", "HeapMemoryUsage", TypeLiterals.STRING_MAP, null, new Equator<Map<String, Object>>() {
            @Override
            public boolean equate(final Map o1, final Map o2) {
                return true;
            }
        }, true);
    }
}
