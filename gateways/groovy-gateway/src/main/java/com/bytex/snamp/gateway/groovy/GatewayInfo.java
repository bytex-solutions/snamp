package com.bytex.snamp.gateway.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import groovy.grape.Grape;
import org.apache.ivy.Ivy;

import java.util.logging.Logger;

/**
 * Represents information about Groovy-based gateway.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GatewayInfo {
    /**
     * The name of the Groovy Gateway.
     */
    public static final String NAME = "groovy";

    private GatewayInfo(){

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
