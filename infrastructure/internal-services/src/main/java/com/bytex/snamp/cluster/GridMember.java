package com.bytex.snamp.cluster;

import com.bytex.snamp.Internal;
import com.bytex.snamp.core.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;

import javax.annotation.Nonnull;
import javax.management.JMException;
import javax.management.openmbean.InvalidKeyException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.1.0
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

    private final class GridServiceLoader extends CacheLoader<SharedObject.ID<?>, GridSharedObject> {
        @Override
        public GridSharedObject load(@Nonnull final SharedObject.ID<?> key) throws Exception {
            return getOrCreateSharedObject(key, true);
        }
    }

    private final HazelcastInstance hazelcast;
    private final DatabaseNode databaseHost;
    private volatile LeaderElectionThread electionThread;
    private final boolean shutdownHazelcast;
    private final LoadingCache<SharedObject.ID<?>, GridSharedObject> sharedObjects;

    private GridMember(final HazelcastInstance hazelcastInstance, final boolean shutdownHazelcast) throws ReflectiveOperationException, IOException, JMException {
        databaseHost = new DistributedDatabaseNode(hazelcastInstance);
        this.electionThread = new LeaderElectionThread(hazelcastInstance);
        this.hazelcast = hazelcastInstance;
        this.shutdownHazelcast = shutdownHazelcast;
        sharedObjects = CacheBuilder.<SharedObject.ID<?>, GridSharedObject>newBuilder()
                .build(new GridServiceLoader());
    }

    public GridMember(final HazelcastInstance hazelcastInstance) throws ReflectiveOperationException, IOException, JMException {
        this(hazelcastInstance, false);
    }

    @Internal
    static GridMember bootstrap() throws JMException, ReflectiveOperationException, IOException{
        return new GridMember(Hazelcast.newHazelcastInstance(), true);
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

    private GridSharedObject getOrCreateSharedObject(final SharedObject.ID<?> key, final boolean forceCreate) {
        getLogger().fine(() -> String.format("Querying service %s", key));
        if (key instanceof SharedCounter.ID)
            return new HazelcastCounter(hazelcast, key.name);
        else if (key instanceof Communicator.ID)
            return new HazelcastCommunicator(hazelcast, key.name);
        else if (key instanceof SharedBox.ID)
            return new HazelcastBox(hazelcast, key.name);
        else if (key instanceof KeyValueStorage.ID)
            return ((KeyValueStorage.ID) key).persistent ?
                    new OrientKeyValueStorage(databaseHost.getSnampDatabase(), key.name, forceCreate) :
                    new HazelcastKeyValueStorage(hazelcast, key.name);
        else {
            getLogger().warning(() -> String.format("Requested service %s is not supported", key));
            throw new InvalidKeyException(String.format("Service %s is not supported", key));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends SharedObject> Optional<S> getService(final SharedObject.ID<S> objectID) {
        try {
            final GridSharedObject so = sharedObjects.get(objectID);
            return so == null ? Optional.empty() : Optional.of((S) so);
        } catch (final ExecutionException e) {
            getLogger().log(Level.WARNING, String.format("Failed to query service %s", objectID), e);
            return Optional.empty();
        }
    }

    @Override
    public void releaseService(final SharedObject.ID<?> objectID) {
        getLogger().info(() -> String.format("Destroying distributed service %s", objectID));
        GridSharedObject sharedObject = sharedObjects.asMap().remove(objectID);
        if (sharedObject == null)
            sharedObject = getOrCreateSharedObject(objectID, false);
        sharedObject.destroy();
        getLogger().info(() -> String.format("Distributed service %s is destroyed", objectID));
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
