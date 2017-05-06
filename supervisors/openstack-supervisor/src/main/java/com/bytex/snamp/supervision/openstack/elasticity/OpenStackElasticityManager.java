package com.bytex.snamp.supervision.openstack.elasticity;

import com.bytex.snamp.supervision.def.DefaultElasticityManager;
import org.openstack4j.api.senlin.SenlinService;

/**
 * Represents elasticity manager based on OpenStack Senlin.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OpenStackElasticityManager extends DefaultElasticityManager {
    public void performScaling(final OpenStackScalingEvaluationContext context, final SenlinService senlin) {
        switch (decide(context)) {

        }
    }
}
