package com.bytex.snamp.core;

import com.bytex.snamp.Switch;
import org.osgi.framework.BundleContext;

/**
 * Represents a set of cluster services.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ClusterServices {
    private static final Switch<Class<?>, Object> inMemoryServices = new Switch<Class<?>, Object>()
            .equals(IDGenerator.class, new InMemoryIDGenerator())
            .equals(ObjectStorage.class, new InMemoryObjectStorage());

    private ClusterServices(){
    }

    /**
     * Gets process-local ID generator.
     * @return Process-local ID generator.
     */
    public static IDGenerator getProcessLocalIDGenerator(){
        return (IDGenerator) inMemoryServices.apply(IDGenerator.class);
    }

    /**
     * Gets process-local storage of objects.
     * @return Process-local storage of objects.
     */
    public static ObjectStorage getProcessLocalObjectStorage(){
        return (ObjectStorage) inMemoryServices.apply(ObjectStorage.class);
    }

    private static <S> S getService(final BundleContext context,
                                                     final Class<S> serviceType) {
        ServiceHolder<ClusterNode> holder = null;
        try{
            holder = new ServiceHolder<>(context, ClusterNode.class);
            return holder.getService().queryObject(serviceType);
        } catch (final IllegalArgumentException ignored){ //service not found
            return serviceType.cast(inMemoryServices.apply(serviceType));
        }finally {
            if(holder != null)
                holder.release(context);
        }
    }

    public static ObjectStorage getClusteredObjectStorage(final BundleContext context){
        return getService(context, ObjectStorage.class);
    }

    public static IDGenerator getClusteredIDGenerator(final BundleContext context){
        return getService(context, IDGenerator.class);
    }
}
