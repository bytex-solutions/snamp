package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.MalfunctionStatus;
import com.bytex.snamp.connector.health.ResourceMalfunctionStatus;
import com.bytex.snamp.json.DurationDeserializer;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.1
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

    private double vote(final ResourceMalfunctionStatus status) {
        if (status.getLevel().compareTo(level) >= 0) {
            startIfNotStarted();
            return computeVoteWeight();
        } else {
            reset();
            return 0D;
        }
    }

    synchronized double vote(final HealthStatus status) {
        if (status instanceof ResourceMalfunctionStatus) {  //ignore cluster status
            return vote((ResourceMalfunctionStatus) status);
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
        return vote(context.getHealthStatus().getSummaryStatus());
    }

    static HealthStatusBasedScalingPolicy parse(final String json, final ObjectMapper mapper) throws IOException {
        return mapper.readValue(json, HealthStatusBasedScalingPolicy.class);
    }
}
