package com.bytex.snamp.core;

import com.google.common.collect.ImmutableMap;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Represents information about local cluster member.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class LocalMember implements ClusterMemberInfo {
    static final ClusterMemberInfo INSTANCE = new LocalMember();

    private LocalMember(){
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String getName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public Map<String, ?> getAttributes() {
        return ImmutableMap.of();
    }
}
