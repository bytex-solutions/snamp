package com.bytex.snamp.testing.connector.composite;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.jmx.AbstractJmxConnectorTest;
import com.google.common.collect.ImmutableMap;

import java.lang.management.ManagementFactory;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.RSHELL_CONNECTOR, SnampFeature.JMX_CONNECTOR})
public final class RShellWithJmxCompositionTest extends AbstractCompositeConnectorTest {
    private static String buildConnectionString() {
        return new StringBuilder()
                .append("jmx:=")
                .append(AbstractJmxConnectorTest.getConnectionString())
                .append(';')
                .append("rshell:=process")
                .toString();

    }

    public RShellWithJmxCompositionTest(){
        super(buildConnectionString(), ImmutableMap.of(
            "jmx:login", AbstractJmxConnectorTest.JMX_LOGIN,
            "jmx:password", AbstractJmxConnectorTest.JMX_PASSWORD,
            "jmx:objectName", ManagementFactory.MEMORY_MXBEAN_NAME
        ));
    }

    @Override
    protected void fillOperations(final EntityMap<? extends OperationConfiguration> operations) {
        operations.addAndConsume("jmx:forceGC", operation -> operation.setAlternativeName("gc"));
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("jmx:finalizationCount", attribute -> attribute.setAlternativeName("ObjectPendingFinalizationCount"));
        attributes.addAndConsume("rshell:ms", attribute -> {
            attribute.setAlternativeName(getPathToFileInProjectRoot("freemem-tool-profile.xml"));
            attribute.getParameters().put("format", "-m");
        });
    }
}
