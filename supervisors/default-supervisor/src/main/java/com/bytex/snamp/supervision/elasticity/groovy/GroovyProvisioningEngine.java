package com.bytex.snamp.supervision.elasticity.groovy;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.scripting.groovy.Scriptlet;

/**
 * Represents Groovy-based provisioning engine.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class GroovyProvisioningEngine extends Scriptlet {
    public abstract void scaleIn(final Aggregator provisioningContext);

    public abstract void scaleOut(final Aggregator provisioningContext);

    public void cooldown(final Aggregator provisioningContext){

    }

    public void outOfSpace(final Aggregator provisioningContext){
        
    }
}
