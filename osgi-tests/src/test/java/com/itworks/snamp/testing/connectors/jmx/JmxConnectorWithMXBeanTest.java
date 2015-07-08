package com.itworks.snamp.testing.connectors.jmx;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.TypeTokens;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;
import com.itworks.snamp.connectors.operations.OperationSupport;
import com.itworks.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Map;

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
    protected void fillOperations(final Map<String, OperationConfiguration> operations,
                                  final Supplier<OperationConfiguration> operationFactory) {
        OperationConfiguration operation = operationFactory.get();
        operation.setOperationName("gc");
        operation.getParameters().put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);
        operations.put("forceGC", operation);
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
    public void testForAttributes() throws Exception {
        testAttribute("1", TypeTokens.INTEGER, 0,
                AbstractResourceConnectorTest.<Integer>valueEquator(),
                true);
        testAttribute("2", TypeToken.of(CompositeData.class),
                null,
                AbstractResourceConnectorTest.<CompositeData>successEquator(),
                true);
    }

    @Test
    public void operationTest() throws ReflectionException, MBeanException {
        final OperationSupport operationSupport = getManagementConnector(getTestBundleContext()).queryObject(OperationSupport.class);
        try{
            final Object result = operationSupport.invoke("forceGC", new Object[0], new String[0]);
            assertNull(result);
        }
        finally {
            releaseManagementConnector();
        }
    }
}
