package com.bytex.snamp.cluster;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.SharedObject;
import com.bytex.snamp.core.SharedObjectDefinition;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;

import javax.annotation.Nonnull;
import javax.management.JMException;
import javax.management.openmbean.InvalidKeyException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0.0
 * @since 1.0
 */
public final class GridMember extends DatabaseNode implements ClusterMember, AutoCloseable {

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

    private static final class GridServiceKey<S extends SharedObject> extends SharedObjectDefinition<S> {
        private final String serviceName;

        private GridServiceKey(final String serviceName, final SharedObjectDefinition<S> definition) {
            super(definition);
            this.serviceName = serviceName;
        }

        private boolean represents(final SharedObjectDefinition<?> definition){
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
            if (key.represents(SHARED_COUNTER))
                return new HazelcastCounter(hazelcast, key.serviceName);
            else if (key.represents(COMMUNICATOR))
                return new HazelcastCommunicator(hazelcast, key.serviceName);
            else if (key.represents(SHARED_BOX))
                return new HazelcastBox(hazelcast, key.serviceName);
            else if (key.represents(KV_STORAGE))
                return new HazelcastKeyValueStorage(hazelcast, key.serviceName);
            else if (key.represents(PERSISTENT_KV_STORAGE))
                return new OrientKeyValueStorage(getSnampDatabase(), key.serviceName);
            else throw new InvalidKeyException(String.format("Service %s is not supported", key));
        }
    }

    private final Logger logger = Logger.getLogger("com.bytex.snamp.cluster");

    private final HazelcastInstance hazelcast;
    private volatile LeaderElectionThread electionThread;
    private final boolean shutdownHazelcast;
    private final LoadingCache<GridServiceKey<?>, GridSharedObject> sharedObjects;

    private GridMember(final HazelcastInstance hazelcastInstance, final boolean shutdownHazelcast) throws ReflectiveOperationException, JAXBException, IOException, JMException {
        super(hazelcastInstance);
        this.electionThread = new LeaderElectionThread(hazelcastInstance);
        this.hazelcast = hazelcastInstance;
        this.shutdownHazelcast = shutdownHazelcast;
        sharedObjects = CacheBuilder.<GridServiceKey<?>, GridSharedObject>newBuilder()
                .removalListener(GridMember::cleanupSharedObject)
                .build(new GridServiceLoader());
    }

    private static void cleanupSharedObject(final RemovalNotification<GridServiceKey<?>, GridSharedObject> notification){
        if (!notification.wasEvicted() && notification.getValue() != null)
            notification.getValue().destroy();
    }

    public GridMember(final HazelcastInstance hazelcastInstance) throws ReflectiveOperationException, JAXBException, IOException, JMException {
        this(hazelcastInstance, false);
    }

    GridMember() throws JMException, ReflectiveOperationException, IOException, JAXBException {
        this(Hazelcast.newHazelcastInstance(), true);
    }

    @Override
    public GridMember startupFromConfiguration() throws InvocationTargetException, NoSuchMethodException, IOException {
        super.startupFromConfiguration();
        electionThread.start();
        return this;
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

    /**
     * Gets distributed service.
     *
     * @param serviceName Service name.
     * @param serviceType Service type.
     * @return Distributed service; or {@literal null}, if service is not supported.
     */
    @Override
    public <S extends SharedObject> S getService(final String serviceName, final SharedObjectDefinition<S> serviceType) {
        GridSharedObject result;
        try {
            result = sharedObjects.get(new GridServiceKey<>(serviceName, serviceType));
        } catch (final ExecutionException e) {
            logger.log(Level.WARNING, String.format("Failed to query service %s with name %s", serviceType, serviceName), e);
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
    public void releaseService(final String serviceName, final SharedObjectDefinition<?> serviceType) {
        sharedObjects.invalidate(new GridServiceKey<>(serviceName, serviceType));
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return logger;
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
    public <T> T queryObject(final Class<T> objectType) {
        final Object result;
        if (objectType.isInstance(this))
            result = this;
        else if (objectType.isInstance(logger))
            result = logger;
        else if (objectType.isInstance(hazelcast))
            result = hazelcast;
        else
            return null;
        return objectType.cast(result);
    }

    @Override
    public void close() throws InterruptedException {
        final String instanceName = getName();
        logger.info(() -> String.format("GridMember service %s is closing. Shutdown Hazelcast? %s", instanceName, shutdownHazelcast ? "yes" : "no"));
        if(shutdownHazelcast)
            sharedObjects.invalidateAll();
        else
            sharedObjects.cleanUp();
        shutdown();
        try {
            electionThread.close();
        } finally {
            electionThread = null;
            if (shutdownHazelcast) {
                hazelcast.shutdown();
            }
        }
        logger.info(() -> String.format("GridMember service %s is closed successfully", instanceName));
    }
}
