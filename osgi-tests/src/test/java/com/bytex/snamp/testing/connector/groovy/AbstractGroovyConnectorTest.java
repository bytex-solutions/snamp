package com.bytex.snamp.testing.connector.groovy;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
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
        return "file:" + getPathToFileInProjectRoot("sample-groovy-scripts") + File.separator;
    }

    protected static Map<String, String> getDefaultConnectionParams(){
        return ImmutableMap.of("initScript", "init.groovy");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.getOrAdd("DummyAttribute").getParameters().put("configParam", "value");

        attributes.getOrAdd("JsonAttribute");

        attributes.getOrAdd("Yahoo");

        attributes.getOrAdd("DictionaryAttribute");

        attributes.getOrAdd("TableAttribute");
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        events.getOrAdd("Event");
    }
}
