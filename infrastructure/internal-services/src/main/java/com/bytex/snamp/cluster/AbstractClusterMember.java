package com.bytex.snamp.cluster;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.core.SharedObjectRepository;
import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.common.exception.OException;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
abstract class AbstractClusterMember implements ClusterMember, AutoCloseable {
    private static final class DatabaseBootstrapException extends OException{
        private static final long serialVersionUID = 7893128210010888754L;

        private DatabaseBootstrapException(final Exception e){
            super("Unable to activate database node");
            initCause(e);
        }
    }

    private final DatabaseNode database;

    AbstractClusterMember() {
        database = callAndWrapException(DatabaseNode::new, DatabaseBootstrapException::new);
    }

    AbstractClusterMember(final HazelcastInstance hazelcast) {
        database = callAndWrapException(() -> new DistributedDatabaseNode(hazelcast), DatabaseBootstrapException::new);
    }

    SharedObjectRepository<? extends KeyValueStorage> getNonPersistentDatabases() {
        return ClusterMember.super.getKeyValueDatabases(false);
    }

    final void dropPersistentDatabase(){
        database.dropDatabases();
    }

    @Nonnull
    @Override
    public final SharedObjectRepository<? extends KeyValueStorage> getKeyValueDatabases(final boolean persistent) {
        return persistent ? database : getNonPersistentDatabases();
    }

    @OverridingMethodsMustInvokeSuper
    public void start(){
        callAndWrapException(() -> database.startupFromConfiguration().activate(), DatabaseBootstrapException::new);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        database.shutdown();
    }
}
