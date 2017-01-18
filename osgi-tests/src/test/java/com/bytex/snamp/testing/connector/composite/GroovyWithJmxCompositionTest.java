package com.bytex.snamp.testing.connector.composite;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.groovy.AbstractGroovyConnectorTest;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connector.jmx.TestOpenMBean;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.management.MBeanException;
import javax.management.ReflectionException;

/**
 * Test for composition of JMX and Groovy connector.
 */
@SnampDependencies({SnampFeature.GROOVY_CONNECTOR, SnampFeature.JMX_CONNECTOR})
public final class GroovyWithJmxCompositionTest extends AbstractCompositeConnectorTest {
    private static final String SEPARATOR = ",";

    public GroovyWithJmxCompositionTest() {
        super(buildConnectionString(), ImmutableMap.of(
                "separator", SEPARATOR,
                "jmx:login", AbstractJmxConnectorTest.JMX_LOGIN,
                "jmx:password", AbstractJmxConnectorTest.JMX_PASSWORD,
                "jmx:objectName", TestOpenMBean.BEAN_NAME
        ));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    private static String buildConnectionString() {
        return "jmx:=" +
                AbstractJmxConnectorTest.getConnectionString() +
                SEPARATOR +
                "groovy:=" +
                AbstractGroovyConnectorTest.getConnectionString();
    }

    @Test
    public void operationTest() throws ReflectionException, MBeanException {
        final OperationSupport operationSupport = getManagementConnector(getTestBundleContext()).queryObject(OperationSupport.class);
        try{
            final Object result = operationSupport.invoke("groovyOp", new Long[]{12L, 2L}, new String[0]);
            assertTrue(result instanceof Long);
            assertEquals(14L, result);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Override
    protected void fillOperations(final EntityMap<? extends OperationConfiguration> operations) {
        operations.addAndConsume("groovyOp", operation -> {
            operation.put("source", "groovy");
            operation.setAlternativeName("CustomOperation");
        });
    }
}
