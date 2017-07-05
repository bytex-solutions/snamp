package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.LockDecorator;
import com.bytex.snamp.internal.KeyedObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import javax.management.MBeanFeatureInfo;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class ModelOfFeatures<M extends MBeanFeatureInfo, TAccessor extends FeatureAccessor<M>, L extends ResourceFeatureList<M, TAccessor>> {
    private final Map<String, L> features;
    private final Supplier<? extends L> featureListFactory;
    final LockDecorator readLock;
    private final LockDecorator writeLock;

    ModelOfFeatures(final Supplier<? extends L> featureList) {
        featureListFactory = Objects.requireNonNull(featureList);
        features = new HashMap<>(10);
        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = LockDecorator.readLock(rwLock);
        writeLock = LockDecorator.writeLock(rwLock);
    }

    private TAccessor addFeatureImpl(final String resourceName,
                                       final M metadata) throws Exception{
        //find resource storage
        final L list;
        if(features.containsKey(resourceName))
            list = features.get(resourceName);
        else
            features.put(resourceName, list = featureListFactory.get());
        //find feature
        final TAccessor accessor;
        if(list.containsKey(metadata))
            accessor = list.get(metadata);
        else
            list.put(accessor = createAccessor(resourceName, metadata));
        return accessor;
    }

    final TAccessor addFeature(final String resourceName,
                                        final M metadata) throws Exception{
        return writeLock.call(() -> addFeatureImpl(resourceName, metadata), null);
    }

    private TAccessor removeFeatureImpl(final String resourceName,
                                          final M metadata) {
        L list;
        if (features.containsKey(resourceName))
            list = features.get(resourceName);
        else
            return null;
        final TAccessor accessor = list.remove(metadata);
        if (list.isEmpty()) {
            list = features.remove(resourceName);
            cleared(resourceName, list.values());
        }
        return accessor;
    }

    final TAccessor removeFeature(final String resourceName,
                                           final M metadata){
        return writeLock.apply(resourceName, metadata, this::removeFeatureImpl);
    }

    final TAccessor getAccessor(final String resourceName, final String featureName) {
        final L f = features.get(resourceName);
        return f != null ? f.get(featureName) : null;
    }

    final <E extends Throwable> boolean processFeature(final String resourceName,
                                                                final String featureName,
                                                                final Acceptor<? super TAccessor, E> processor) throws E, InterruptedException {
        try (final SafeCloseable ignored = readLock.acquireLock(null)) {
            final TAccessor accessor = getAccessor(resourceName, featureName);
            if (accessor != null) {
                processor.accept(accessor);
                return true;
            } else
                return false;
        } catch (final TimeoutException e) {
            throw new UncheckedTimeoutException(e);
        }
    }

    final <E extends Throwable> boolean processFeature(final String resourceName,
                                                       final Predicate<? super TAccessor> filter,
                                                       final Acceptor<? super TAccessor, E> processor) throws E, InterruptedException {
        try (final SafeCloseable ignored = readLock.acquireLock(null)) {
            final L f = features.get(resourceName);
            final Optional<TAccessor> accessor = f != null ? f.find(filter) : Optional.empty();
            if (accessor.isPresent()) {
                processor.accept(accessor.get());
                return true;
            } else
                return false;
        } catch (final TimeoutException e){
            throw new UncheckedTimeoutException(e);
        }
    }

    /**
     * Returns a read-only set of connected managed resources.
     * @return The read-only set of connected managed resources.
     */
    public final Set<String> getHostedResources(){
        return readLock.apply(features, attrs -> ImmutableSet.copyOf(attrs.keySet()));
    }

    private static Set<String> getFeaturesImpl(final String resourceName,
                                                         final Map<String, ? extends KeyedObjects<String, ?>> attributes) {
        return attributes.containsKey(resourceName) ?
                attributes.get(resourceName).keySet() :
                ImmutableSet.of();
    }

    final Set<String> getResourceFeatures(final String resourceName) {
        return readLock.apply(resourceName, features, ModelOfFeatures::getFeaturesImpl);
    }

    private static <M extends MBeanFeatureInfo, TAccessor extends FeatureAccessor<M>> Collection<M> getFeatures(final String resourceName,
                                                                                                                final Map<String, ? extends KeyedObjects<String, TAccessor>> attributes) {
        final KeyedObjects<String, TAccessor> resource = attributes.get(resourceName);
        if (resource != null) {
            return resource.values().stream()
                    .map(FeatureAccessor::getMetadata)
                    .collect(Collectors.toList());
        } else
            return ImmutableList.of();
    }

    final Collection<M> getResourceFeaturesMetadata(final String resourceName){
        return readLock.apply(resourceName, features, ModelOfFeatures::getFeatures);
    }

    protected void cleared(final String resourceName, final Collection<TAccessor> accessors){

    }

    private Collection<TAccessor> clearImpl(final String resourceName) {
        final Collection<TAccessor> removedFeatures = features.containsKey(resourceName) ?
                features.remove(resourceName).values() :
                ImmutableList.of();
        cleared(resourceName, removedFeatures);
        return removedFeatures;
    }

    /**
     * Removes all attributes from this model and associated with the specified resource.
     * @param resourceName The name of the managed resource.
     * @return The read-only collection of removed attributes.
     */
    public final Collection<TAccessor> clear(final String resourceName) {
        return writeLock.apply(this, resourceName, ModelOfFeatures::clearImpl);
    }

    final <E extends Throwable> boolean forEachFeature(final EntryReader<String, ? super TAccessor, E> featureReader) throws E {
        try(final SafeCloseable ignored = readLock.acquireLock()){
            for (final Map.Entry<String, L> entry : features.entrySet())
                for (final TAccessor accessor : entry.getValue().values())
                    if (!featureReader.accept(entry.getKey(), accessor)) return false;
        }
        return true;
    }

    protected void cleared(){

    }

    private void clearImpl(){
        features.values().forEach(ResourceFeatureList::clear);
        features.clear();
        cleared();
    }

    /**
     * Removes all attributes from this model.
     */
    public final void clear() {
        writeLock.accept(this, ModelOfFeatures::clearImpl);
    }

    protected abstract TAccessor createAccessor(final String resourceName, final M metadata) throws Exception;
}
