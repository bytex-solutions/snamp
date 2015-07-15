package com.itworks.snamp.connectors;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.itworks.snamp.WeakEventListenerList;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.itworks.snamp.io.IOUtils;

import javax.management.MBeanFeatureInfo;
import java.math.BigInteger;
import java.util.*;

/**
 * Represents an abstract class for all modelers of managed resource features.
 * You cannot derive from this class directly.
 * @param <F> Type of the modeling feature.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractFeatureModeler<F extends MBeanFeatureInfo> extends ThreadSafeObject implements Iterable<F> {

    private static final class ResourceEventListenerList extends WeakEventListenerList<ResourceEventListener, ResourceEvent> {
        private static final long serialVersionUID = -9139754747382955308L;

        private ResourceEventListenerList() {

        }

        @Override
        protected void invoke(final ResourceEvent event, final ResourceEventListener listener) {
            listener.handle(event);
        }
    }

    /**
     * Represents a container for metadata element and its identity.
     * @param <F> Type of the managed resource feature.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class FeatureHolder<F extends MBeanFeatureInfo>{
        private final F metadata;
        /**
         * The identity of this feature.
         */
        protected final BigInteger identity;

        /**
         * Initializes a new container of the managed resource feature.
         * @param metadata The metadata of the managed resource feature.
         * @param identity The identity of the feature.
         */
        protected FeatureHolder(final F metadata,
                              final BigInteger identity){
            this.metadata = Objects.requireNonNull(metadata);
            this.identity = Objects.requireNonNull(identity);
        }

        public final boolean equals(final FeatureHolder<?> other){
            return other != null &&
                    Objects.equals(getClass(), other.getClass()) &&
                    Objects.equals(identity, other.identity);
        }

        /**
         * Gets metadata of the managed resource feature.
         * @return The metadata of the managed resource feature.
         */
        public final F getMetadata(){
            return metadata;
        }

        @Override
        public final boolean equals(final Object other) {
            return other instanceof FeatureHolder<?> && equals((FeatureHolder<?>)other);
        }

        protected static BigInteger toBigInteger(final String value){
            return value == null || value.isEmpty() ?
                    BigInteger.ZERO:
                    new BigInteger(value.getBytes(IOUtils.DEFAULT_CHARSET));
        }

        @Override
        public final int hashCode() {
            return identity.hashCode();
        }

        @Override
        public final String toString() {
            return metadata.toString();
        }
    }

    /**
     * Metadata of the managed resource feature.
     */
    protected final Class<F> metadataType;
    private final ResourceEventListenerList resourceEventListeners;
    private final Enum<?> resourceEventListenerSyncGroup;
    private final String resourceName;

    protected <G extends Enum<G>> AbstractFeatureModeler(final String resourceName,
                                                       final Class<F> metadataType,
                                                       final Class<G> resourceGroupDef,
                                                       final G resourceEventListenerSyncGroup) {
        super(resourceGroupDef);
        this.metadataType = Objects.requireNonNull(metadataType);
        this.resourceEventListeners = new ResourceEventListenerList();
        this.resourceEventListenerSyncGroup = Objects.requireNonNull(resourceEventListenerSyncGroup);
        this.resourceName = resourceName;
    }

    /**
     * Gets name of the resource.
     *
     * @return The name of the resource.
     */
    public final String getResourceName() {
        return resourceName;
    }

    /**
     * Adds a new feature modeler event listener.
     *
     * @param listener Feature modeler event listener to add.
     */
    public final void addModelEventListener(final ResourceEventListener listener) {
        try (final LockScope ignored = beginWrite(resourceEventListenerSyncGroup)) {
            resourceEventListeners.add(listener);
        }
    }

    /**
     * Removes the specified modeler event listener.
     *
     * @param listener The listener to remove.
     */
    public final void removeModelEventListener(final ResourceEventListener listener) {
        try (final LockScope ignored = beginWrite(resourceEventListenerSyncGroup)) {
            resourceEventListeners.remove(listener);
        }
    }

    protected final void fireResourceEvent(final FeatureModifiedEvent<?> event) {
        try (final LockScope ignored = beginWrite(resourceEventListenerSyncGroup)) {
            resourceEventListeners.fire(event);
        }
    }

    protected final F[] toArray(final Collection<? extends FeatureHolder<F>> features) {
        final F[] result = ObjectArrays.newArray(metadataType, features.size());
        int index = 0;
        for(final FeatureHolder<F> holder: features)
            result[index++] = holder.getMetadata();
        return result;
    }

    protected final void removeAllResourceEventListeners() {
        try (final LockScope ignored = beginWrite(resourceEventListenerSyncGroup)) {
            resourceEventListeners.clear();
        }
    }

    /**
     * Determines whether the specified feature is already registered in this model.
     * @param featureID ID of the registered feature.
     * @return {@literal true}, if the feature is already registered; otherwise, {@literal false}.
     */
    @ThreadSafe
    public abstract boolean isRegistered(final String featureID);

    /**
     * Gets feature by its ID.
     * @param featureID ID of the feature.
     * @return Feature instance; or {@literal null}, if feature with the specified ID doesn't exist.
     */
    @ThreadSafe
    public abstract F get(final String featureID);

    /**
     * Gets the size of this model.
     * @return The size of this model.
     */
    public abstract int size();

    /**
     * Unregisters feature by its ID.
     * @param featureID ID of the feature to remove.
     * @return Metadata of the removed feature.
     */
    public abstract F remove(final String featureID);

    /**
     * Gets a set of identifiers.
     * @return A set of identifiers.
     */
    public abstract Set<String> getIDs();

    /**
     * Removes all features which IDs are not present in the specified set.
     * @param featureIDs A set of features which cannot be deleted.
     * @return Collection of removes features.
     */
    public Collection<F> removeAllExcept(final Set<String> featureIDs) {
        final Set<String> featuresToRemove = Sets.difference(getIDs(), featureIDs);
        final List<F> result = Lists.newArrayListWithCapacity(featuresToRemove.size());
        for (final String removingFeatureID : featuresToRemove) {
            final F removedFeature = remove(removingFeatureID);
            if (removedFeature != null)
                result.add(removedFeature);
        }
        return result;
    }

    /**
     * Removes all features which IDs are not present in the specified set.
     * @param featureIDs A set of features which cannot be deleted.
     * @return Collection of removes features.
     */
    public final Collection<F> removeAllExcept(final String... featureIDs){
        return removeAllExcept(ImmutableSet.copyOf(featureIDs));
    }

    /**
     * Expands this model.
     * @return A list of expanded features; or empty list if this model doesn't support expansion.
     * @see ManagedResourceConnector#expand(Class)
     */
    public Collection<F> expand(){
        return Collections.emptyList();
    }

    protected static  <F extends MBeanFeatureInfo> Iterator<F> iterator(final Iterable<? extends FeatureHolder<F>> holders){
        return Iterators.transform(holders.iterator(), new Function<FeatureHolder<F>, F>() {
            @Override
            public F apply(final FeatureHolder<F> input) {
                return input.getMetadata();
            }
        });
    }
}
