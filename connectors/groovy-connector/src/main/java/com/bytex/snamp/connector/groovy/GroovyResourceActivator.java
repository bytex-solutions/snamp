package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.discovery.AbstractDiscoveryService;
import groovy.grape.GrabAnnotationTransformation;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import static com.bytex.snamp.MapUtils.toProperties;


/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ImportClass(GrabAnnotationTransformation.class)
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
                                                   final DependencyManager dependencies) throws IOException, ResourceException, ScriptException {
        return new GroovyResourceConnector(resourceName, connectionString, connectionParameters);
    }

    private static AbstractDiscoveryService<ManagedResourceInfo> newDiscoveryService(final DependencyManager dependencies){
        return new AbstractDiscoveryService<ManagedResourceInfo>() {
            @Override
            protected ManagedResourceInfo createProvider(final String connectionString, final Map<String, String> connectionOptions) throws IOException, ResourceException, ScriptException {
                final GroovyConnectionString connectionInfo = new GroovyConnectionString(connectionString);
                //the last path is a path to groovy
                final ManagedResourceScriptEngine engine = new ManagedResourceScriptEngine(
                        connectionInfo.getScriptName(),
                        getLogger(),
                        getClass().getClassLoader(),
                        true,
                        toProperties(connectionOptions),
                        connectionInfo.getScriptPath());
                final ManagedResourceScriptlet scriptlet = engine.createScript(connectionInfo.getScriptName(), null);
                scriptlet.run();
                return scriptlet;
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
