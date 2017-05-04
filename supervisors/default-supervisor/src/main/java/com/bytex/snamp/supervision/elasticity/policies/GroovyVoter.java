package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.scripting.groovy.Scriptlet;

/**
 * Represents Groovy-based voter for scaling process.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class GroovyVoter extends Scriptlet implements Voter {
    private double voteWeight;

    protected abstract long isUpscaleNeeded();

    protected abstract long isDownscaleNeeded();

    /**
     * Performs voting.
     *
     * @param context An object containing all necessary data for voting by this voter.
     * @return Vote weight: &gt;0 - for scale-out; &lt;0 - for scale-in
     */
    @Override
    public final double vote(final VotingContext context) {
        long factor;
        if ((factor = isUpscaleNeeded()) > 0)
            return voteWeight * factor;
        else if ((factor = isDownscaleNeeded()) > 0)
            return -voteWeight * factor;
        else
            return 0D;
    }

    final void setVoteWeight(final double value){
        voteWeight = value;
    }
}
