package com.bytex.snamp.supervision.openstack.elasticity;

import com.bytex.snamp.supervision.def.DefaultElasticityManager;
import com.google.common.collect.ImmutableMap;
import org.openstack4j.api.senlin.SenlinService;
import org.openstack4j.model.senlin.ClusterActionCreate;
import org.openstack4j.openstack.senlin.domain.SenlinClusterActionCreate;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents elasticity manager based on OpenStack Senlin.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OpenStackElasticityManager extends DefaultElasticityManager {
    private final String clusterID;

    public OpenStackElasticityManager(@Nonnull final String clusterID){
        this.clusterID = Objects.requireNonNull(clusterID);
    }

    public void performScaling(final OpenStackScalingEvaluationContext context, final SenlinService senlin) {
        final String RESIZE_COUNT_PARAM = "count";
        final Map<String, Double> ballotBox = new HashMap<>();
        final int scalingStatus;
        switch (decide(context, ballotBox)) {
            case SCALE_IN:
                ClusterActionCreate scalingAction = SenlinClusterActionCreate.build()
                        .scaleIn(ImmutableMap.of(RESIZE_COUNT_PARAM, Integer.toString(getScalingSize())))
                        .build();
                senlin.cluster().action(clusterID, scalingAction);
                context.reportScaleIn(ballotBox);
                break;
            case SCALE_OUT:
                scalingAction = SenlinClusterActionCreate.build()
                        .scaleOut(ImmutableMap.of(RESIZE_COUNT_PARAM, Integer.toString(getScalingSize())))
                        .build();
                senlin.cluster().action(clusterID, scalingAction);
                context.reportScaleOut(ballotBox);
                break;
            case OUT_OF_SPACE:
                context.reportMaxClusterSizeReached(ballotBox);
                break;
        }
        ballotBox.clear();
    }
}
