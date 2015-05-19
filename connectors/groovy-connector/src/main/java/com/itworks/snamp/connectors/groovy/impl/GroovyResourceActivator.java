package com.itworks.snamp.connectors.groovy.impl;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.connectors.groovy.ManagedResourceInfo;
import com.itworks.snamp.connectors.groovy.ManagementScriptEngine;
import com.itworks.snamp.internal.annotations.SpecialUse;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class GroovyResourceActivator extends ManagedResourceActivator<GroovyResourceConnector> {
    private static final String NAME = GroovyResourceConnector.NAME;

    private static final class GroovyResourceConnectorFactory extends ManagedResourceConnectorModeler<GroovyResourceConnector>{

        @Override
        protected void addAttribute(final GroovyResourceConnector connector,
                                    final String attributeID,
                                    final String attributeName,
                                    final TimeSpan readWriteTimeout,
                                    final CompositeData options) {
            connector.addAttribute(attributeID, attributeName, readWriteTimeout, options);
        }

        @Override
        protected void enableNotifications(final GroovyResourceConnector connector, final String listId, final String category, final CompositeData options) {
            connector.enableNotifications(listId, category, options);
        }

        @Override
        protected void addOperation(final GroovyResourceConnector connector, final String operationID, final String operationName, final CompositeData options) {
            //not supported
        }

        @Override
        public GroovyResourceConnector createConnector(final String resourceName,
                                                       final String connectionString,
                                                       final Map<String, String> connectionParameters,
                                                       final RequiredService<?>... dependencies) throws IOException, ResourceException, ScriptException {
            return new GroovyResourceConnector(resourceName, connectionString, connectionParameters);
        }
    }

    private static final class GroovyDiscoveryService extends SimpleDiscoveryServiceManager<ManagedResourceInfo>{

        @Override
        protected ManagedResourceInfo createManagementInformationProvider(final String connectionString,
                                                                           final Map<String, String> connectionOptions,
                                                                           final RequiredService<?>... dependencies) throws IOException, ResourceException, ScriptException {
            final String[] paths = GroovyResourceConnector.getPaths(connectionString);
            final ManagementScriptEngine engine = new ManagementScriptEngine(
                    getClass().getClassLoader(),
                    GroovyResourceConnector.toProperties(connectionOptions),
                    paths);
            final String initScript = GroovyResourceConfigurationDescriptor.getInitScriptFile(connectionOptions);
            return engine.init(initScript, connectionOptions);
        }

        @Override
        protected <T extends FeatureConfiguration> Collection<T> getManagementInformation(final Class<T> entityType,
                                                                                          final ManagedResourceInfo provider,
                                                                                          final RequiredService<?>... dependencies) {
            return provider.getEntities(entityType);
        }
    }

    private static final class GroovyConfigurationDescriptionProvider extends ConfigurationEntityDescriptionManager<GroovyResourceConfigurationDescriptor>{

        @Override
        protected GroovyResourceConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new GroovyResourceConfigurationDescriptor();
        }
    }

    @SpecialUse
    public GroovyResourceActivator(){
        super(NAME,
                new GroovyResourceConnectorFactory(),
                new GroovyDiscoveryService(),
                new GroovyConfigurationDescriptionProvider());
    }
}
