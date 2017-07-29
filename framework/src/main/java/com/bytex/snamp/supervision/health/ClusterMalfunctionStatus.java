package com.bytex.snamp.supervision.health;

import com.bytex.snamp.connector.health.MalfunctionStatus;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * Indicates that the cluster with resources is in malfunction state.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class ClusterMalfunctionStatus extends MalfunctionStatus {
    private static final long serialVersionUID = -4762866679904079839L;

    protected ClusterMalfunctionStatus(@Nonnull final Instant timeStamp) {
        super(timeStamp);
    }
}
