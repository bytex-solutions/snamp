package com.bytex.snamp.cluster;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.SharedObject;
import com.bytex.snamp.core.SharedObjectType;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.management.JMException;
import javax.management.openmbean.InvalidKeyException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0.0
 * @since 1.0
 */
public final class GridMember implements ClusterMember, AutoCloseable {

    private static final class LeaderElectionThread extends Thread implements AutoCloseable {
        private final ILock masterLock;
        private final Member localMember;

        private LeaderElectionThread(final HazelcastInstance hazelcast) {
            super("LeaderElection");
            setDaemon(true);
            setPriority(MIN_PRIORITY + 1);
            this.masterLock = hazelcast.getLock("SnampMasterLock");
            this.localMember = hazelcast.getCluster().getLocalMember();
        }

        private boolean isActive(){
            return HazelcastNodeInfo.isActive(localMember);
        }

        @Override
        public void run() {
            while (!HazelcastNodeInfo.isActive(localMember))
                try {
                    //try to become a master
                    HazelcastNodeInfo.setActive(localMember, masterLock.tryLock(3, TimeUnit.MILLISECONDS));
                } catch (final InterruptedException e) {
                    HazelcastNodeInfo.setActive(localMember, false);
                    return;
                }
        }

        @Override
        public void close() throws InterruptedException {
            interrupt();
            try {
                join();
            } finally {
                masterLock.forceUnlock();
                HazelcastNodeInfo.setActive(localMember, false);
            }
        }
    }

    @Immutable
    private static final class GridServiceKey<S extends SharedObject> extends SharedObjectType<S> {
        private final String serviceName;

        private GridServiceKey(final String serviceName, final SharedObjectType<S> definition) {
            super(definition);
            this.serviceName = serviceName;
        }

        private boolean represents(final SharedObjectType<?> definition){
            return Objects.equals(getType(), definition.getType()) && isPersistent() == definition.isPersistent();
        }

        private boolean equals(final GridServiceKey<?> other){
            return serviceName.equals(other.serviceName) && Objects.equals(getType(), other.getType()) && isPersistent() == other.isPersistent();
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof GridServiceKey<?> && equals((GridServiceKey<?>) other);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceName, getType(), isPersistent());
        }

        @Override
        public String toString() {
            return "LocalServiceKey{" +
                    "persistent=" + isPersistent() +
                    ", objectType=" + getType() +
                    ", serviceName=" + serviceName +
                    '}';
        }
    }

    private final class GridServiceLoader extends CacheLoader<GridServiceKey<?>, GridSharedObject> {
        @Override
        public GridSharedObject load(@Nonnull final GridServiceKey<?> key) throws Exception {
            return getOrCreateSharedObject(key, true);
        }
    }

    private final HazelcastInstance hazelcast;
    private final DatabaseNode databaseHost;
    private volatile LeaderElectionThread electionThread;
    private final boolean shutdownHazelcast;
    private final LoadingCache<GridServiceKey<?>, GridSharedObject> sharedObjects;

    private GridMember(final HazelcastInstance hazelcastInstance, final boolean shutdownHazelcast) throws ReflectiveOperationException, JAXBException, IOException, JMException {
        databaseHost = new DatabaseNode(hazelcastInstance);
        this.electionThread = new LeaderElectionThread(hazelcastInstance);
        this.hazelcast = hazelcastInstance;
        this.shutdownHazelcast = shutdownHazelcast;
        sharedObjects = CacheBuilder.<GridServiceKey<?>, GridSharedObject>newBuilder()
                .build(new GridServiceLoader());
    }

    public GridMember(final HazelcastInstance hazelcastInstance) throws ReflectiveOperationException, JAXBException, IOException, JMException {
        this(hazelcastInstance, false);
    }

    GridMember() throws JMException, ReflectiveOperationException, IOException, JAXBException {
        this(Hazelcast.newHazelcastInstance(), true);
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    public void start() throws ReflectiveOperationException, IOException {
        databaseHost.startupFromConfiguration().activate();
        electionThread.start();
    }

    /**
     * Determines whether this node is active.
     * <p/>
     * Passive SNAMP node ignores any notifications received by resource connector.
     * As a result, all gateways will not route notifications to the connected
     * monitoring tools. But you can still read any attributes.
     *
     * @return {@literal true}, if this node is active; otherwise, {@literal false}.
     */
    @Override
    public boolean isActive() {
        return electionThread.isActive();
    }

    /**
     * Marks this node as passive and execute leader election.
     */
    @Override
    public synchronized void resign() {
        try {
            electionThread.close();
        } catch (final InterruptedException e) {
            getLogger().log(Level.SEVERE, "Election thread interrupted", e);
            return;
        }
        electionThread = new LeaderElectionThread(hazelcast);
        electionThread.start();
    }

    /**
     * Gets attributes associated with this member.
     *
     * @return The attributes associated with this member.
     */
    @Override
    public Map<String, ?> getAttributes() {
        return hazelcast.getCluster().getLocalMember().getAttributes();
    }

    /**
     * Gets unique name of this node.
     *
     * @return Name of the cluster node.
     */
    @Override
    public String getName() {
        return hazelcast.getName();
    }

    private GridSharedObject getOrCreateSharedObject(final GridServiceKey<?> key, final boolean forceCreate) {
        getLogger().fine(() -> String.format("Querying service %s", key));
        if (key.represents(SharedObjectType.COUNTER))
            return new HazelcastCounter(hazelcast, key.serviceName);
        else if (key.represents(SharedObjectType.COMMUNICATOR))
            return new HazelcastCommunicator(hazelcast, key.serviceName);
        else if (key.represents(SharedObjectType.BOX))
            return new HazelcastBox(hazelcast, key.serviceName);
        else if (key.represents(SharedObjectType.KV_STORAGE))
            return new HazelcastKeyValueStorage(hazelcast, key.serviceName);
        else if (key.represents(SharedObjectType.PERSISTENT_KV_STORAGE))
            return new OrientKeyValueStorage(databaseHost.getSnampDatabase(), key.serviceName, forceCreate);
        else {
            getLogger().warning(() -> String.format("Requested service %s is not supported", key));
            throw new InvalidKeyException(String.format("Service %s is not supported", key));
        }
    }

    /**
     * Gets distributed service.
     *
     * @param serviceName Service name.
     * @param serviceType Service type.
     * @return Distributed service; or {@literal null}, if service is not supported.
     */
    @Override
    public <S extends SharedObject> Optional<S> getService(final String serviceName, final SharedObjectType<S> serviceType) {
        GridSharedObject result;
        try {
            result = sharedObjects.get(new GridServiceKey<>(serviceName, serviceType));
        } catch (final ExecutionException e) {
            getLogger().log(Level.WARNING, String.format("Failed to query service %s with name %s", serviceType, serviceName), e);
            result = null;
        }
        return serviceType.cast(result);
    }

    /**
     * Destroys the specified service
     *
     * @param serviceName Name of the service to release.
     * @param serviceType Type of the service to release.
     */
    @Override
    public void releaseService(final String serviceName, final SharedObjectType<?> serviceType) {
        final GridServiceKey<?> serviceKey = new GridServiceKey<>(serviceName, serviceType);
        getLogger().info(() -> String.format("Destroying distributed service %s", serviceKey));
        GridSharedObject sharedObject = sharedObjects.asMap().remove(serviceKey);
        if (sharedObject == null)
            sharedObject = getOrCreateSharedObject(serviceKey, false);
        if (sharedObject == null)
            getLogger().info(() -> String.format("Distributed service %s cannot be destroyed because it doesn't exist", serviceKey));
        else {
            sharedObject.destroy();
            getLogger().info(() -> String.format("Distributed service %s is destroyed", serviceKey));
        }
    }

    /**
     * Gets address of this node.
     *
     * @return Address of this node.
     */
    @Override
    public InetSocketAddress getAddress() {
        return hazelcast.getCluster().getLocalMember().getSocketAddress();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        final Optional<?> result;
        if (objectType.isInstance(this))
            result = Optional.of(this);
        else if (objectType.isInstance(hazelcast))
            result = Optional.of(hazelcast);
        else
            result = Optional.empty();
        return result.map(objectType::cast);
    }

    //only for testing purposes
    //NOT THREAD SAFE
    void destroyLocalServices(){
        sharedObjects.asMap().values().forEach(GridSharedObject::destroy);
        sharedObjects.invalidateAll();
    }

    @Override
    public void close() throws InterruptedException {
        final String instanceName = getName();
        getLogger().info(() -> String.format("GridMember service %s is closing. Shutdown Hazelcast? %s", instanceName, shutdownHazelcast ? "yes" : "no"));
        databaseHost.shutdown();
        try {
            electionThread.close();
        } finally {
            electionThread = null;
            sharedObjects.invalidateAll();
            if (shutdownHazelcast)
                hazelcast.shutdown();
        }
        getLogger().info(() -> String.format("GridMember service %s is closed successfully", instanceName));
    }
}
