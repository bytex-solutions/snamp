package com.bytex.snamp.connector.groovy.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.discovery.AbstractDiscoveryService;
import com.bytex.snamp.connector.groovy.ManagedResourceInfo;
import com.bytex.snamp.connector.groovy.ManagedResourceScriptEngine;
import com.bytex.snamp.io.IOUtils;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.FeatureConfiguration;


/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GroovyResourceActivator extends ManagedResourceActivator<GroovyResourceConnector> {
    private static final class GroovyResourceConnectorFactory extends ManagedResourceConnectorModeler<GroovyResourceConnector>{

        @Override
        protected boolean addAttribute(final GroovyResourceConnector connector,
                                    final String attributeName,
                                    final Duration readWriteTimeout,
                                    final CompositeData options) {
            return connector.addAttribute(attributeName, readWriteTimeout, options);
        }

        @Override
        protected boolean enableNotifications(final GroovyResourceConnector connector, final String category, final CompositeData options) {
            return connector.enableNotifications(category, options);
        }

        @Override
        protected boolean enableOperation(final GroovyResourceConnector connector, final String operationName, final Duration timeout, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected void retainAttributes(final GroovyResourceConnector connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected void retainNotifications(final GroovyResourceConnector connector, final Set<String> events) {
            connector.disableNotificationsExcept(events);
        }

        @Override
        protected void retainOperations(final GroovyResourceConnector connector, final Set<String> operations) {
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

    @SpecialUse
    public GroovyResourceActivator(){
        super( new GroovyResourceConnectorFactory(),
                configurationDescriptor(GroovyResourceConfigurationDescriptor::new),
                discoveryService(GroovyResourceActivator::newDiscoveryService));
    }

    private static AbstractDiscoveryService<ManagedResourceInfo> newDiscoveryService(final RequiredService<?>... dependencies){
        return new AbstractDiscoveryService<ManagedResourceInfo>() {
            @Override
            protected ManagedResourceInfo createProvider(final String connectionString, final Map<String, String> connectionOptions) throws IOException, ResourceException, ScriptException {
                final String[] paths = IOUtils.splitPath(connectionString);
                final ManagedResourceScriptEngine engine = new ManagedResourceScriptEngine(
                        getClass().getClassLoader(),
                        GroovyResourceConnector.toProperties(connectionOptions),
                        paths);
                final String initScript = GroovyResourceConfigurationDescriptor.getInitScriptFile(connectionOptions);
                return engine.init(initScript, connectionOptions);
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
