package com.bytex.snamp.supervision.def;

import com.bytex.snamp.supervision.elasticity.VotingSubject;

import java.util.Set;

/**
 * Represents a voter in elasticity management process.
 */
public interface Voter {
    /**
     * Performs voting.
     * @param subject Voting subject. Cannot be {@literal null}.
     * @param context An object containing all necessary data for voting by this voter.
     * @return Vote weight. May be zero.
     */
    int vote(final VotingSubject subject, final VotingContext context);
}
