package com.bytex.snamp.testing.connector.jmx;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.connector.operations.OperationManager;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class JmxConnectorWithMXBeanTest extends AbstractJmxConnectorTest<MemoryMXBean> {

    public JmxConnectorWithMXBeanTest() throws MalformedObjectNameException {
        super(ManagementFactory.getMemoryMXBean(), new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME));
    }

    @Override
    protected void fillOperations(final EntityMap<? extends OperationConfiguration> operations) {
        OperationConfiguration operation = operations.getOrAdd("forceGC");
        operation.setAlternativeName("gc");
        operation.put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("ObjectPendingFinalizationCount");
        attribute.put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);

        attribute = attributes.getOrAdd("HeapMemoryUsage");
        attribute.put("objectName", ManagementFactory.MEMORY_MXBEAN_NAME);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }

    @Test
    public void testForAttributes() throws Exception {
        testAttribute("ObjectPendingFinalizationCount", TypeToken.of(Integer.class), 0,
                Objects::equals,
                true);
        testAttribute("HeapMemoryUsage", TypeToken.of(CompositeData.class),
                null,
                (l, r) -> true,
                true);
    }

    @Test
    public void operationTest() throws JMException {
        final OperationManager operationSupport = getManagementConnector().queryObject(OperationManager.class).orElseThrow(AssertionError::new);
        try{
            final Object result = operationSupport.invoke("forceGC", new Object[0], new String[0]);
            assertNull(result);
        }
        finally {
            releaseManagementConnector();
        }
    }
}
