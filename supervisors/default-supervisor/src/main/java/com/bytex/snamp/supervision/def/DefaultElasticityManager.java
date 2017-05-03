package com.bytex.snamp.supervision.def;

import com.bytex.snamp.supervision.elasticity.ElasticityManager;
import com.bytex.snamp.supervision.elasticity.VotingSubject;

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
    private final List<Voter> voters;
    private final AtomicIntegerArray votes;  //store voting results
    private Duration cooldownTime;
    private int scale;

    public DefaultElasticityManager() {
        voters = new ArrayList<>();
        cooldownTime = Duration.ofMinutes(3);
        scale = 1;
        votes = new AtomicIntegerArray(VotingSubject.values().length);
    }

    public final void addVoter(final Voter voter){

    }

    protected void applyDecision(final VotingSubject subject){

    }

    protected boolean applyDecision(final VotingSubject subject, final VotingContext context) {
        int votes = voters.stream().mapToInt(voter -> voter.vote(subject, context)).sum();
        this.votes.set(subject.ordinal(), votes);
        final boolean approved;
        if (approved = votes >= getCastingVoteWeight())
            applyDecision(subject);
        return approved;
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
    public int getCastingVoteWeight() {
        return voters.size() / 2 + 1;
    }

    @Override
    public int getVotes(@Nonnull final VotingSubject subject) {
        return votes.get(subject.ordinal());
    }
}
