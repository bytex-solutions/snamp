package com.bytex.snamp.core;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Implemented by service when cluster-wide replication and synchronization required.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
public interface ReplicationSupport<R extends Serializable> {
    /**
     * Indicates failure in replication.
     */
    class ReplicationException extends Exception{
        private static final long serialVersionUID = 6146683368403803161L;

        public ReplicationException(final Exception e){
            super(e);
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    /**
     * Gets unique name of replica. This name should be the same for each node in the cluster.
     * @return Unique name of replica.
     */
    String getReplicaName();

    /**
     * Takes snapshot to be distributed across cluster nodes.
     * @return Snapshot of the internal state.
     * @throws ReplicationException Unable to take snapshot.
     */
    @Nonnull
    R createReplica() throws ReplicationException;

    /**
     * Loads replica.
     * @param replica Replica to load. Cannot be {@literal null}.
     */
    void loadFromReplica(@Nonnull final R replica) throws ReplicationException;
}

