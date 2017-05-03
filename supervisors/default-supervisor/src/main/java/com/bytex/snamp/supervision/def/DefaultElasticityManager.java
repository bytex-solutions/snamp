package com.bytex.snamp.supervision.def;

import com.bytex.snamp.concurrent.Timeout;
import com.bytex.snamp.connector.metrics.Rate;
import com.bytex.snamp.connector.metrics.RateRecorder;
import com.bytex.snamp.supervision.elasticity.ElasticityManagementState;
import com.bytex.snamp.supervision.elasticity.ElasticityManager;
import com.bytex.snamp.supervision.elasticity.ScalingAction;
import com.google.common.util.concurrent.AtomicDoubleArray;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DefaultElasticityManager implements ElasticityManager {
    private final class CooldownTimer extends Timeout {
        CooldownTimer(final Duration ttl) {
            super(ttl);
        }

        CooldownTimer() {
            this(Duration.ofMinutes(3));
        }

        Duration getCooldownTime() {
            return Duration.ofMillis(timeout);
        }
    }

    private final List<Voter> voters;
    private volatile double scaleInVotes;
    private volatile double scaleOutVotes;
    private final RateRecorder scaleInRate;
    private final RateRecorder scaleOutRate;
    private CooldownTimer cooldownTimer;
    private int scale;
    private ElasticityManagementState state;

    public DefaultElasticityManager() {
        voters = new ArrayList<>();
        cooldownTimer = new CooldownTimer();
        scale = 1;
        scaleOutVotes = scaleInVotes = 0D;
        scaleInRate = new RateRecorder("scaleIn");
        scaleOutRate = new RateRecorder("scaleOut");
    }

    private void setVotes(final ScalingAction action, final double votes) {
        switch (action) {
            case SCALE_IN:
                scaleInVotes = votes;
                return;
            case SCALE_OUT:
                scaleOutVotes = votes;
        }
    }

    public final void addVoter(final Voter voter){

    }

    protected void applyDecision(final ScalingAction action){

    }

    protected boolean applyDecision(final ScalingAction action, final VotingContext context) {
        double votes = voters.stream().mapToDouble(voter -> voter.vote(action, context)).sum();
        setVotes(action, votes);
        final boolean approved;
        if (approved = votes >= getCastingVoteWeight()) {
            cooldownTimer.acceptIfExpired(this, action, DefaultElasticityManager::applyDecision);
        }
        return approved;
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
    public final int getScale() {
        return scale;
    }

    @Override
    public Rate getActionRate(@Nonnull final ScalingAction action) {
        switch (action) {
            case SCALE_IN:
                return scaleInRate;
            case SCALE_OUT:
                return scaleOutRate;
            default:
                return null;
        }
    }

    @Nonnull
    @Override
    public ElasticityManagementState getState() {
        return null;
    }

    @Override
    public double getCastingVoteWeight() {
        return voters.size() / 2D;
    }

    @Override
    public double getVotes(@Nonnull final ScalingAction subject) {
        switch (subject) {
            case SCALE_IN:
                return scaleInVotes;
            case SCALE_OUT:
                return scaleOutVotes;
            default:
                return Double.NaN;
        }
    }
}
