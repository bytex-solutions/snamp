package com.bytex.snamp.cluster;

import com.bytex.snamp.TypeTokens;
import com.bytex.snamp.core.AbstractFrameworkService;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LongCounter;
import com.google.common.reflect.TypeToken;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

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
public final class GridMember extends AbstractFrameworkService implements ClusterMember, AutoCloseable {
    private static final class LeaderElectionThread extends Thread implements AutoCloseable{
        private final ILock masterLock;
        private volatile boolean lockAcquired;

        private LeaderElectionThread(final HazelcastInstance hazelcast){
            super("LeaderElection");
            setDaemon(true);
            setPriority(MIN_PRIORITY);
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
                    break;
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
    private LeaderElectionThread electionThread;
    private final boolean shutdownHazelcast;

    private GridMember(final HazelcastInstance hazelcastInstance, final boolean shutdown){
        this.electionThread = new LeaderElectionThread(hazelcastInstance);
        this.hazelcast = hazelcastInstance;
        electionThread.start();
        this.shutdownHazelcast = shutdown;
    }

    public GridMember(final HazelcastInstance hazelcastInstance){
        this(hazelcastInstance, false);
    }

    GridMember(){
        this(Hazelcast.newHazelcastInstance(), true);
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

    @Override
    public int getNeighbors() {
        return hazelcast.getCluster().getMembers().size() - 1;
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
    public <S> S getService(final String serviceName, final TypeToken<S> serviceType) {
        final Object result;
        if (STORAGE_SERVICE.equals(serviceType))
            result = getStorage(serviceName);
        else if (IDGEN_SERVICE.equals(serviceType))
            result = getLongCounter(serviceName);
        else if(COMMUNICATION_SERVICE.equals(serviceType))
            result = getCommunicator(serviceName);
        else if(BOX.equals(serviceType))
            result = getBox(serviceName);
        else return null;
        return TypeTokens.cast(result, serviceType);
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

    private LongCounter getLongCounter(final String counterName){
        return hazelcast.getAtomicLong(counterName)::getAndIncrement;
    }

    /**
     * Destroys the specified service
     *
     * @param serviceName Name of the service to release.
     * @param serviceType Type of the service to release.
     */
    @Override
    public void releaseService(final String serviceName, final TypeToken<?> serviceType) {
        if(STORAGE_SERVICE.equals(serviceType))
            releaseStorage(serviceName);
        else if(IDGEN_SERVICE.equals(serviceType))
            releaseLongCounter(serviceName);
    }

    private void releaseStorage(final String collectionName) {
        hazelcast.getMap(collectionName).destroy();
    }

    private void releaseLongCounter(final String generatorName) {
        hazelcast.getIdGenerator(generatorName).destroy();
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    @Aggregation(cached = true)
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

    @Override
    public synchronized void close() throws InterruptedException {
        try {
            electionThread.close();
        }finally {
            electionThread = null;
            if (shutdownHazelcast)
                hazelcast.shutdown();
            clearCache();
        }
    }
}
