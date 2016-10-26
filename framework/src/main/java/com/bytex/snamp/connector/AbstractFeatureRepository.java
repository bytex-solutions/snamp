package com.bytex.snamp.connector;

import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.WeakEventListener;
import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.connector.metrics.Metric;
import com.google.common.collect.Sets;

import javax.management.MBeanFeatureInfo;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.arrayConstructor;

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
    protected final Collection<F> retainAll(final Set<String> featureIDs) {
        return Sets.difference(getIDs(), featureIDs).stream()
                .map(this::remove)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public abstract void forEach(final Consumer<? super F> action);

    /**
     * Gets metrics associated with activity of the features in this repository.
     * @return Metrics associated with activity in this repository.
     */
    public abstract Metric getMetrics();

    protected final F[] toArray(final Collection<F> features){
        return features.stream().toArray(arrayConstructor(metadataType));
    }
}
