package com.bytex.snamp.cluster;

import com.bytex.snamp.TypeTokens;
import com.bytex.snamp.core.AbstractFrameworkService;
import com.bytex.snamp.core.ClusterMember;
import com.google.common.reflect.TypeToken;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0.0
 * @since 1.0
 */
public final class GridMember extends AbstractFrameworkService implements ClusterMember, AutoCloseable {
    private static final class LeaderElectionThread extends Thread{
        private final ILock masterLock;
        private volatile boolean lockAcquired;

        private LeaderElectionThread(final HazelcastInstance hazelcast){
            super("LeaderElection");
            setDaemon(true);
            setPriority(MIN_PRIORITY);
            this.masterLock = hazelcast.getLock("SnampMasterLock");
        }

        private void resign(){
            masterLock.forceUnlock();
            lockAcquired = false;
        }

        @Override
        public void run() {
            while (!lockAcquired)
                try {
                    //try to become a master
                    lockAcquired = masterLock.tryLock(3, TimeUnit.MILLISECONDS);
                } catch (final InterruptedException e) {
                    break;
                }
        }
    }
    private final Logger logger = Logger.getLogger("com.bytex.snamp.cluster");

    private final HazelcastInstance hazelcast;
    private LeaderElectionThread electionThread;

    public GridMember(final HazelcastInstance hazelcastInstance){
        this.electionThread = new LeaderElectionThread(hazelcastInstance);
        this.hazelcast = hazelcastInstance;
        electionThread.start();
    }

    /**
     * Determines whether this node is active.
     * <p/>
     * Passive SNAMP node ignores any notifications received by resource connectors.
     * As a result, all resource adapters will not route notifications to the connected
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
        electionThread.interrupt();
        try {
            electionThread.join();
        } catch (final InterruptedException e) {
            getLogger().log(Level.SEVERE, "Election thread interrupted", e);
            return;
        }
        finally {
            electionThread.resign();
        }
        electionThread = new LeaderElectionThread(hazelcast);
        electionThread.start();
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
        if(STORAGE_SERVICE.equals(serviceType))
            return TypeTokens.cast(new HazelcastStorage(hazelcast, serviceName), serviceType);
        else if(IDGEN_SERVICE.equals(serviceType))
            return TypeTokens.cast(new HazelcastLongCounter(hazelcast, serviceName), serviceType);
        else return null;
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
            HazelcastStorage.release(hazelcast, serviceName);
        else if(IDGEN_SERVICE.equals(serviceType))
            HazelcastLongCounter.release(hazelcast, serviceName);
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
        electionThread.interrupt();
        try {
            electionThread.join();
        }finally {
            electionThread.resign();
            electionThread = null;
            clearCache();
        }
    }
}
