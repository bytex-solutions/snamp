package com.bytex.snamp.testing.connectors.groovy;

import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;

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
        return getPathToFileInProjectRoot("sample-groovy-scripts") + File.separator;
    }

    protected static Map<String, String> getDefaultConnectionParams(){
        return ImmutableMap.of("initScript", "init.groovy");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attr = attributes.getOrAdd("dummy");
        attr.setAttributeName("DummyAttribute");
        attr.getParameters().put("configParam", "value");

        attr = attributes.getOrAdd("json");
        attr.setAttributeName("JsonAttribute");

        attr = attributes.getOrAdd("finance");
        attr.setAttributeName("Yahoo");

        attr = attributes.getOrAdd("dict");
        attr.setAttributeName("DictionaryAttribute");

        attr = attributes.getOrAdd("table");
        attr.setAttributeName("TableAttribute");
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration ev = events.getOrAdd("ev");
        ev.setCategory("Event");
    }
}
