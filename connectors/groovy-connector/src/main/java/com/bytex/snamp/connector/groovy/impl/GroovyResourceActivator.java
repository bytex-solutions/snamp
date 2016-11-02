package com.bytex.snamp.connector.groovy.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.discovery.AbstractDiscoveryService;
import com.bytex.snamp.connector.groovy.ManagedResourceInfo;
import com.bytex.snamp.connector.groovy.ManagedResourceScriptEngine;
import com.bytex.snamp.io.IOUtils;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;


/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GroovyResourceActivator extends ManagedResourceActivator<GroovyResourceConnector> {
    @SpecialUse
    public GroovyResourceActivator(){
        super(GroovyResourceActivator::createConnector,
                configurationDescriptor(GroovyResourceConfigurationDescriptor::getInstance),
                discoveryService(GroovyResourceActivator::newDiscoveryService));
    }

    private static GroovyResourceConnector createConnector(final String resourceName,
                                                   final String connectionString,
                                                   final Map<String, String> connectionParameters,
                                                   final RequiredService<?>... dependencies) throws IOException, ResourceException, ScriptException {
        return new GroovyResourceConnector(resourceName, connectionString, connectionParameters);
    }

    private static AbstractDiscoveryService<ManagedResourceInfo> newDiscoveryService(final RequiredService<?>... dependencies){
        return new AbstractDiscoveryService<ManagedResourceInfo>() {
            @Override
            protected ManagedResourceInfo createProvider(final String connectionString, final Map<String, String> connectionOptions) throws IOException, ResourceException, ScriptException {
                final String[] paths = IOUtils.splitPath(connectionString);
                final ManagedResourceScriptEngine engine = new ManagedResourceScriptEngine(
                        connectionString,
                        getLogger(),
                        getClass().getClassLoader(),
                        GroovyResourceConnector.toProperties(connectionOptions),
                        paths);
                final String initScript = GroovyResourceConfigurationDescriptor.getInitScriptFile(connectionOptions);
                return engine.init(initScript, true, connectionOptions);
            }

            @Override
            protected <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType, final ManagedResourceInfo provider) {
                return provider.getEntities(entityType);
            }

            @Override
            public Logger getLogger() {
                return AbstractManagedResourceConnector.getLogger(GroovyResourceConnector.class);
            }
        };
    }
}
