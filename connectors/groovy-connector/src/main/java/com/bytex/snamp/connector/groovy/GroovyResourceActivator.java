package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;
import groovy.grape.GrabAnnotationTransformation;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;


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
                configurationDescriptor(GroovyResourceConfigurationDescriptor::getInstance));
    }

    private static GroovyResourceConnector createConnector(final String resourceName,
                                                           final com.bytex.snamp.configuration.ManagedResourceInfo configuration,
                                                   final DependencyManager dependencies) throws IOException, ResourceException, ScriptException {
        return new GroovyResourceConnector(resourceName, configuration);
    }
}
