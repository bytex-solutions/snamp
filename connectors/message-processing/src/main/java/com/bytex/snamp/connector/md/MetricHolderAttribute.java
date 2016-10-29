package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.AbstractMetric;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.io.Serializable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.bytex.snamp.concurrent.LockManager.lockAndAccept;
import static com.bytex.snamp.concurrent.LockManager.lockAndApply;

/**
 * Represents a holder for metric.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MetricHolderAttribute<M extends AbstractMetric> extends DistributedAttribute<CompositeData> implements AutoCloseable {
    private static final long serialVersionUID = 2645456225474793148L;
    private M metric;
    private final Predicate<? super Serializable> isInstance;
    /*
        Handling notification and reading metric can be parallel, therefore, read lock is used
        Taking snapshot and state recovery must be executed with exclusive access, therefore, write lock is used
     */
    private final ReadWriteLock lockManager;

    MetricHolderAttribute(final String name,
                          final CompositeType type,
                          final String description,
                          final AttributeDescriptor descriptor,
                          final Function<? super String, ? extends M> metricFactory) {
        super(name, type, description, descriptor);
        metric = metricFactory.apply(name);
        assert metric != null;
        isInstance = metric.getClass()::isInstance;
        lockManager = new ReentrantReadWriteLock();
    }

    abstract CompositeData getValue(final M metric);

    private CompositeData getValueImpl(){
        return getValue(metric);
    }

    @Override
    protected final CompositeData getValue() throws InterruptedException {
        return lockAndApply(lockManager.readLock(), this, MetricHolderAttribute::getValueImpl, Function.identity());
    }

    @SuppressWarnings("unchecked")
    private M takeSnapshotImpl(){
        final AbstractMetric result = metric.clone();
        assert isInstance.test(result);
        return (M) result;
    }

    @Override
    protected final M takeSnapshot() {
        //taking snapshot is an exclusive operation. Another thread cannot modify the metric
        return lockAndApply(lockManager.writeLock(), this, MetricHolderAttribute::takeSnapshotImpl).orElse(null);
    }

    @SuppressWarnings("unchecked")
    private void loadFromSnapshotImpl(final Serializable snapshot){
        if(isInstance.test(snapshot))
            metric = (M) snapshot;
    }

    @Override
    protected final void loadFromSnapshot(final Serializable snapshot) {
        lockAndAccept(lockManager.writeLock(), this, snapshot, MetricHolderAttribute::loadFromSnapshotImpl);
    }

    abstract boolean updateMetric(final M metric, final MeasurementNotification notification);

    private boolean acceptImpl(final MeasurementNotification notification){
        return updateMetric(metric, notification);
    }

    @Override
    protected final boolean accept(final MeasurementNotification notification) {
        return lockAndApply(lockManager.readLock(), this, notification, MetricHolderAttribute::acceptImpl).orElse(Boolean.FALSE);
    }

    private void closeImpl() {
        metric = null;
    }

    @Override
    public final void close() throws InterruptedException {
        lockAndAccept(lockManager.writeLock(), this, MetricHolderAttribute::closeImpl, Function.identity());
    }
}
