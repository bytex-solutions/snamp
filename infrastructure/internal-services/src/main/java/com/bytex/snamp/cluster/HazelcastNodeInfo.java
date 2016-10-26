package com.bytex.snamp.cluster;

import com.bytex.snamp.core.ClusterMemberInfo;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

import java.net.InetSocketAddress;

/**
 * Represents identity of the cluster member.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HazelcastNodeInfo implements ClusterMemberInfo {
    private final boolean active;
    private final String name;
    private final InetSocketAddress address;
    private final ImmutableMap<String, ?> attributes;
    private final String nodeID;

    HazelcastNodeInfo(final Member sender, final boolean active, final String name){
        this.active = active;
        this.name = name;
        this.nodeID = sender.getUuid();
        this.address = sender.getSocketAddress();
        this.attributes = ImmutableMap.copyOf(sender.getAttributes());
    }

    HazelcastNodeInfo(final HazelcastInstance hazelcast, final boolean active){
        this(hazelcast.getCluster().getLocalMember(), active, hazelcast.getName());
    }

    String getNodeID(){
        return nodeID;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public ImmutableMap<String, ?> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return nodeID;
    }

    private boolean equals(final HazelcastNodeInfo other){
        return nodeID.equals(other.nodeID);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof HazelcastNodeInfo && equals((HazelcastNodeInfo)other);
    }

    @Override
    public int hashCode() {
        return nodeID.hashCode();
    }
}
