package com.bytex.snamp.supervision.elasticity;

/**
 * Represents state of elasticity management process.
 */
public enum ElasticityManagementState {
    /**
     * Elasticity manager executing scaling action.
     * <p>
     *     Outbound transitions: {@link #COOLDOWN}.
     */
    PERFORMING_ACTION,
    /**
     * Elasticity manager is in idle state.
     * <p>
     *     Outbound transitions: {@link #PERFORMING_ACTION}.
     */
    IDLE,

    /**
     *
     */
    COOLDOWN
}
