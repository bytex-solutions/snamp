package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.MalfunctionStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.json.DurationDeserializer;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class HealthStatusBasedScalingPolicy extends AbstractWeightedScalingPolicy implements com.bytex.snamp.supervision.elasticity.HealthStatusBasedScalingPolicy {
    private static final String LEVEL_PROPERTY = "level";
    static final String LANGUAGE_NAME = "HealthStatusBased";
    private final MalfunctionStatus.Level level;

    public HealthStatusBasedScalingPolicy(final double voteWeight, final MalfunctionStatus.Level level) {
        super(voteWeight);
        this.level = Objects.requireNonNull(level);
    }

    @JsonCreator
    public HealthStatusBasedScalingPolicy(@JsonProperty(VOTE_WEIGHT_PROPERTY) final double voteWeight,
                                          @JsonProperty(LEVEL_PROPERTY) @JsonDeserialize(using = MalfunctionLevelDeserializer.class) final MalfunctionStatus.Level level,
                                          @JsonProperty(OBSERVATION_TIME_PROPERTY) @JsonDeserialize(using = DurationDeserializer.class) final Duration observationTime,
                                          @JsonProperty(INCREMENTAL_WEIGHT_PROPERTY) final boolean incrementalWeight){
        super(voteWeight);
        this.level = level;
        setObservationTime(observationTime);
        setIncrementalVoteWeight(incrementalWeight);
    }

    @JsonProperty(LEVEL_PROPERTY)
    @JsonSerialize(using = MalfunctionLevelSerializer.class)
    @Override
    public MalfunctionStatus.Level getLevel(){
        return level;
    }

    @Override
    public void configureScriptlet(final ScriptletConfiguration scriptlet) {
        configureScriptlet(scriptlet, LANGUAGE_NAME);
    }

    private static HealthStatus getHealthStatusAndClose(final ManagedResourceConnectorClient client) {
        try {
            return client.queryObject(HealthCheckSupport.class).map(HealthCheckSupport::getStatus).orElseGet(OkStatus::new);
        } finally {
            client.close();
        }
    }

    private double vote(final MalfunctionStatus status) {
        if (status.getLevel().compareTo(level) >= 0) {
            startIfNotStarted();
            return computeVoteWeight();
        } else {
            reset();
            return 0D;
        }
    }

    synchronized double vote(final HealthStatus status) {
        if (status instanceof MalfunctionStatus) {
            return vote((MalfunctionStatus) status);
        } else {
            reset();
            return 0D;
        }
    }

    /**
     * Evaluates scaling policy and obtain vote weight.
     *
     * @param context An object containing all necessary data for voting.
     * @return Vote weight: &gt;0 - for scale-out; &lt;0 - for scale-in
     */
    @Override
    public double evaluate(final ScalingPolicyEvaluationContext context) {
        final BundleContext bc = Utils.getBundleContextOfObject(context);
        assert bc != null;
        HealthStatus summary = new OkStatus();
        for (final String resourceName : context.getResources()) {
            final HealthStatus status = ManagedResourceConnectorClient
                    .tryCreate(bc, resourceName).map(HealthStatusBasedScalingPolicy::getHealthStatusAndClose)
                    .orElseGet(OkStatus::new);
            summary = summary.worst(status);
        }
        return vote(summary);
    }

    static HealthStatusBasedScalingPolicy parse(final String json, final ObjectMapper mapper) throws IOException {
        return mapper.readValue(json, HealthStatusBasedScalingPolicy.class);
    }
}
