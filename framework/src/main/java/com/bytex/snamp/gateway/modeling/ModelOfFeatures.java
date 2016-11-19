package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.internal.KeyedObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.management.MBeanFeatureInfo;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class ModelOfFeatures<M extends MBeanFeatureInfo, TAccessor extends FeatureAccessor<M>, L extends ResourceFeatureList<M, TAccessor>> extends ThreadSafeObject {
    private final Enum<?> listGroup;
    private final Map<String, L> features;
    private final Supplier<? extends L> featureListFactory;

    <G extends Enum<G>> ModelOfFeatures(final Supplier<? extends L> featureList, final Class<G> resourceGroupDef, final Enum<G> listGroup) {
        super(resourceGroupDef);
        this.listGroup = Objects.requireNonNull(listGroup);
        featureListFactory = Objects.requireNonNull(featureList);
        features = new HashMap<>(10);
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
        return writeLock.call(listGroup, () -> addFeatureImpl(resourceName, metadata), null);
    }

    private TAccessor removeFeatureImpl(final String resourceName,
                                          final M metadata){
        final L list;
        if(features.containsKey(resourceName))
            list = features.get(resourceName);
        else return null;
        final TAccessor accessor = list.remove(metadata);
        if(list.isEmpty())
            list.remove(resourceName);
        return accessor;
    }

    final TAccessor removeFeature(final String resourceName,
                                           final M metadata){
        return writeLock.apply(listGroup, resourceName, metadata, this::removeFeatureImpl);
    }

    @ThreadSafe(false)
    final TAccessor getAccessor(final String resourceName, final String featureName) {
        final L f = features.get(resourceName);
        return f != null ? f.get(featureName) : null;
    }

    final <E extends Throwable> boolean processFeature(final String resourceName,
                                                                final String featureName,
                                                                final Acceptor<? super TAccessor, E> processor) throws E {
        try (final SafeCloseable ignored = readLock.acquireLock(listGroup)) {
            final TAccessor accessor = getAccessor(resourceName, featureName);
            if (accessor != null) {
                processor.accept(accessor);
                return true;
            } else
                return false;
        }
    }

    final <E extends Throwable> boolean processFeature(final String resourceName,
                                                       final Predicate<? super TAccessor> filter,
                                                       final Acceptor<? super TAccessor, E> processor) throws E {
        try (final SafeCloseable ignored = readLock.acquireLock(listGroup)) {
            final L f = features.get(resourceName);
            final Optional<TAccessor> accessor = f != null ? f.find(filter) : Optional.empty();
            if (accessor.isPresent()) {
                processor.accept(accessor.get());
                return true;
            } else
                return false;
        }
    }

    /**
     * Returns a read-only set of connected managed resources.
     * @return The read-only set of connected managed resources.
     */
    @ThreadSafe
    public final Set<String> getHostedResources(){
        return readLock.apply(listGroup, features, attrs -> ImmutableSet.copyOf(attrs.keySet()));
    }

    private static Set<String> getFeaturesImpl(final String resourceName,
                                                         final Map<String, ? extends KeyedObjects<String, ?>> attributes) {
        return attributes.containsKey(resourceName) ?
                attributes.get(resourceName).keySet() :
                ImmutableSet.of();
    }

    @ThreadSafe
    final Set<String> getResourceFeatures(final String resourceName) {
        return readLock.apply(listGroup, resourceName, features, ModelOfFeatures::getFeaturesImpl);
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

    @ThreadSafe
    final Collection<M> getResourceFeaturesMetadata(final String resourceName){
        return readLock.apply(listGroup, resourceName, features, ModelOfFeatures::getFeatures);
    }

    private static <TAccessor extends FeatureAccessor<?>> Collection<TAccessor> clearImpl(final String resourceName,
                                                                                  final Map<String, ? extends KeyedObjects<String, TAccessor>> features){
        return features.containsKey(resourceName) ?
                features.remove(resourceName).values():
                ImmutableList.of();
    }

    /**
     * Removes all attributes from this model and associated with the specified resource.
     * @param resourceName The name of the managed resource.
     * @return The read-only collection of removed attributes.
     */
    @ThreadSafe
    public Collection<TAccessor> clear(final String resourceName) {
        return writeLock.apply(listGroup, resourceName, features, ModelOfFeatures::clearImpl);
    }

    private <E extends Exception> void forEachFeatureImpl(final EntryReader<String, ? super TAccessor, E> featureReader) throws E {
        for (final Map.Entry<String, L> entry : features.entrySet())
            for (final TAccessor accessor : entry.getValue().values())
                if (!featureReader.read(entry.getKey(), accessor)) return;
    }

    final <E extends Exception> void forEachFeature(final EntryReader<String, ? super TAccessor, E> featureReader) throws E {
        readLock.accept(listGroup, featureReader, this::forEachFeatureImpl);
    }

    private static void clearImpl(final Map<String, ? extends ResourceFeatureList<?, ?>> attributes){
        attributes.values().forEach(ResourceFeatureList::clear);
        attributes.clear();
    }

    /**
     * Removes all attributes from this model.
     */
    @ThreadSafe
    public void clear(){
        writeLock.accept(SingleResourceGroup.INSTANCE, features, ModelOfFeatures::clearImpl);
    }

    protected abstract TAccessor createAccessor(final String resourceName, final M metadata) throws Exception;
}
