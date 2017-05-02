package com.bytex.snamp.supervision.def;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Represents voting context.
 */
public interface VotingContext {
    Set<String> getResources();
}
