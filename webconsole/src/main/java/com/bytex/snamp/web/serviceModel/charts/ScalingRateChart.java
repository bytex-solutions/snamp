package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.connector.metrics.Rate;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.elasticity.ElasticityManager;
import com.bytex.snamp.supervision.elasticity.ScalingMetrics;
import org.codehaus.jackson.annotate.JsonProperty;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
abstract class ScalingRateChart extends RateChart {
    private String groupName = "";
    private final Function<ScalingMetrics, Rate> extractor;

    ScalingRateChart(@Nonnull final Function<ScalingMetrics, Rate> extractor) {
        this.extractor = extractor;
    }

    @JsonProperty("group")
    public final void setGroupName(final String value) {
        groupName = Objects.requireNonNull(value);
    }

    public final String getGroupName() {
        return groupName;
    }

    private Rate getRate(final SupervisorClient client) {
        try {
            return client.queryObject(ElasticityManager.class)
                    .map(ElasticityManager::getScalingMetrics)
                    .map(extractor)
                    .orElse(Rate.EMPTY);
        } finally {
            client.close();
        }
    }

    @Override
    protected final Rate extractDataSource(final BundleContext context) {
        return SupervisorClient.tryCreate(context, groupName).map(this::getRate).orElse(Rate.EMPTY);
    }
}
