package com.bytex.snamp.core;

import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
final class ProxyClusterMember implements ClusterMember {
    private final BundleContext context;

    ProxyClusterMember(final BundleContext context) {
        this.context = Objects.requireNonNull(context);
    }

    private <S> S processClusterNode(final Function<? super ClusterMember, S> processor) {
        final Optional<ServiceHolder<ClusterMember>> memberRef = ServiceHolder.tryCreate(context, ClusterMember.class);
        if (memberRef.isPresent()) {
            final ServiceHolder<ClusterMember> member = memberRef.get();
            try {
                return processor.apply(member.get());
            } finally {
                member.release(context);
            }
        } else
            return processor.apply(ClusterMember.get(null));
    }

    @Override
    @Nonnull
    public SharedObjectRepository<? extends SharedCounter> getCounters() {
        return processClusterNode(ClusterMember::getCounters);
    }


    @Override
    @Nonnull
    public SharedObjectRepository<? extends SharedBox> getBoxes() {
        return processClusterNode(ClusterMember::getBoxes);
    }

    @Override
    @Nonnull
    public SharedObjectRepository<? extends Communicator> getCommunicators() {
        return processClusterNode(ClusterMember::getCommunicators);
    }

    @Override
    @Nonnull
    public SharedObjectRepository<? extends KeyValueStorage> getKeyValueDatabases(final boolean persistent) {
        return processClusterNode(member -> member.getKeyValueDatabases(persistent));
    }

    @Override
    public void resign() {
        processClusterNode(member -> {
            member.resign();
            return null;
        });
    }

    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        return processClusterNode(member -> member.queryObject(objectType));
    }

    @Override
    public boolean isActive() {
        return processClusterNode(ClusterMember::isActive);
    }

    @Override
    public String getName() {
        return processClusterNode(ClusterMember::getName);
    }

    @Override
    public InetSocketAddress getAddress() {
        return processClusterNode(ClusterMember::getAddress);
    }

    @Override
    @Nonnull
    public Map<String, ?> getConfiguration() {
        return processClusterNode(ClusterMember::getConfiguration);
    }

    @Override
    public int hashCode() {
        return context.hashCode();
    }

    private boolean equals(final ProxyClusterMember other) {
        return Objects.equals(context, other.context);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ProxyClusterMember && equals((ProxyClusterMember) other);
    }
}
