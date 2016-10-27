package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.metrics.AbstractMetric;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.io.Serializable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.*;

import static com.bytex.snamp.concurrent.LockManager.*;

/**
 * Represents a holder for metric.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MetricHolderAttribute<M extends AbstractMetric> extends MessageDrivenAttribute<CompositeData> {
    private static final long serialVersionUID = 2645456225474793148L;
    private M metric;
    private final Predicate<? super Serializable> isInstance;
    private final ReadWriteLock lockManager;

    MetricHolderAttribute(final String name,
                          final CompositeType type,
                          final String description,
                          final AttributeDescriptor descriptor,
                          final Function<? super String, ? extends M> metricFactory) {
        super(name, type, description, AttributeSpecifier.READ_ONLY, descriptor);
        metric = metricFactory.apply(name);
        assert metric != null;
        isInstance = metric.getClass()::isInstance;
        lockManager = new ReentrantReadWriteLock();
    }

    private long extractAsLongImpl(final ToLongFunction<? super M> extractor){
        return extractor.applyAsLong(metric);
    }

    final long extractAsLong(final ToLongFunction<? super M> extractor, final long defval){
        return lockAndApplyAsLong(lockManager.readLock(), this, extractor, MetricHolderAttribute<M>::extractAsLongImpl).orElse(defval);
    }

    private int extractAsIntImpl(final ToIntFunction<? super M> extractor){
        return extractor.applyAsInt(metric);
    }

    final int extractAsInt(final ToIntFunction<? super M> extractor, final int defval){
        return lockAndApplyAsInt(lockManager.readLock(), this, extractor, MetricHolderAttribute<M>::extractAsIntImpl).orElse(defval);
    }

    private <O> O extractImpl(final Function<? super M, ? extends O> extractor){
        return extractor.apply(metric);
    }

    final <O> O extract(final Function<? super M, O> extractor, final O defval){
        return lockAndApply(lockManager.readLock(), this, extractor, MetricHolderAttribute<M>::extractImpl).orElse(defval);
    }

    private double extractAsDoubleImpl(final ToDoubleFunction<? super M> extractor){
        return extractor.applyAsDouble(metric);
    }

    final double extractAsDouble(final ToDoubleFunction<? super M> extractor, final double defval){
        return lockAndApplyAsDouble(lockManager.readLock(), this, extractor, MetricHolderAttribute<M>::extractAsDoubleImpl).orElse(defval);
    }

    abstract CompositeData getValue(final M metric);

    @Override
    protected final CompositeData getValue() {
        return getValue(metric);
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
}
