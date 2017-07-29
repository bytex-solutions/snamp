package com.bytex.snamp.cluster;

import com.bytex.snamp.core.ClusterMemberInfo;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents identity of the cluster member.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class HazelcastNodeInfo implements ClusterMemberInfo, Serializable {
    private static final long serialVersionUID = 6007492486747649863L;
    private static final Set<String> ACTIVE_LOCAL_NODES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final boolean active;
    private final String name;
    private final InetSocketAddress address;
    private final ImmutableMap<String, ?> attributes;
    private final String nodeID;

    HazelcastNodeInfo(final Member sender, final boolean isActive, final String name){
        this.name = name;
        this.nodeID = sender.getUuid();
        this.address = sender.getSocketAddress();
        this.attributes = ImmutableMap.copyOf(sender.getAttributes());
        active = isActive;
    }

    HazelcastNodeInfo(final HazelcastInstance hazelcast) {
        this(hazelcast.getCluster().getLocalMember(), isActive(hazelcast.getCluster().getLocalMember()), hazelcast.getName());
    }

    String getNodeID(){
        return nodeID;
    }

    static boolean isActive(final Member clusterMember) {
        return ACTIVE_LOCAL_NODES.contains(clusterMember.getUuid());
    }

    static void setActive(final Member clusterMember, final boolean active) {
        if (active)
            ACTIVE_LOCAL_NODES.add(clusterMember.getUuid());
        else
            ACTIVE_LOCAL_NODES.remove(clusterMember.getUuid());
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
