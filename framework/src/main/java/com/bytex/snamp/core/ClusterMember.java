package com.bytex.snamp.core;

import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents SNAMP service that represents single SNAMP member in the cluster.
 * You can discover this service via OSGi service registry.
 * <p>
 *     You can query the following cluster-wide services:
 *     <ul>
 *         <li>{@link SharedCounter} for generating unique identifiers</li>
 *         <li>{@link KeyValueStorage} for accessing data collections</li>
 *         <li>{@link SharedBox} for accessing single object</li>
 *         <li>{@link Communicator} for sending messages between actors</li>
 *     </ul>
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see SharedCounter
 */
public interface ClusterMember extends ClusterMemberInfo, SupportService {
    /**
     * Marks this node as passive and execute leader election.
     */
    void resign();

    /**
     * Gets distributed service.
     *
     * @param serviceName Service name.
     * @param serviceType Service type.
     * @param <S>         Type of the service contract.
     * @return Distributed service.
     *
     * @see SharedObjectType#COUNTER
     * @see SharedObjectType#KV_STORAGE
     * @see SharedObjectType#COMMUNICATOR
     * @see SharedObjectType#BOX
     */
    <S extends SharedObject> Optional<S> getService(final String serviceName, final SharedObjectType<S> serviceType);

    /**
     * Destroys the specified service
     *
     * @param serviceName Name of the service to release.
     * @param serviceType Type of the service to release.
     */
    void releaseService(final String serviceName, final SharedObjectType<?> serviceType);

    static ClusterMember get(final BundleContext context) {
        if (context == null)
            return LocalMember.getInstance();
        else
            return new ClusterMember() {
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
                        return processor.apply(LocalMember.getInstance());
                }

                @Override
                public void resign() {
                    processClusterNode(member -> {
                        member.resign();
                        return null;
                    });
                }

                @Override
                public <S extends SharedObject> Optional<S> getService(final String serviceName, final SharedObjectType<S> serviceType) {
                    return processClusterNode(member -> member.getService(serviceName, serviceType));
                }

                @Override
                public void releaseService(final String serviceName, final SharedObjectType<?> serviceType) {
                    processClusterNode(member -> {
                        member.releaseService(serviceName, serviceType);
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
                public Map<String, ?> getAttributes() {
                    return processClusterNode(ClusterMember::getAttributes);
                }
            };
    }

    static boolean isInCluster(final BundleContext context) {
        return !get(context).queryObject(LocalMember.class).isPresent();
    }
}
