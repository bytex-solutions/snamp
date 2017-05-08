package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.json.DurationDeserializer;
import com.bytex.snamp.json.RangeSerializer;
import com.bytex.snamp.moa.DoubleReservoir;
import com.bytex.snamp.moa.RangeUtils;
import com.bytex.snamp.moa.ReduceOperation;
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
import java.time.Duration;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.Convert.toDouble;

/**
 * Represents scaling policy based on value of attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MetricBasedScalingPolicy extends AbstractWeightedScalingPolicy {
    static final String LANGUAGE_NAME = "MetricBased";
    private static final String ATTRIBUTE_NAME_PROPERTY = "attributeName";
    private static final String RANGE_PROPERTY = "operationalRange";
    private static final String AGGREGATION_PROPERTY = "aggregation";

    private final Range<Double> operationalRange;
    private final String attributeName;
    private ReduceOperation aggregator;
    private int previousObservation;

    @JsonCreator
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public MetricBasedScalingPolicy(@JsonProperty(ATTRIBUTE_NAME_PROPERTY) final String attributeName,
                                    @JsonProperty(VOTE_WEIGHT_PROPERTY) final double voteWeight,
                                    @JsonProperty(RANGE_PROPERTY) @JsonDeserialize(using = DoubleRangeDeserializer.class) final Range<Double> operationalRange,
                                    @JsonProperty(OBSERVATION_TIME_PROPERTY) @JsonDeserialize(using = DurationDeserializer.class) final Duration observationTime,
                                    @JsonProperty(AGGREGATION_PROPERTY) @JsonDeserialize(using = ReduceOperationDeserializer.class) final ReduceOperation aggregator,
                                    @JsonProperty(INCREMENTAL_WEIGHT_PROPERTY) final boolean incrementalWeight){
        super(voteWeight);
        setObservationTime(observationTime);
        this.operationalRange = Objects.requireNonNull(operationalRange);
        setIncrementalVoteWeight(incrementalWeight);
        this.attributeName = attributeName;
        this.aggregator = Objects.requireNonNull(aggregator);
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

    @JsonProperty(AGGREGATION_PROPERTY)
    @JsonSerialize(using = ReduceOperationSerializer.class)
    public ReduceOperation getAggregator(){
        return aggregator;
    }

    @JsonIgnore
    void setValuesAggregator(@Nonnull final ReduceOperation value){
        aggregator = Objects.requireNonNull(value);
    }

    synchronized double vote(final DoubleReservoir values) {
        final int freshObservation = RangeUtils.getLocation(values.applyAsDouble(aggregator), operationalRange);
        if (freshObservation == 0) {
            reset();
            return 0D;
        } else {
            if (previousObservation != freshObservation)
                restartObservationTimer(); //reset timer if state of observation was changed
            previousObservation = freshObservation;
            return freshObservation * computeVoteWeight();
        }
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public synchronized void reset() {
        super.reset();
        previousObservation = 0;
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

    @Override
    public void configureScriptlet(final ScriptletConfiguration scriptlet) {
        configureScriptlet(scriptlet, LANGUAGE_NAME);
    }
}