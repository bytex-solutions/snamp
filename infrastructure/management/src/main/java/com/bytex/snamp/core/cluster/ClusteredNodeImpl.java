package com.bytex.snamp.core.cluster;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.ClusterNode;
import com.bytex.snamp.core.IDGenerator;
import com.bytex.snamp.core.ObjectStorage;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ClusteredNodeImpl extends AbstractAggregator implements ClusterNode {
    private static final String NODE_STATUS_PROPERTY = "snampNodeStatus";
    private final Logger logger = Logger.getLogger("com.bytex.snamp.core.cluster");

    private static final class HazelcastIDGenerator implements IDGenerator{
        private final HazelcastInstance hazelcast;

        private HazelcastIDGenerator(final HazelcastInstance hazelcast){
            this.hazelcast = hazelcast;
        }

        @Override
        public long generateID(String generatorName) {
            return hazelcast.getIdGenerator(generatorName).newId();
        }

        @Override
        public void reset(String generatorName) {
            hazelcast.getIdGenerator(generatorName).destroy();
        }
    }

    private static final class HazelcastObjectStorage implements ObjectStorage{
        private final HazelcastInstance hazelcast;

        private HazelcastObjectStorage(final HazelcastInstance hazelcast){
            this.hazelcast = hazelcast;
        }

        @Override
        public ConcurrentMap<String, Object> getCollection(final String collectionName) {
            return hazelcast.getMap(collectionName);
        }

        /**
         * Deletes the specified collection.
         *
         * @param collectionName Name of the collection to remove.
         */
        @Override
        public void deleteCollection(final String collectionName) {
            hazelcast.getMap(collectionName).destroy();
        }
    }

    private final Member localMember;
    private final String instanceName;
    private final IDGenerator idGenerator;
    private final ObjectStorage storageService;

    public ClusteredNodeImpl(final HazelcastInstance hazelcastInstance){
        instanceName = hazelcastInstance.getName();
        localMember = hazelcastInstance.getCluster().getLocalMember();
        localMember.setBooleanAttribute(NODE_STATUS_PROPERTY, true);
        storageService = new HazelcastObjectStorage(hazelcastInstance);
        idGenerator = new HazelcastIDGenerator(hazelcastInstance);
    }

    @Aggregation
    @SpecialUse
    public ObjectStorage getObjectStorage(){
        return storageService;
    }

    @Aggregation
    @SpecialUse
    public IDGenerator getIDGenerator(){
        return idGenerator;
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
        return localMember.getBooleanAttribute(NODE_STATUS_PROPERTY);
    }

    /**
     * Marks this node as active or passive.
     *
     * @param value {@literal true} to activate the node in the cluster; otherwise, {@literal false}.
     */
    @Override
    public void setActive(final boolean value) {
        localMember.setBooleanAttribute(NODE_STATUS_PROPERTY, value);
    }

    /**
     * Gets unique name of this node.
     *
     * @return Name of the cluster node.
     */
    @Override
    public String getName() {
        return instanceName;
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
}
