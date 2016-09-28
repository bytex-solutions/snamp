package com.bytex.snamp.cluster;

import com.bytex.snamp.core.ClusterMemberInfo;
import com.google.common.collect.ImmutableMap;
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

    HazelcastNodeInfo(final Member sender, final boolean active, final String name){
        this.active = active;
        this.name = name;
        this.address = sender.getSocketAddress();
        this.attributes = ImmutableMap.copyOf(sender.getAttributes());
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
}
