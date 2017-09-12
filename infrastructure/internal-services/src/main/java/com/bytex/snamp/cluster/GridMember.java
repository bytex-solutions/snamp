package com.bytex.snamp.cluster;

import com.bytex.snamp.Internal;
import com.bytex.snamp.core.AbstractSharedObjectRepository;
import com.bytex.snamp.core.SharedObjectRepository;
import com.bytex.snamp.internal.Utils;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;

import javax.annotation.Nonnull;
import javax.management.JMException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;

/**
 * @author Roman Sakno
 * @version 2.1.0
 * @since 1.0
 */
public final class GridMember extends AbstractClusterMember {
    private static final class LeaderElectionThread extends Thread implements AutoCloseable {
        private final ILock masterLock;
        private final Member localMember;

        LeaderElectionThread(final HazelcastInstance hazelcast) {
            super("SNAMP-Leader-Election");
            setDaemon(true);
            setPriority(MIN_PRIORITY + 1);
            this.masterLock = hazelcast.getLock("SnampMasterLock");
            this.localMember = hazelcast.getCluster().getLocalMember();
        }

        boolean isActive(){
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

    private static final class GridObjectRepository<S extends GridSharedObject> extends AbstractSharedObjectRepository<S>{
        private static final long serialVersionUID = 7481954461574333295L;
        private final BiFunction<? super HazelcastInstance, ? super String, ? extends S> factory;
        final HazelcastInstance hazelcast;

        GridObjectRepository(final HazelcastInstance hazelcast, final BiFunction<HazelcastInstance, String, ? extends S> factory){
            this.factory = Objects.requireNonNull(factory);
            this.hazelcast = Objects.requireNonNull(hazelcast);
        }

        @Nonnull
        @Override
        protected S createSharedObject(final String name) {
            return factory.apply(hazelcast, name);
        }

        @Override
        protected void releaseSharedObject(final S service) {
            service.destroy();
        }
    }

    private final GridObjectRepository<HazelcastBox> boxes;
    private final GridObjectRepository<HazelcastCounter> counters;
    private final GridObjectRepository<HazelcastCommunicator> communicators;
    private final GridObjectRepository<HazelcastKeyValueStorage> nonPersistentDatabases;
    private LeaderElectionThread electionThread;
    private final ReplicationJob replicator;

    private final boolean shutdownHazelcast;

    private GridMember(final HazelcastInstance hazelcastInstance, final boolean shutdownHazelcast) throws ReflectiveOperationException, IOException, JMException {
        super(hazelcastInstance);
        boxes = new GridObjectRepository<>(hazelcastInstance, HazelcastBox::new);
        counters = new GridObjectRepository<>(hazelcastInstance, HazelcastCounter::new);
        communicators = new GridObjectRepository<>(hazelcastInstance, HazelcastCommunicator::new);
        nonPersistentDatabases = new GridObjectRepository<>(hazelcastInstance, HazelcastKeyValueStorage::new);
        this.electionThread = new LeaderElectionThread(hazelcastInstance);
        this.replicator = new ReplicationJob(this);
        this.shutdownHazelcast = shutdownHazelcast;
    }

    public GridMember(final HazelcastInstance hazelcastInstance) throws ReflectiveOperationException, IOException, JMException {
        this(hazelcastInstance, false);
    }

    private HazelcastInstance getHazelcast(){
        return boxes.hazelcast;
    }

    @Internal
    static GridMember bootstrap() throws JMException, ReflectiveOperationException, IOException{
        return new GridMember(Hazelcast.newHazelcastInstance(), true);
    }
    
    @Override
    public void start() {
        super.start();
        electionThread.start();
        replicator.run();
    }

    @Nonnull
    @Override
    public SharedObjectRepository<HazelcastCounter> getCounters() {
        return counters;
    }

    @Nonnull
    @Override
    public SharedObjectRepository<HazelcastBox> getBoxes() {
        return boxes;
    }

    @Nonnull
    @Override
    public SharedObjectRepository<HazelcastCommunicator> getCommunicators() {
        return communicators;
    }

    @Override
    SharedObjectRepository<HazelcastKeyValueStorage> getNonPersistentDatabases() {
        return nonPersistentDatabases;
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
        electionThread = new LeaderElectionThread(getHazelcast());
        electionThread.start();
    }

    /**
     * Gets attributes associated with this member.
     *
     * @return The attributes associated with this member.
     */
    @Override
    @Nonnull
    public Map<String, ?> getConfiguration() {
        return getHazelcast().getCluster().getLocalMember().getAttributes();
    }

    /**
     * Gets unique name of this node.
     *
     * @return Name of the cluster node.
     */
    @Override
    public String getName() {
        return getHazelcast().getName();
    }

    /**
     * Gets address of this node.
     *
     * @return Address of this node.
     */
    @Override
    public InetSocketAddress getAddress() {
        return getHazelcast().getCluster().getLocalMember().getSocketAddress();
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
        else if (objectType.isInstance(getHazelcast()))
            result = Optional.of(getHazelcast());
        else
            result = Optional.empty();
        return result.map(objectType::cast);
    }

    //only for testing purposes
    //NOT THREAD SAFE
    @Internal
    void destroyLocalServices() {
        boxes.releaseAll();
        communicators.releaseAll();
        counters.releaseAll();
        nonPersistentDatabases.releaseAll();
        dropPersistentDatabase();
    }

    @Override
    public void close() throws Exception {
        final String instanceName = getName();
        getLogger().info(() -> String.format("GridMember service %s is closing. Shutdown Hazelcast? %s", instanceName, shutdownHazelcast ? "yes" : "no"));
        boxes.clear();
        communicators.clear();
        counters.clear();
        nonPersistentDatabases.clear();
        super.close();
        try {
            Utils.closeAll(electionThread, replicator);
        } finally {
            electionThread = null;
            if (shutdownHazelcast)
                getHazelcast().shutdown();
        }
        getLogger().info(() -> String.format("GridMember service %s is closed successfully", instanceName));
    }
}
