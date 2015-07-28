package com.bytex.snamp.adapters.groovy;

import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.internal.annotations.SpecialUse;
import groovy.grape.Grape;
import org.apache.ivy.Ivy;

import java.util.logging.Logger;

/**
 * Represents information about Groovy-based resource adapter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ResourceAdapterInfo {
    /**
     * The name of the Groovy Resource Adapter.
     */
    public static final String NAME = "groovy";

    private ResourceAdapterInfo(){

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
