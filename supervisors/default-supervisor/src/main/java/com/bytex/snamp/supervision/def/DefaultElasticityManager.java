package com.bytex.snamp.supervision.def;

import com.bytex.snamp.ResettableIterator;
import com.bytex.snamp.concurrent.Timeout;
import com.bytex.snamp.connector.metrics.*;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.bytex.snamp.supervision.elasticity.ElasticityManager;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicy;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyEvaluationContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

/**
 * Represents eventual implementation of elasticity manager that can be used to make decisions only.
 * <p>
 *     Real scaling logic should be implemented in the derived class.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DefaultElasticityManager implements ElasticityManager, AutoCloseable {
    private final class CooldownTimer extends Timeout {
        private static final long serialVersionUID = -3781057798278842855L;

        CooldownTimer(final Duration ttl) {
            super(ttl);
        }

        CooldownTimer() {
            this(Duration.ofMinutes(3));
        }

        Duration getCooldownTime() {
            return Duration.ofMillis(timeout);
        }

        boolean cooledDown(){
            return resetIfExpired();
        }
    }

    /**
     * Represents scaling decision.
     */
    protected enum ScalingDecision {
        /**
         * Nothing to do with cluster.
         */
        NOTHING_TO_DO,

        /**
         * Cluster is cools down.
         */
        COOLDOWN,

        /**
         * Shrink the size of a cluster
         */
        SCALE_IN,

        /**
         * Inflate the size of a cluster
         */
        SCALE_OUT,

        /**
         * Indicates that maximum number of resources in the cluster are reached.
         */
        OUT_OF_SPACE
    }

    private final Map<String, ScalingPolicy> policies;
    private final KeyedObjects<String, GaugeFPRecorder> statistics;
    private volatile double scaleInVotes;
    private volatile double scaleOutVotes;
    private final RateRecorder scaleInRate;
    private final RateRecorder scaleOutRate;
    private CooldownTimer cooldownTimer;
    private int scale;
    private int minClusterSize;
    private int maxClusterSize;

    /**
     * Initializes a new elasticity manager.
     */
    public DefaultElasticityManager() {
        policies = new HashMap<>();
        statistics = AbstractKeyedObjects.create(GaugeFPRecorder::getName);
        cooldownTimer = new CooldownTimer();
        scale = 1;
        minClusterSize = 0;
        maxClusterSize = Integer.MAX_VALUE;
        scaleOutVotes = scaleInVotes = 0D;
        scaleInRate = new RateRecorder(SCALE_IN_RATE);
        scaleOutRate = new RateRecorder(SCALE_OUT_RATE);
    }

    /**
     * Adds a new voter to this manager.
     * @param policyName Name of the policy.
     * @param voter A voter that will be used to compute decision about scaling.
     */
    public final void addScalingPolicy(@Nonnull final String policyName, @Nonnull final ScalingPolicy voter) {
        policies.put(Objects.requireNonNull(policyName), Objects.requireNonNull(voter));
        statistics.put(new GaugeFPRecorder(policyName));
    }

    private ScalingDecision scaleOutDecision(final ScalingPolicyEvaluationContext context) {
        if (context.getResources().size() >= maxClusterSize)
            return ScalingDecision.OUT_OF_SPACE;
        scaleOutRate.mark();
        return ScalingDecision.SCALE_OUT;
    }

    private ScalingDecision scaleInDecision(final ScalingPolicyEvaluationContext context) {
        if (context.getResources().isEmpty())
            return ScalingDecision.NOTHING_TO_DO;
        scaleInRate.mark();
        return ScalingDecision.SCALE_IN;
    }

    protected final ScalingDecision decide(final ScalingPolicyEvaluationContext context) {
        double scaleInVotes = 0D, scaleOutVotes = 0D;
        for (final Map.Entry<String, ScalingPolicy> voter : policies.entrySet()) {
            double vote = voter.getValue().evaluate(context);
            if (vote < 0D)
                scaleInVotes += vote;
            else
                scaleOutVotes += vote;
            statistics.get(voter.getKey()).accept(vote);
        }
        if (cooldownTimer.cooledDown()) {
            this.scaleInVotes = scaleInVotes = Math.abs(scaleInVotes);
            this.scaleOutVotes = scaleOutVotes;
            final double castingVote = getCastingVoteWeight();
            if (scaleOutVotes > castingVote)
                return scaleOutDecision(context);
            else if (scaleInVotes > castingVote)
                return scaleInDecision(context);
            else
                return ScalingDecision.NOTHING_TO_DO;
        } else
            return ScalingDecision.COOLDOWN;
    }

    /**
     * Sets maximum size of a cluster.
     *
     * @param value Maximum size of a cluster. Cannot be less than 1.
     */
    public final void setMaxClusterSize(final int value) {
        if (value < 1)
            throw new IllegalArgumentException("Max cluster size cannot be less than 1");
        maxClusterSize = value;
    }

    /**
     * Sets minimum size of a cluster.
     *
     * @param value Minimum size of a cluster.
     */
    public final void setMinClusterSize(final int value) {
        if (value < 0)
            throw new IllegalArgumentException("Max cluster size cannot be less than 1");
        minClusterSize = value;
    }

    /**
     * Gets maximum number of resources in cluster.
     *
     * @return Maximum number of resources in cluster.
     */
    @Override
    public final int getMaxClusterSize() {
        return maxClusterSize;
    }

    /**
     * Gets minimum number of resources in cluster.
     *
     * @return Minimum number of resources in cluster.
     */
    @Override
    public final int getMinClusterSize() {
        return minClusterSize;
    }

    public final void setCooldownTime(@Nonnull final Duration value) {
        cooldownTimer = new CooldownTimer(value);
    }

    @Nonnull
    @Override
    public final Duration getCooldownTime() {
        return cooldownTimer.getCooldownTime();
    }

    public final void setScale(final int value) {
        if (value < 1)
            throw new IllegalArgumentException("Scaling size cannot be less than 1");
        else
            scale = value;
    }

    @Override
    public final int getScalingSize() {
        return scale;
    }

    @Override
    public final double getCastingVoteWeight() {
        return policies.size() / 2D;
    }

    /**
     * Returns a set of supported metrics.
     *
     * @param metricType Type of the metrics.
     * @return Immutable set of metrics.
     */
    @SuppressWarnings("unchecked")
    @Override
    public final  <M extends Metric> Iterable<? extends M> getMetrics(final Class<M> metricType) {
        final Iterable metrics;
        if (metricType.isAssignableFrom(Rate.class))
            metrics = Arrays.asList(scaleInRate, scaleOutRate);
        else if (metricType.isAssignableFrom(GaugeFP.class))
            metrics = statistics.values();
        else
            metrics = ImmutableList.of();
        return (Iterable<? extends M>) metrics;
    }

    /**
     * Gets metric by its name.
     *
     * @param metricName Name of the metric.
     * @return An instance of metric; or {@literal null}, if metrics doesn't exist.
     */
    @Override
    public Metric getMetric(final String metricName) {
        switch (metricName) {
            case SCALE_IN_RATE:
                return scaleInRate;
            case SCALE_OUT_RATE:
                return scaleOutRate;
            default:
                return statistics.get(metricName);
        }
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        scaleInRate.reset();
        scaleOutRate.reset();
        policies.values().forEach(ScalingPolicy::reset);
        statistics.values().forEach(GaugeFP::reset);
    }

    @Override
    public final double getVotesForScaleIn() {
        return scaleInVotes;
    }

    @Override
    public double getVotesForScaleOut() {
        return scaleOutVotes;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        policies.clear();
        statistics.clear();
    }

    @Override
    public final void forEach(final Consumer<? super Metric> action) {
        action.accept(scaleInRate);
        action.accept(scaleOutRate);
        statistics.values().forEach(action);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    @Nonnull
    public final Iterator<Metric> iterator() {
        return Iterators.concat(ResettableIterator.of(scaleInRate, scaleOutRate), statistics.values().iterator());
    }
}
