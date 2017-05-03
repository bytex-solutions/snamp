package com.bytex.snamp.supervision.elasticity;

import java.util.SortedSet;

/**
 * Represents scaling action.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public enum ScalingAction {
    /**
     * Shrink the size of a cluster
     */
    SCALE_IN,

    /**
     * Inflate the size of a cluster
     */
    SCALE_OUT
}
