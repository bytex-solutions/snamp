package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.json.DurationDeserializer;
import com.bytex.snamp.json.DurationSerializer;
import com.bytex.snamp.json.RangeSerializer;
import com.bytex.snamp.moa.DoubleReservoir;
import com.bytex.snamp.moa.RangeUtils;
import com.bytex.snamp.moa.ReduceOperation;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.JMException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.Convert.toDouble;

/**
 * Represents scaling policy based on value of attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MetricBasedScalingPolicy extends AbstractScalingPolicy {
    static final String LANGUAGE_NAME = "MetricBased";
    private static final String ATTRIBUTE_NAME_PROPERTY = "attributeName";
    private static final String VOTE_WEIGHT_PROPERTY = "voteWeight";
    private static final String RANGE_PROPERTY = "operationalRange";
    private static final String OBSERVATION_TIME_PROPERTY = "observationTime";
    private static final String INCREMENTAL_WEIGHT_PROPERTY = "incrementalWeight";
    private static final String AGGREGATION_PROPERTY = "aggregation";

    private long observationTimeMillis;
    private final Range<Double> operationalRange;
    private boolean incrementalVoteWeight;
    private final String attributeName;
    private ReduceOperation aggregator;
    private int previousObservation;
    private final Stopwatch observationTimer;

    @JsonCreator
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public MetricBasedScalingPolicy(@JsonProperty(ATTRIBUTE_NAME_PROPERTY) final String attributeName,
                                    @JsonProperty(VOTE_WEIGHT_PROPERTY) final double voteWeight,
                                    @JsonProperty(RANGE_PROPERTY) @JsonDeserialize(using = DoubleRangeDeserializer.class) final Range<Double> operationalRange,
                                    @JsonProperty(OBSERVATION_TIME_PROPERTY) @JsonDeserialize(using = DurationDeserializer.class) final Duration observationTime,
                                    @JsonProperty(AGGREGATION_PROPERTY) @JsonDeserialize(using = ReduceOperationDeserializer.class) final ReduceOperation aggregator,
                                    @JsonProperty(INCREMENTAL_WEIGHT_PROPERTY) final boolean incrementalWeight){
        super(voteWeight);
        observationTimeMillis = observationTime.toMillis();
        this.operationalRange = Objects.requireNonNull(operationalRange);
        incrementalVoteWeight = incrementalWeight;
        this.attributeName = attributeName;
        this.aggregator = Objects.requireNonNull(aggregator);
        observationTimer = Stopwatch.createUnstarted();
    }

    public MetricBasedScalingPolicy(final String attributeName,
                             final double voteWeight,
                             final Range<Double> operationalRange) {
        this(attributeName, voteWeight, operationalRange, Duration.ZERO, ReduceOperation.MAX, false);
    }

    @JsonProperty(ATTRIBUTE_NAME_PROPERTY)
    public String getAttributeName(){
        return attributeName;
    }

    @JsonProperty(RANGE_PROPERTY)
    @JsonSerialize(using = RangeSerializer.class)
    public Range<Double> getOperationalRange(){
        return operationalRange;
    }

    @JsonProperty(VOTE_WEIGHT_PROPERTY)
    public double getVoteWeight(){
        return voteWeight;
    }

    @JsonProperty(INCREMENTAL_WEIGHT_PROPERTY)
    public boolean isIncrementalVoteWeight(){
        return incrementalVoteWeight;
    }

    @JsonProperty(OBSERVATION_TIME_PROPERTY)
    @JsonSerialize(using = DurationSerializer.class)
    public Duration getObservationTime(){
        return Duration.ofMillis(observationTimeMillis);
    }

    @JsonProperty(AGGREGATION_PROPERTY)
    @JsonSerialize(using = ReduceOperationSerializer.class)
    public ReduceOperation getAggregator(){
        return aggregator;
    }

    @JsonIgnore
    void setValuesAggregator(@Nonnull final ReduceOperation value){
        aggregator = Objects.requireNonNull(value);
    }

    @JsonIgnore
    void setObservationTime(@Nonnull final Duration value){
        observationTimeMillis = Objects.requireNonNull(value).toMillis();
    }

    @JsonIgnore
    void setIncrementalVoteWeight(final boolean value){
        incrementalVoteWeight = value;
    }

    private double computeVoteWeight(final long elapsedMillis) {
        final long multiplier = (incrementalVoteWeight && observationTimeMillis > 0L) ? (elapsedMillis / observationTimeMillis) : 1L;
        return voteWeight * multiplier;
    }

    synchronized double vote(final DoubleReservoir values) {
        final int freshObservation = RangeUtils.getLocation(values.applyAsDouble(aggregator), operationalRange);
        if (freshObservation == 0) {
            reset();
            return 0D;
        }
        else {
            if (previousObservation != freshObservation)
                observationTimer.reset().start(); //reset timer if state of observation was changed
            previousObservation = freshObservation;
            final long elapsedMillis = observationTimer.elapsed(TimeUnit.MILLISECONDS);
            return elapsedMillis >= observationTimeMillis ? (freshObservation * computeVoteWeight(elapsedMillis)) : 0D;
        }
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public synchronized void reset() {
        previousObservation = 0;
        observationTimer.reset();
    }

    private static void putAttributeIntoReservoir(final ManagedResourceConnectorClient connector,
                                                  final String attributeName,
                                                  final DoubleReservoir reservoir,
                                                  final Logger logger) {
        final Object attributeValue;
        try {
            attributeValue = connector.getAttribute(attributeName);
        } catch (final JMException e) {
            logger.log(Level.SEVERE, String.format("Scaling policy for attribute %s cannot be evaluated", attributeName), e);
            return;
        } finally {
            connector.close();
        }
        final OptionalDouble convertedValue = toDouble(attributeValue);
        if (convertedValue.isPresent())
            reservoir.add(convertedValue.getAsDouble());
        else
            logger.warning(String.format("Scaling policy for attribute %s cannot be evaluated. Value %s cannot be converted to floating-point number", attributeName, attributeValue));
    }

    /**
     * Performs voting.
     *
     * @param context An object containing all necessary data for voting by this voter.
     * @return Vote weight: &gt;0 - for scale-out; &lt;0 - for scale-in
     */
    @Override
    public double evaluate(final ScalingPolicyEvaluationContext context) {
        final BundleContext bc = Utils.getBundleContextOfObject(context);
        assert bc != null;

        final int resources = context.getResources().size();
        switch (resources) {
            case 0:
                return 0D;
            default:
                final DoubleReservoir reservoir = new DoubleReservoir(resources);
                final String attributeName = this.attributeName;
                final Logger logger = LoggerProvider.getLoggerForObject(context);
                for (final String resourceName : context.getResources())
                    ManagedResourceConnectorClient.tryCreate(bc, resourceName).ifPresent(client -> putAttributeIntoReservoir(client, attributeName, reservoir, logger));
                return reservoir.getSize() == 0 ? 0D : vote(reservoir);
        }
    }

    static MetricBasedScalingPolicy parse(final String json, final ObjectMapper mapper) throws IOException {
        return mapper.readValue(json, MetricBasedScalingPolicy.class);
    }

    public void configureScriptlet(final ScriptletConfiguration scriptlet) {
        scriptlet.setURL(false);
        scriptlet.setLanguage(LANGUAGE_NAME);
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
