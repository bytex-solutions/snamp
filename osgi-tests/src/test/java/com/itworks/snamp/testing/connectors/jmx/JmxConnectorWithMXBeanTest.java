package com.itworks.snamp.testing.connectors.jmx;

import com.google.common.base.Supplier;
import com.itworks.snamp.mapping.RecordSet;
import com.itworks.snamp.mapping.TypeLiterals;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;
import org.junit.Test;
import org.osgi.framework.BundleContext;

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
public final class JmxConnectorWithMXBeanTest extends AbstractJmxConnectorTest<MemoryMXBean> {

    public JmxConnectorWithMXBeanTest() throws MalformedObjectNameException {
        super(ManagementFactory.getMemoryMXBean(), new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME));
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("ObjectPendingFinalizationCount");
        attribute.getParameters().put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);
        attributes.put("1", attribute);
        attribute = attributeFactory.get();
        attribute.setAttributeName("HeapMemoryUsage");
        attribute.getParameters().put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);
        attributes.put("2", attribute);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }

    @Test
    public void testForAttributes() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        testAttribute("1", "ObjectPendingFinalizationCount", TypeLiterals.INTEGER, 0, true);
        testAttribute("2", "HeapMemoryUsage", TypeLiterals.NAMED_RECORD_SET, null, new Equator<RecordSet<String, ?>>() {
            @Override
            public boolean equate(final RecordSet o1, final RecordSet o2) {
                return true;
            }
        }, true);
    }
}
