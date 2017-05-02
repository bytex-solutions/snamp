package com.bytex.snamp.supervision.def;

import com.bytex.snamp.moa.DoubleReservoir;
import com.bytex.snamp.supervision.elasticity.ElasticityManager;
import com.bytex.snamp.supervision.elasticity.VotingSubject;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DefaultElasticityManager implements ElasticityManager {
    private final List<Voter> voters;
    private final int[] votes;
    private Duration cooldownTime;
    private int scale;

    public DefaultElasticityManager() {
        voters = new ArrayList<>();
        cooldownTime = Duration.ofMinutes(3);
        scale = 1;
        votes = new int[VotingSubject.values().length];
    }

    public final void addVoter(final Voter voter){

    }

    protected void applyDecision(final VotingSubject subject){

    }

    protected boolean applyDecision(final VotingSubject subject, final VotingContext context) {
//        final DoubleReservoir ballotBox = new DoubleReservoir(voters.size());
//        for (final Voter voter : voters)
//            ballotBox.accept(voter.vote(subject, context));
//        final double votes = ballotBox.sum();
//        this.votes[subject.ordinal()] = votes;
//        final boolean approved;
//        if (approved = ballotBox.poll())
//            applyDecision(subject);
//        return approved;
        return true;
    }

    public final void setCooldownTime(@Nonnull final Duration value){
        cooldownTime = Objects.requireNonNull(value);
    }

    @Nonnull
    @Override
    public final Duration getCooldownTime() {
        return cooldownTime;
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
    public int getVotes() {
        return 0;
    }

    @Override
    public double getCastingVoteWeight() {
        return 0;
    }

    @Override
    public int getUpScalingVotes() {
        return 0;
    }

    @Override
    public int getDownScalingVotes() {
        return 0;
    }
}
