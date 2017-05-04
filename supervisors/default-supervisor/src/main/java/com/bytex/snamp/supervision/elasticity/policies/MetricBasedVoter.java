package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.moa.DoubleReservoir;
import com.bytex.snamp.moa.RangeUtils;
import com.bytex.snamp.moa.ReduceOperation;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.JMException;
import java.time.Duration;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.Convert.toDouble;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class MetricBasedVoter extends AbstractVoter {
    private long observationTimeMillis;
    private final Range<Double> operationalRange;
    private boolean incrementalVoteWeight;
    private final String attributeName;
    private ReduceOperation aggregator;
    private int previousObservation;
    private final Stopwatch observationTimer;

    MetricBasedVoter(final String attributeName,
                     final double voteWeight,
                     final Range<Double> operationalRange) {
        super(voteWeight);
        observationTimeMillis = 0;
        this.operationalRange = Objects.requireNonNull(operationalRange);
        incrementalVoteWeight = false;
        this.attributeName = attributeName;
        aggregator = ReduceOperation.MEDIAN;
        observationTimer = Stopwatch.createUnstarted();
    }

    void setValuesAggregator(@Nonnull final ReduceOperation value){
        aggregator = Objects.requireNonNull(value);
    }

    void setObservationTime(@Nonnull final Duration value){
        observationTimeMillis = Objects.requireNonNull(value).toMillis();
    }

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
            return elapsedMillis >= observationTimeMillis ? computeVoteWeight(elapsedMillis) : 0D;
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
    public double vote(final VotingContext context) {
        final BundleContext bc = Utils.getBundleContextOfObject(context);
        assert bc != null;
        final DoubleReservoir reservoir = new DoubleReservoir(context.getResources().size());
        final String attributeName = this.attributeName;
        final Logger logger = LoggerProvider.getLoggerForObject(context);
        for (final String resourceName : context.getResources())
            ManagedResourceConnectorClient.tryCreate(bc, resourceName).ifPresent(client -> putAttributeIntoReservoir(client, attributeName, reservoir, logger));
        return vote(reservoir);
    }
}
