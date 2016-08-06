package com.bytex.snamp.connectors;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.WeakEventListener;
import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.connectors.metrics.Metrics;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.management.MBeanFeatureInfo;
import java.math.BigInteger;
import java.util.*;

/**
 * Represents repository of resource features such as attributes, notification, operations and etc.
 * @param <F> Type of the features managed by repository.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public abstract class AbstractFeatureRepository<F extends MBeanFeatureInfo> extends ThreadSafeObject implements Iterable<F> {
    private static final class WeakResourceEventListener extends WeakEventListener<ResourceEventListener, ResourceEvent> implements ResourceEventListener{
        private WeakResourceEventListener(final ResourceEventListener listener) {
            super(listener);
        }

        @Override
        protected void invoke(final ResourceEventListener listener, final ResourceEvent event) {
            listener.handle(event);
        }

        @Override
        public void handle(final ResourceEvent event) {
            invoke(event);
        }
    }

    private static final class ResourceEventListenerList extends WeakEventListenerList<ResourceEventListener, ResourceEvent> {
        @Override
        protected WeakResourceEventListener createWeakEventListener(final ResourceEventListener listener) {
            return new WeakResourceEventListener(listener);
        }
    }

    /**
     * Represents a container for metadata element and its identity.
     * @param <F> Type of the managed resource feature.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
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
     * Metadata of the resource feature stored in repository.
     */
    final Class<F> metadataType;
    private final ResourceEventListenerList resourceEventListeners;
    private final String resourceName;

    protected <G extends Enum<G>> AbstractFeatureRepository(final String resourceName,
                                                            final Class<F> metadataType,
                                                            final Class<G> resourceGroupDef) {
        super(resourceGroupDef);
        this.metadataType = Objects.requireNonNull(metadataType);
        this.resourceEventListeners = new ResourceEventListenerList();
        this.resourceName = resourceName;
    }

    protected AbstractFeatureRepository(final String resourceName,
                                        final Class<F> metadataType){
        this(resourceName, metadataType, SingleResourceGroup.class);
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
     * Adds a new repository event listener.
     *
     * @param listener Repository event listener to add.
     */
    public final void addModelEventListener(final ResourceEventListener listener) {
        resourceEventListeners.add(listener);
    }

    /**
     * Removes the specified repository event listener.
     *
     * @param listener The listener to remove.
     * @return {@literal true}, if listener is removed successfully; otherwise, {@literal false}.
     */
    public final boolean removeModelEventListener(final ResourceEventListener listener) {
        return resourceEventListeners.remove(listener);
    }

    protected final void fireResourceEvent(final FeatureModifiedEvent<?> event) {
        resourceEventListeners.fire(event);
    }

    protected final F[] toArray(final Collection<? extends FeatureHolder<F>> features) {
        return features.stream().map(FeatureHolder::getMetadata).toArray(ArrayUtils.arrayConstructor(metadataType));
    }

    protected final void removeAllResourceEventListeners() {
        resourceEventListeners.clear();
    }

    /**
     * Gets feature by its ID.
     * @param featureID ID of the feature.
     * @return Feature instance; or {@literal null}, if feature with the specified ID doesn't exist.
     */
    @ThreadSafe
    public abstract F get(final String featureID);

    /**
     * Gets the size of this repository.
     * @return The size of this repository.
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
    public Collection<F> retainAll(final Set<String> featureIDs) {
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
     * Gets metrics associated with activity of the features in this repository.
     * @return Metrics associated with activity in this repository.
     */
    public abstract Metrics getMetrics();

    /**
     * Expands this repository.
     * @return A list of expanded features; or empty list if this repository doesn't support expansion.
     * @see ManagedResourceConnector#expand(Class)
     */
    public Collection<F> expand(){
        return Collections.emptyList();
    }

    protected static  <F extends MBeanFeatureInfo> Iterator<F> iterator(final Collection<? extends FeatureHolder<F>> holders){
        return holders.stream().map(FeatureHolder::getMetadata).iterator();
    }
}
