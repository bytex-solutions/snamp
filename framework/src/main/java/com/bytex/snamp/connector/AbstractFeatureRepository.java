package com.bytex.snamp.connector;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.connector.metrics.Metric;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.MBeanFeatureInfo;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.arrayConstructor;

/**
 * Represents repository of resource features such as attributes, notification, operations and etc.
 * @param <F> Type of the features managed by repository.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 * @deprecated Replaced by {@link FeatureRepository}.
 */
@ThreadSafe
@Deprecated
public abstract class AbstractFeatureRepository<F extends MBeanFeatureInfo> implements Iterable<F>, SafeCloseable {
    /**
     * Metadata of the resource feature stored in repository.
     */
    private final Class<F> metadataType;
    private final WeakEventListenerList<ResourceEventListener, ResourceEvent> resourceEventListeners;
    private final String resourceName;
    private ManagedResourceConnector notificationSource;

    protected AbstractFeatureRepository(final String resourceName,
                                                            @Nonnull final Class<F> metadataType) {
        this.metadataType = Objects.requireNonNull(metadataType);
        this.resourceEventListeners = new WeakEventListenerList<>(ResourceEventListener::resourceModified);
        this.resourceName = resourceName;
    }

    protected AbstractFeatureRepository(@Nonnull final AbstractManagedResourceConnector source,
                                        @Nonnull final Class<F> metadataType){
        this(source.resourceName, metadataType);
        notificationSource = source;
    }

    /**
     * Gets source for all outbound notifications emitted by this repository.
     * @return Source of all outbound notifications.
     * @throws IllegalStateException Owner is not defined. Please call {@link #setSource(ManagedResourceConnector)}.
     * @since 2.1
     */
    @Nonnull
    protected final ManagedResourceConnector getSource() {
        if (notificationSource == null)
            throw new IllegalStateException("Source is not defined for this repository. Please call setSource.");
        else
            return notificationSource;
    }

    /**
     * Defines source for all outbound notifications emitted by this repository.
     * @param value A source for all notifications. Cannot be {@literal null}.
     * @since 2.1
     */
    public final void setSource(@Nonnull final ManagedResourceConnector value) {
        notificationSource = value;
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

    protected final void fireResourceEvent(final FeatureModifiedEvent event) {
        resourceEventListeners.fire(event);
    }

    /**
     * Notifies about new feature in this repository.
     * @param feature Added feature.
     * @since 2.1
     */
    protected final void featureAdded(@Nonnull final F feature){
        fireResourceEvent(new FeatureModifiedEvent(getSource(), resourceName, feature, FeatureModifiedEvent.Modifier.ADDED));
    }

    /**
     * Notifies before feature being removed from this repository.
     * @param feature Feature to be removed.
     * @since 2.1
     */
    protected final void removingFeature(@Nonnull final F feature){
        fireResourceEvent(new FeatureModifiedEvent(getSource(), resourceName, feature, FeatureModifiedEvent.Modifier.REMOVING));
    }

    /**
     * Gets feature by its ID.
     * @param featureID ID of the feature.
     * @return Feature instance; or {@link Optional#empty()}, if feature with the specified ID doesn't exist.
     */
    public abstract Optional<F> get(final String featureID);

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
    public abstract Optional<F> remove(final String featureID);

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
                .filter(Optional::isPresent)
                .map(Optional::get)
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

    /**
     * Removes all features from this repository.
     * @since 2.0
     */
    public abstract void clear();

    /**
     * Releases all resources associated with this repository.
     * @implSpec Always call {@code super.close()} when overriding this method in the derived class.
     * @implNote This method calls {@link #clear()}.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() {
        clear();
        resourceEventListeners.clear();
    }

    protected final <E extends FeatureConfiguration, D extends FeatureDescriptor<E>> D createDescriptor(final Class<E> entityType,
                                                                                              final Consumer<E> initializer,
                                                                                              final Function<E, D> descriptorFactory) {
        final E entity = ConfigurationManager.createEntityConfiguration(getClass().getClassLoader(), entityType);
        assert entity != null;
        initializer.accept(entity);
        return descriptorFactory.apply(entity);
    }
}
