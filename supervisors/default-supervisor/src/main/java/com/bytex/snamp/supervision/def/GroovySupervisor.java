package com.bytex.snamp.supervision.def;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.supervision.elasticity.groovy.GroovyProvisioningEngine;
import com.bytex.snamp.supervision.elasticity.groovy.GroovyProvisioningEngineFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents default supervisor with Groovy-based elasticity manager.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GroovySupervisor extends DefaultSupervisor {

    private static final String GROOVY_MANAGER_PATH = "groovyElasticityManager";
    private GroovyProvisioningEngine provisioningEngine;

    GroovySupervisor(@Nonnull final String groupName) {
        super(groupName);
    }

    private void performScaling(final DefaultElasticityManager elastman) {
        final GroovyProvisioningEngine provisioningEngine = this.provisioningEngine;
        if (provisioningEngine == null)
            return;
        final Map<String, Double> ballotBox = new HashMap<>();
        switch (elastman.decide(this, ballotBox)) {
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

    /**
     * Executes automatically using scheduling time.
     */
    @Override
    protected void supervise() {
        super.supervise();
        queryObject(DefaultElasticityManager.class).ifPresent(this::performScaling);
    }

    /**
     * Starts the tracking resources.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @param configuration Tracker startup parameters.
     * @throws Exception Unable to start tracking.
     */
    @Override
    protected void start(final SupervisorInfo configuration) throws Exception {
        if (configuration.containsKey(GROOVY_MANAGER_PATH)) {
            final String path = configuration.get(GROOVY_MANAGER_PATH);
            final GroovyProvisioningEngineFactory factory = new GroovyProvisioningEngineFactory(getClass().getClassLoader(),
                    MapUtils.toProperties(configuration),
                    path);
            provisioningEngine = factory.createScript(null);
            overrideElasticityManager(new DefaultElasticityManager());
        }
        super.start(configuration);
    }

    /**
     * Stops tracking resources.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     *
     * @throws Exception Unable to stop tracking resources.
     */
    @Override
    protected void stop() throws Exception {
        provisioningEngine = null;
        super.stop();
    }
}
