package com.bytex.snamp.core;

import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a set of distributed services.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class DistributedServices {

    private DistributedServices(){
        throw new InstantiationError();
    }

    private static <S> S processClusterNode(final BundleContext context,
                                            final Function<? super ClusterMember, S> processor) {
        if (context == null)
            return processor.apply(LocalMember.getInstance());
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

    public static ClusterMember getMember(final BundleContext context){
        return new ClusterMember() {
            @Override
            public void resign() {
                processClusterNode(context, member -> {
                    member.resign();
                    return null;
                });
            }

            @Override
            public <S extends SharedObject> Optional<S> getService(final String serviceName, final SharedObjectType<S> serviceType) {
                return processClusterNode(context, member -> member.getService(serviceName, serviceType));
            }

            @Override
            public void releaseService(final String serviceName, final SharedObjectType<?> serviceType) {
                processClusterNode(context, member -> {
                    member.releaseService(serviceName, serviceType);
                    return null;
                });
            }

            @Override
            public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
                return processClusterNode(context, member -> member.queryObject(objectType));
            }

            @Override
            public boolean isActive() {
                return processClusterNode(context, ClusterMember::isActive);
            }

            @Override
            public String getName() {
                return processClusterNode(context, ClusterMember::getName);
            }

            @Override
            public InetSocketAddress getAddress() {
                return processClusterNode(context, ClusterMember::getAddress);
            }

            @Override
            public Map<String, ?> getAttributes() {
                return processClusterNode(context, ClusterMember::getAttributes);
            }
        };
    }
}
