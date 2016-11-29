package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import groovy.grape.Grape;
import org.apache.ivy.Ivy;

import java.util.logging.Logger;

/**
 * Represents generic information about this resource connector.
 * This class cannot be inherited or instantiated.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ResourceConnectorInfo {
    /**
     * The name of the Groovy Resource Connector.
     */
    private static final String NAME = "groovy";
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