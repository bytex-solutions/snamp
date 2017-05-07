package com.bytex.snamp.supervision.elasticity.groovy;

import com.bytex.snamp.supervision.def.DefaultElasticityManager;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Provides Groovy-based provisioning on top of default elasticity manager.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GroovyElasticityManager extends DefaultElasticityManager {
    private final GroovyProvisioningEngine provisioningEngine;

    public GroovyElasticityManager(@Nonnull final GroovyProvisioningEngine engine){
        provisioningEngine = Objects.requireNonNull(engine);
    }

    public void performScaling(){
        
    }
}
