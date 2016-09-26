package com.bytex.snamp.connector.md;

/**
 * Represents internal specification of message-driven connector.
 * @since 2.0
 * @version 2.0
 */
public final class ConnectorSpecification {
    private final boolean synchronizeOverCluster; //synchronize all events across cluster automatically
    private final boolean sharedReceiver;         //use single message receiver for all instances of connector

    private ConnectorSpecification(final boolean synchronizeOverCluster, final boolean sharedReceiver){
        this.synchronizeOverCluster = synchronizeOverCluster;
        this.sharedReceiver = sharedReceiver;
    }
}
