package com.bytex.snamp.supervision.def;

import com.bytex.snamp.concurrent.Timeout;
import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.supervision.elasticity.ElasticityManager;
import com.bytex.snamp.supervision.elasticity.ScalingMetrics;
import com.bytex.snamp.supervision.elasticity.ScalingMetricsRecorder;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicy;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyEvaluationContext;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.time.Duration;
import java.util.*;

/**
 * Represents eventual implementation of elasticity manager that can be used to make decisions only.
 * <p>
 *     Real scaling logic should be implemented in the derived class.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DefaultElasticityManager implements ElasticityManager, AutoCloseable {
    private static final class CooldownTimer extends Timeout{
        private static final long serialVersionUID = 4146337540311692927L;
        private boolean started;

        CooldownTimer(final Duration ttl) {
            super(ttl);
        }

        Duration getCooldownTime(){
            return getTimeout();
        }

        void start(){
            started = true;
            reset();
        }

        void stop(){
            started = false;
        }

        boolean isCooledDown() {
            return !started || isExpired();
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
    private volatile double scaleInVotes;
    private volatile double scaleOutVotes;
    private final ScalingMetricsRecorder scalingMetrics;
    private CooldownTimer cooldownTimer;
    private int scale;
    private int minClusterSize;
    private int maxClusterSize;

    /**
     * Initializes a new elasticity manager.
     */
    public DefaultElasticityManager() {
        policies = new HashMap<>();
        cooldownTimer = new CooldownTimer(Duration.ZERO);
        scale = 1;
        minClusterSize = 0;
        maxClusterSize = Integer.MAX_VALUE;
        scaleOutVotes = scaleInVotes = 0D;
        scalingMetrics = new ScalingMetricsRecorder();
    }

    /**
     * Adds a new voter to this manager.
     * @param policyName Name of the policy.
     * @param voter A voter that will be used to compute decision about scaling.
     */
    public final synchronized void addScalingPolicy(@Nonnull final String policyName, @Nonnull final ScalingPolicy voter) {
        policies.put(Objects.requireNonNull(policyName), Objects.requireNonNull(voter));
    }

    private ScalingDecision scaleOutDecision(final ScalingPolicyEvaluationContext context) {
        if (context.getResources().size() >= maxClusterSize)
            return ScalingDecision.OUT_OF_SPACE;
        scalingMetrics.upscale();
        cooldownTimer.start();
        return ScalingDecision.SCALE_OUT;
    }

    private ScalingDecision scaleInDecision(final ScalingPolicyEvaluationContext context) {
        if (context.getResources().size() <= minClusterSize)
            return ScalingDecision.NOTHING_TO_DO;
        scalingMetrics.downscale();
        cooldownTimer.start();
        return ScalingDecision.SCALE_IN;
    }

    protected synchronized final ScalingDecision decide(@Nonnull final ScalingPolicyEvaluationContext context, @Nullable final Map<String, Double> ballotBox) {
        double scaleInVotes = 0D, scaleOutVotes = 0D;
        for (final Map.Entry<String, ScalingPolicy> voter : policies.entrySet()) {
            double vote = voter.getValue().evaluate(context);
            if (ballotBox != null)
                ballotBox.put(voter.getKey(), vote);
            if (vote < 0D)
                scaleInVotes += vote;
            else
                scaleOutVotes += vote;
        }
        this.scaleInVotes = scaleInVotes = Math.abs(scaleInVotes);
        this.scaleOutVotes = scaleOutVotes;
        if (cooldownTimer.isCooledDown()) {
            cooldownTimer.stop();
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

    public final void setScalingSize(final int value) {
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
        final int numberOfPolicies = policies.size();
        return numberOfPolicies > 1 ? numberOfPolicies / 2D : numberOfPolicies;
    }

    /**
     * Gets scaling metrics.
     *
     * @return Scaling metrics.
     */
    @Override
    public final ScalingMetrics getScalingMetrics() {
        return scalingMetrics;
    }

    /**
     * Returns a set of supported metrics.
     *
     * @param metricType Type of the metrics.
     * @return Immutable set of metrics.
     */
    @Override
    public <M extends Metric> Iterable<? extends M> getMetrics(final Class<M> metricType) {
        if (metricType.equals(ScalingMetrics.class))
            return Collections.singletonList(metricType.cast(scalingMetrics));
        else
            return Collections.emptyList();
    }

    /**
     * Gets metric by its name.
     *
     * @param metricName Name of the metric.
     * @return An instance of metric; or {@literal null}, if metrics doesn't exist.
     */
    @Override
    public Metric getMetric(final String metricName) {
        return scalingMetrics.getName().equals(metricName) ? scalingMetrics : null;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    @Nonnull
    public Iterator<Metric> iterator() {
        return Collections.<Metric>singletonList(scalingMetrics).iterator();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        scalingMetrics.reset();
        policies.values().forEach(ScalingPolicy::reset);
    }

    @Override
    public final double getVotesForScaleIn() {
        return scaleInVotes;
    }

    @Override
    public double getVotesForScaleOut() {
        return scaleOutVotes;
    }

    /**
     * Gets read-only map of active scaling policies.
     *
     * @return A map of scaling policies.
     */
    @Override
    public final synchronized ImmutableMap<String, ScalingPolicy> getPolicies() {
        return ImmutableMap.copyOf(policies);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        policies.clear();
    }
}
