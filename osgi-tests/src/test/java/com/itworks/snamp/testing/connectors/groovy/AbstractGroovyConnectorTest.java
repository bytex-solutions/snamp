package com.itworks.snamp.testing.connectors.groovy;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.AbstractResourceConnectorTest;

import java.io.File;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.GROOVY_CONNECTOR)
public abstract class AbstractGroovyConnectorTest extends AbstractResourceConnectorTest {
    public static final String CONNECTOR_TYPE = "groovy";

    protected AbstractGroovyConnectorTest(final String connectionString,
                                          final Map<String, String> options) {
        super(CONNECTOR_TYPE, connectionString, options);
    }

    protected AbstractGroovyConnectorTest(){
        this(getConnectionString(),
                getDefaultConnectionParams());
    }

    protected static String getConnectionString(){
        return getProjectRootDir() + File.separator + "sample-groovy-scripts/";
    }

    protected static Map<String, String> getDefaultConnectionParams(){
        return ImmutableMap.of("initScript", "init.groovy");
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attr = attributeFactory.get();
        attr.setAttributeName("DummyAttribute");
        attr.getParameters().put("configParam", "value");
        attributes.put("dummy", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("JsonAttribute");
        attributes.put("json", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("Yahoo");
        attributes.put("finance", attr);
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration ev = eventFactory.get();
        ev.setCategory("Event");
        events.put("ev", ev);
    }
}
