package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.discovery.AbstractFeatureDiscoveryService;
import groovy.grape.GrabAnnotationTransformation;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.bytex.snamp.MapUtils.toProperties;


/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ImportClass(GrabAnnotationTransformation.class)
public final class GroovyResourceActivator extends ManagedResourceActivator<GroovyResourceConnector> {
    @SpecialUse(SpecialUse.Case.OSGi)
    public GroovyResourceActivator(){
        super(GroovyResourceActivator::createConnector,
                configurationDescriptor(GroovyResourceConfigurationDescriptor::getInstance),
                discoveryService(GroovyResourceActivator::newDiscoveryService));
    }

    private static GroovyResourceConnector createConnector(final String resourceName,
                                                           final com.bytex.snamp.configuration.ManagedResourceInfo configuration,
                                                   final DependencyManager dependencies) throws IOException, ResourceException, ScriptException {
        return new GroovyResourceConnector(resourceName, configuration);
    }

    private static AbstractFeatureDiscoveryService<ManagedResourceInfo> newDiscoveryService(final DependencyManager dependencies){
        return new AbstractFeatureDiscoveryService<ManagedResourceInfo>() {
            @Override
            protected ManagedResourceInfo createProvider(final String connectionString, final Map<String, String> connectionOptions) throws IOException, ResourceException, ScriptException {
                //the last path is a path to groovy
                final ManagedResourceScriptEngine engine = new ManagedResourceScriptEngine(
                        "discovery",
                        getClass().getClassLoader(),
                        true,
                        toProperties(connectionOptions),
                        connectionString);
                final ManagedResourceScriptlet scriptlet = engine.createScript(null);
                scriptlet.run();
                return scriptlet;
            }

            @Override
            protected <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType, final ManagedResourceInfo provider) {
                return provider.getEntities(entityType);
            }
        };
    }
}
