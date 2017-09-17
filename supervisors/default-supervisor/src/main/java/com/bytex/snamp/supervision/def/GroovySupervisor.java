package com.bytex.snamp.supervision.def;

import com.bytex.snamp.supervision.elasticity.groovy.GroovyProvisioningEngine;
import com.bytex.snamp.supervision.elasticity.groovy.GroovyProvisioningEngineFactory;
import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Represents default supervisor with Groovy-based elasticity manager.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class GroovySupervisor extends DefaultSupervisor {
    private final GroovyProvisioningEngine provisioningEngine;

    GroovySupervisor(@Nonnull final String groupName,
                     final String scriptPath,
                     final Properties scriptEnvironment) throws IOException, ResourceException, ScriptException {
        super(groupName);
        final GroovyProvisioningEngineFactory factory = new GroovyProvisioningEngineFactory(
                getClass().getClassLoader(),
                scriptEnvironment,
                scriptPath);
        provisioningEngine = factory.createScript(new Binding(scriptEnvironment));
    }

    /**
     * Executes automatically using scheduling time.
     */
    @Override
    protected void supervise() {
        super.supervise();
        final Map<String, Double> ballotBox = new HashMap<>();
        switch (getElasticityManager().decide(this, ballotBox)) {
            case SCALE_IN:
                provisioningEngine.scaleIn(this);
                scaleIn(ballotBox);
                break;
            case SCALE_OUT:
                provisioningEngine.scaleOut(this);
                scaleOut(ballotBox);
                break;
            case COOLDOWN:
                provisioningEngine.cooldown(this);
                break;
            case OUT_OF_SPACE:
                provisioningEngine.outOfSpace(this);
                maxClusterSizeReached(ballotBox);
                break;
        }
        ballotBox.clear();
    }
}
