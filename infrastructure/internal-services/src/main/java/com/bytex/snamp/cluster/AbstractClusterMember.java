package com.bytex.snamp.cluster;

import com.bytex.snamp.concurrent.WeakRepeater;
import com.bytex.snamp.core.*;
import com.bytex.snamp.internal.Utils;
import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.common.exception.OException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.InstanceNotFoundException;
import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final class ReplicationJob extends WeakRepeater<AbstractClusterMember>{

        private ReplicationJob(final AbstractClusterMember input) {
            super(Duration.ofSeconds(10), input);
        }

        @Override
        protected void doAction() throws ReplicationSupport.ReplicationException, InterruptedException {
            getReferenceOrTerminate().replicate();
        }
    }

    private static final String REPLICATION_STORAGE_NAME = "SNAMP_REPLICATION_STORAGE";
    private final DatabaseNode database;
    private final ReplicationJob replicator;

    AbstractClusterMember() {
        database = callAndWrapException(DatabaseNode::new, DatabaseBootstrapException::new);
        replicator = new ReplicationJob(this);
    }

    AbstractClusterMember(final HazelcastInstance hazelcast) {
        database = callAndWrapException(() -> new DistributedDatabaseNode(hazelcast), DatabaseBootstrapException::new);
        replicator = new ReplicationJob(this);
    }

    SharedObjectRepository<? extends KeyValueStorage> getNonPersistentDatabases() {
        return ClusterMember.super.getKeyValueDatabases(false);
    }

    final Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @SuppressWarnings("unchecked")
    private static void replicate(final KeyValueStorage replicationStorage,
                                  final ReplicationSupport serviceToReplicate,
                                  final boolean masterNode) throws ReplicationSupport.ReplicationException {
        /*
            Master node always used as a source for replication
            Slave nodes always should be in-sync with master node
         */
        if (masterNode) {
            final Serializable replica = serviceToReplicate.createReplica();
            replicationStorage.updateOrCreateRecord(serviceToReplicate.getReplicaName(), KeyValueStorage.SerializableRecordView.class, record -> record.setValue(replica));
        } else {
            final Optional<Serializable> replica =
                    replicationStorage.getRecord(serviceToReplicate.getReplicaName(), KeyValueStorage.SerializableRecordView.class)
                            .map(KeyValueStorage.SerializableRecordView::getValue);
            if (replica.isPresent())
                serviceToReplicate.loadFromReplica(replica.get());
        }
    }

    private void replicate() throws ReplicationSupport.ReplicationException {
        final BundleContext context = Utils.getBundleContextOfObject(this);
        final KeyValueStorage replicationStorage = getNonPersistentDatabases().getSharedObject(REPLICATION_STORAGE_NAME);
        for (final ServiceReference<ReplicationSupport> serviceRef : new DefaultServiceSelector().setServiceType(ReplicationSupport.class)
                .getServiceReferences(context, ReplicationSupport.class)) {
            final ServiceHolder<ReplicationSupport> service;
            try {
                service = new ServiceHolder<>(context, serviceRef);
            } catch (final InstanceNotFoundException e) {
                getLogger().log(Level.SEVERE, "Unable to replicate service " + serviceRef, e);
                continue;
            }
            try {
                if (Thread.interrupted())   //replicator is interrupted
                    return;
                else
                    replicate(replicationStorage, service.get(), isActive());
            } finally {
                service.release(context);
            }
        }
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
        replicator.run();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        database.shutdown();
        replicator.close(replicator.getPeriod());
    }
}
