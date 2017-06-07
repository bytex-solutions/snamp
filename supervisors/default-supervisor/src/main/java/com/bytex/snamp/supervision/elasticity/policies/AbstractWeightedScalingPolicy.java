package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.scriptlet.ScriptletConfigurationSupport;
import com.bytex.snamp.json.DurationSerializer;
import com.bytex.snamp.supervision.elasticity.WeightedScalingPolicy;
import com.google.common.base.Stopwatch;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractWeightedScalingPolicy implements ScalingPolicy, WeightedScalingPolicy, ScriptletConfigurationSupport {
    static final String OBSERVATION_TIME_PROPERTY = "observationTime";
    static final String VOTE_WEIGHT_PROPERTY = "voteWeight";
    static final String INCREMENTAL_WEIGHT_PROPERTY = "incrementalWeight";

    private final double voteWeight;
    private final Stopwatch observationTimer;
    private long observationTimeMillis;
    private boolean incrementalVoteWeight;

    AbstractWeightedScalingPolicy(final double voteWeight) {
        this.voteWeight = voteWeight;
        observationTimer = Stopwatch.createUnstarted();
    }

    @JsonProperty(OBSERVATION_TIME_PROPERTY)
    @JsonSerialize(using = DurationSerializer.class)
    public final Duration getObservationTime(){
        return Duration.ofMillis(observationTimeMillis);
    }

    @JsonIgnore
    final void setObservationTime(@Nonnull final Duration value){
        observationTimeMillis = Objects.requireNonNull(value).toMillis();
    }

    @JsonProperty(VOTE_WEIGHT_PROPERTY)
    @Override
    public final double getVoteWeight(){
        return voteWeight;
    }

    @JsonIgnore
    final void setIncrementalVoteWeight(final boolean value){
        incrementalVoteWeight = value;
    }

    @JsonProperty(INCREMENTAL_WEIGHT_PROPERTY)
    public final boolean isIncrementalVoteWeight(){
        return incrementalVoteWeight;
    }

    final double computeVoteWeight() {
        final long elapsedMillis = observationTimer.elapsed(TimeUnit.MILLISECONDS);
        if (elapsedMillis >= observationTimeMillis) {
            final long multiplier = (incrementalVoteWeight && observationTimeMillis > 0L) ? (elapsedMillis / observationTimeMillis) : 1L;
            return voteWeight * multiplier;
        } else
            return 0D;
    }

    final void restartObservationTimer(){
        observationTimer.reset().start();
    }

    final void startIfNotStarted() {
        if (!observationTimer.isRunning())
            restartObservationTimer();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void reset() {
        observationTimer.reset();
    }

    final void configureScriptlet(final ScriptletConfiguration scriptlet, final String language){
        scriptlet.setURL(false);
        scriptlet.setLanguage(language);
        final ObjectMapper mapper = new ObjectMapper();
        final String json;
        try {
            json = mapper.writeValueAsString(this);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        scriptlet.setScript(json);
    }
}
