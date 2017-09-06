package com.bytex.snamp.core;

import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

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
 * @version 2.1
 * @since 1.0
 * @see SharedCounter
 */
public interface ClusterMember extends ClusterMemberInfo, SupportService {
    /**
     * Marks this node as passive and execute leader election.
     */
    void resign();

    /**
     * Gets repository of counters.
     * @return Repository of counters.
     */
    @Nonnull
    default SharedObjectRepository<? extends SharedCounter> getCounters() {
        return LocalMember.getCounters();
    }

    /**
     * Gets repository of boxes.
     * @return Repository of boxes.
     */
    @Nonnull
    default SharedObjectRepository<? extends SharedBox> getBoxes(){
        return LocalMember.getBoxes();
    }

    /**
     * Gets repository of communicators.
     * @return Repository of communicators.
     */
    @Nonnull
    default SharedObjectRepository<? extends Communicator> getCommunicators(){
        return LocalMember.getCommunicators();
    }

    /**
     * Gets repository of key/value data stores.
     * @param persistent {@literal true} to request repository of persistent databases; {@literal false} to request repository of in-memory databases.
     * @return Repository of databases.
     * @implNote This method may return non-persistent database even if {@code persistent} parameter is specified to {@literal true}.
     *          It can be happened because cluster member doesn't support persistent database. Please use {@link KeyValueStorage#isPersistent()}
     *          to check persistence of the database after calling this method.
     */
    @Nonnull
    default SharedObjectRepository<? extends KeyValueStorage> getKeyValueDatabases(final boolean persistent) {
        return LocalMember.getNonPersistentStores();
    }

    @Override
    default boolean isActive(){
        return true;
    }

    @Override
    default String getName() {
        return LocalMember.getName();
    }

    @Override
    default InetSocketAddress getAddress() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    default Map<String, ?> getConfiguration() {
        return (Map) System.getProperties();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object.
     */
    @Override
    default <T> Optional<T> queryObject(@Nonnull final Class<T> objectType){
        return Optional.of(this).filter(objectType::isInstance).map(objectType::cast);
    }

    /**
     * Gets cluster member that executes the calling code.
     * @param context Bundle context of the calling code. Use {@literal null} to obtain local cluster member.
     * @return Information about cluster member.
     */
    @Nonnull
    static ClusterMember get(final BundleContext context) {
        return context == null ? () -> { } : new ProxyClusterMember(context);
    }

    static boolean isInCluster(final BundleContext context) {
        return get(context).getAddress() != null;
    }
}
