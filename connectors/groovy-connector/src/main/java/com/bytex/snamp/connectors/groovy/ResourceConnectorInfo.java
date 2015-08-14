package com.bytex.snamp.connectors.groovy;

import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.internal.annotations.SpecialUse;
import groovy.grape.Grape;
import org.apache.ivy.Ivy;

import java.util.logging.Logger;

/**
 * Represents generic information about this resource connector.
 * This class cannot be inherited or instantiated.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ResourceConnectorInfo {
    /**
     * The name of the Groovy Resource Connector.
     */
    public static final String NAME = "groovy";
    private ResourceConnectorInfo(){
    }

    public static Logger getLogger(){
        return AbstractManagedResourceConnector.getLogger(NAME);
    }

    @SpecialUse
    private static Class<Ivy> apacheIvyDependency(){
        return Ivy.class;
    }

    @SpecialUse
    private static Class<Grape> groovyGrapeDependency(){
        return Grape.class;
    }
}