package com.bytex.snamp.testing.connectors.jmx;

import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.OperationConfiguration;
import com.bytex.snamp.connectors.operations.OperationSupport;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;

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
    protected void fillOperations(final EntityMap<? extends OperationConfiguration> operations) {
        OperationConfiguration operation = operations.getOrAdd("forceGC");
        operation.setOperationName("gc");
        operation.getParameters().put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1");
        attribute.setAttributeName("ObjectPendingFinalizationCount");
        attribute.getParameters().put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);

        attribute = attributes.getOrAdd("2");
        attribute.setAttributeName("HeapMemoryUsage");
        attribute.getParameters().put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }

    @Test
    public void testForAttributes() throws Exception {
        testAttribute("1", TypeToken.of(Integer.class), 0,
                AbstractResourceConnectorTest.<Integer>valueEquator(),
                true);
        testAttribute("2", TypeToken.of(CompositeData.class),
                null,
                AbstractResourceConnectorTest.<CompositeData>successEquator(),
                true);
    }

    @Test
    public void operationTest() throws ReflectionException, MBeanException {
        final OperationSupport operationSupport = getManagementConnector(getTestBundleContext())
                .queryObject(OperationSupport.class);
        try{
            final Object result = operationSupport.invoke("forceGC", new Object[0], new String[0]);
            assertNull(result);
        }
        finally {
            releaseManagementConnector();
        }
    }
}
