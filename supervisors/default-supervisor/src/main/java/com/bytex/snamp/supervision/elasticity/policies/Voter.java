package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.Stateful;

/**
 * Represents a voter in elasticity management process.
 */
public interface Voter extends Stateful {
    Voter VOICELESS = new Voter() {
        @Override
        public double vote(final VotingContext context) {
            return 0D;
        }

        @Override
        public void reset() {

        }
    };
    
    /**
     * Performs voting.
     * @param context An object containing all necessary data for voting by this voter.
     * @return Vote weight: &gt;0 - for scale-out; &lt;0 - for scale-in
     */
    double vote(final VotingContext context);
}
