package com.bytex.snamp.cluster;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.core.SharedCounter;
import com.bytex.snamp.core.SharedObject;
import com.bytex.snamp.internal.Utils;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

import javax.management.JMException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0.0
 * @since 1.0
 */
public final class GridMember extends DatabaseNode implements ClusterMember, AutoCloseable {

    private static final class LeaderElectionThread extends Thread implements AutoCloseable{
        private final ILock masterLock;
        private volatile boolean lockAcquired;

        private LeaderElectionThread(final HazelcastInstance hazelcast){
            super("LeaderElection");
            setDaemon(true);
            setPriority(MIN_PRIORITY + 1);
            this.masterLock = hazelcast.getLock("SnampMasterLock");
        }

        @Override
        public void run() {
            while (!lockAcquired)
                try {
                    //try to become a master
                    lockAcquired = masterLock.tryLock(3, TimeUnit.MILLISECONDS);
                } catch (final InterruptedException e) {
                    lockAcquired = false;
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
                lockAcquired = false;
            }
        }
    }

    private final Logger logger = Logger.getLogger("com.bytex.snamp.cluster");

    private final HazelcastInstance hazelcast;
    private volatile LeaderElectionThread electionThread;
    private final boolean shutdownHazelcast;

    private GridMember(final HazelcastInstance hazelcastInstance, final boolean shutdownHazelcast) throws ReflectiveOperationException, JAXBException, IOException, JMException{
        super(hazelcastInstance);
        this.electionThread = new LeaderElectionThread(hazelcastInstance);
        this.hazelcast = hazelcastInstance;
        this.shutdownHazelcast = shutdownHazelcast;
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
        return electionThread.lockAcquired;
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
    public <S extends SharedObject> S getService(final String serviceName, final Class<S> serviceType) {
        final Object result;
        if (SHARED_MAP.equals(serviceType))
            result = getStorage(serviceName);
        else if (SHARED_COUNTER.equals(serviceType))
            result = getLongCounter(serviceName);
        else if(COMMUNICATOR.equals(serviceType))
            result = getCommunicator(serviceName);
        else if(SHARED_BOX.equals(serviceType))
            result = getBox(serviceName);
        else if(KV_STORAGE_SERVICE.equals(serviceType))
            result = getKeyValueStorage(serviceName);
        else return null;
        return serviceType.cast(result);
    }

    private KeyValueStorage createPersistentKV(final String collectionName) {
        final DatabaseCredentials credentials = DatabaseCredentials.resolveSnampUser(Utils.getBundleContextOfObject(this), getConfiguration());
        if (credentials == null) {
            logger.severe(String.format("Unable to create persistent storage %s because database credentials are not specified. Non-persistent storage is created.", collectionName));
            return createDistributedKV(collectionName);
        }
        final PersistentKeyValueStorage storage = PersistentKeyValueStorage.openOrCreate(this, collectionName);
        return storage;
    }

    private HazelcastKeyValueStorage createDistributedKV(final String collectionName){
        return new HazelcastKeyValueStorage(hazelcast, collectionName);
    }

    private KeyValueStorage getKeyValueStorage(final String collectionName) {
        return collectionName.startsWith("$") ?
                createPersistentKV(collectionName):
                createDistributedKV(collectionName);
    }

    private HazelcastBox getBox(final String boxName){
        return new HazelcastBox(hazelcast, boxName);
    }

    private HazelcastCommunicator getCommunicator(final String serviceName) {
        return new HazelcastCommunicator(hazelcast, this::isActive, serviceName);
    }

    private HazelcastStorage getStorage(final String collectionName){
        return new HazelcastStorage(hazelcast, collectionName);
    }

    private SharedCounter getLongCounter(final String counterName){
        return new HazelcastCounter(hazelcast, counterName);
    }

    /**
     * Destroys the specified service
     *
     * @param serviceName Name of the service to release.
     * @param serviceType Type of the service to release.
     */
    @Override
    public void releaseService(final String serviceName, final Class<? extends SharedObject> serviceType) {
        if(SHARED_MAP.equals(serviceType))
            HazelcastStorage.destroy(hazelcast, serviceName);
        else if(SHARED_COUNTER.equals(serviceType))
            HazelcastCounter.destroy(hazelcast, serviceName);
        else if(SHARED_BOX.equals(serviceType))
            HazelcastBox.destroy(hazelcast, serviceName);
        else if(COMMUNICATOR.equals(serviceType))
            HazelcastCommunicator.destroy(hazelcast, serviceName);
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
        shutdown();
        try {
            electionThread.close();
        } finally {
            electionThread = null;
            if (shutdownHazelcast)
                hazelcast.shutdown();
        }
        logger.info(() -> String.format("GridMember service %s is closed successfully", instanceName));
    }
}
