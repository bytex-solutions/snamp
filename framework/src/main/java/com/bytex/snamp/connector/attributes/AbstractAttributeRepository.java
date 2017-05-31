package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.AbstractFeatureRepository;
import com.bytex.snamp.connector.metrics.AttributeMetrics;
import com.bytex.snamp.connector.metrics.AttributeMetricsRecorder;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import javax.annotation.Nonnull;
import javax.management.*;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.callAndWrapException;
import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Provides a base support of management attributes.
 * @param <M> Type of the attribute metadata.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public abstract class AbstractAttributeRepository<M extends MBeanAttributeInfo> extends AbstractFeatureRepository<M> implements AttributeSupport {
    private static abstract class AttributeTask<M extends MBeanAttributeInfo> extends WeakReference<AbstractAttributeRepository<M>> implements Callable<Attribute>{
        private AttributeTask(final AbstractAttributeRepository<M> repository) {
            super(repository);
        }

        abstract Attribute call(final AbstractAttributeRepository<M> repository) throws Exception;

        @Override
        public final Attribute call() throws Exception {
            final AbstractAttributeRepository<M> repository = get();
            if (repository != null) {
                clear();
                return call(repository);
            } else
                throw new IllegalStateException("Repository is no longer available");
        }
    }

    private static final class ReadAttributeTask<M extends MBeanAttributeInfo> extends AttributeTask<M> {
        private final String attributeName;
        private final M metadata;

        private ReadAttributeTask(final String attributeName,
                                  final M metadata,
                                  final AbstractAttributeRepository<M> repository) {
            super(repository);
            this.attributeName = attributeName;
            this.metadata = Objects.requireNonNull(metadata);
        }

        @Override
        Attribute call(final AbstractAttributeRepository<M> repository) throws Exception {
            return new Attribute(attributeName, repository.getAttribute(metadata));
        }
    }

    private static final class WriteAttributeTask<M extends MBeanAttributeInfo> extends AttributeTask<M> {
        private final Attribute attribute;
        private final M metadata;

        private WriteAttributeTask(final M metadata,
                                   final Attribute attribute,
                                   final AbstractAttributeRepository<M> repository) {
            super(repository);
            this.metadata = Objects.requireNonNull(metadata);
            this.attribute = Objects.requireNonNull(attribute);
        }

        @Override
        Attribute call(final AbstractAttributeRepository<M> repository) throws Exception {
            repository.setAttribute(metadata, attribute.getValue());
            return attribute;
        }
    }

    private final KeyedObjects<String, M> attributes;
    private final AttributeMetricsRecorder metrics;

    /**
     * Initializes a new support of management attributes.
     *
     * @param resourceName          The name of the managed resource.
     * @param attributeMetadataType The type of the attribute metadata.
     */
    protected AbstractAttributeRepository(final String resourceName,
                                          final Class<M> attributeMetadataType) {
        super(resourceName, attributeMetadataType);
        attributes = AbstractKeyedObjects.create(MBeanAttributeInfo::getName);
        metrics = new AttributeMetricsRecorder();
    }

    //this method should be called AFTER registering attribute in this manager
    private void attributeAdded(final M metadata) {
        fireResourceEvent(AttributeModifiedEvent.attributedAdded(this, getResourceName(), metadata));
    }

    //this method should be called before removing attribute from this manager
    private void attributeRemoved(final M metadata) {
        fireResourceEvent(AttributeModifiedEvent.attributedRemoving(this, getResourceName(), metadata));
    }

    /**
     * Returns a count of connected managementAttributes.
     *
     * @return The count of connected managementAttributes.
     */
    @Override
    public final int size() {
        return readLock.supplyInt(SingleResourceGroup.INSTANCE, attributes::size);
    }

    /**
     * Gets an array of connected attributes.
     *
     * @return An array of connected attributes.
     */
    @Override
    public final M[] getAttributeInfo() {
        return readLock.apply(SingleResourceGroup.INSTANCE, attributes.values(), this::toArray);
    }

    /**
     * Gets attribute metadata.
     *
     * @param attributeName The name of the attribute.
     * @return The attribute metadata; or {@literal null}, if attribute doesn't exist.
     */
    @Override
    public final Optional<M> getAttributeInfo(final String attributeName) {
        return Optional.ofNullable(readLock.apply(SingleResourceGroup.INSTANCE, attributes, attributeName, Map::get));
    }

    private static AttributeList toAttributeList(final Collection<Future<Attribute>> completedTasks) throws MBeanException {
        final AttributeList result = new AttributeList(completedTasks.size());
        for (final Future<Attribute> task : completedTasks)
            if (task.isDone())
                result.add(callAndWrapException(task::get, MBeanException::new));
        return result;
    }

    private static AttributeList toAttributeList(final Attribute... attributes){
        final AttributeList result = new AttributeList(attributes.length);
        Collections.addAll(result, attributes);
        return result;
    }

    protected final AttributeList getAttributesParallel(final ExecutorService executor, final Duration timeout) throws MBeanException {
        final Collection<Future<Attribute>> completedTasks;
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE, timeout)) {
            switch (attributes.size()) {
                case 0:
                    return new AttributeList();
                case 1:
                    final Map.Entry<String, M> attribute = Iterables.getFirst(attributes.entrySet(), null);
                    assert attribute != null;
                    return toAttributeList(new Attribute(attribute.getKey(), getAttribute(attribute.getValue())));
                default:
                    final Collection<ReadAttributeTask<M>> tasks = new LinkedList<>();
                    attributes.forEach((name, metadata) -> tasks.add(new ReadAttributeTask<>(name, metadata, this)));
                    completedTasks = timeout == null ?
                            executor.invokeAll(tasks) :
                            executor.invokeAll(tasks, timeout.toMillis(), TimeUnit.MILLISECONDS);
                    tasks.clear();
            }
        } catch (final Exception e) {
            throw new MBeanException(e);
        } finally {
            metrics.updateReads();
        }
        return toAttributeList(completedTasks);
    }

    @Override
    public AttributeList getAttributes() throws MBeanException, ReflectionException {
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE, null)) {
            if(attributes.isEmpty())
                return new AttributeList();
            final AttributeList result = new AttributeList();
            final EntryReader<String, M, Exception> walker = (name, metadata) -> {
                result.add(new Attribute(name, getAttribute(metadata)));
                return true;
            };
            walker.walk(this.attributes);
            return result;
        } catch (final MBeanException | ReflectionException e) {
            throw e;
        } catch (final Exception e) {
            throw new ReflectionException(e);
        } finally {
            metrics.updateReads();
        }
    }

    /**
     * Gets a set of attributes in parallel manner.
     *
     * @param executor   The executor used to schedule attribute reader. Cannot be {@literal null}.
     * @param attributes A set of attributes to read. Cannot be {@literal null}.
     * @param timeout    Synchronization timeout. May be {@literal null}.
     * @return A list of obtained attributes.
     * @throws MBeanException Unable to read one or more attributes.
     */
    protected final AttributeList getAttributesParallel(final ExecutorService executor,
                                                        final String[] attributes,
                                                        final Duration timeout) throws MBeanException {
        if(ArrayUtils.isNullOrEmpty(attributes))
            return new AttributeList();
        final List<Future<Attribute>> completedTasks;
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE)) {
            switch (this.attributes.size()) {
                case 1:
                    final Map.Entry<String, M> attribute = Iterables.getFirst(this.attributes.entrySet(), null);
                    assert attribute != null;
                    for (final String attributeName : attributes)
                        if (attribute.getKey().equals(attributeName))
                            return toAttributeList(new Attribute(attributeName, getAttribute(attribute.getValue())));
                case 0:
                    return new AttributeList();
                default:
                    final Collection<ReadAttributeTask<M>> tasks = new LinkedList<>();
                    for (final String attributeName : attributes) {
                        final M metadata = this.attributes.get(attributeName);
                        if (metadata != null)
                            tasks.add(new ReadAttributeTask<>(attributeName, metadata, this));
                    }
                    completedTasks = timeout == null ?
                            executor.invokeAll(tasks) :
                            executor.invokeAll(tasks, timeout.toMillis(), TimeUnit.MILLISECONDS);
                    tasks.clear();
            }
        } catch (final Exception e) {
            throw new MBeanException(e);
        } finally {
            metrics.updateReads();
        }
        return toAttributeList(completedTasks);
    }

    /**
     * Get the values of several attributes of the managed resource.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #getAttributesParallel(ExecutorService, String[], Duration)
     */
    @Override
    public AttributeList getAttributes(final String[] attributes) {
        if(ArrayUtils.isNullOrEmpty(attributes))
            return new AttributeList();
        final AttributeList result = new AttributeList(attributes.length);
        for (final String attributeName : attributes)
            try {
                result.add(new Attribute(attributeName, getAttribute(attributeName)));
            } catch (final Exception ignored) {
                //already logged by getAttribute
            }
        return result;
    }

    /**
     * Sets the values of several attributes of the managed resource in sequential manner.
     *
     * @param executor   The executor used to schedule attribute writer. Cannot be {@literal null}.
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @param timeout    Synchronization timeout. May be {@literal null}.
     * @return The list of attributes that were set, with their new values.
     * @throws MBeanException Unable to set attributes
     */
    protected final AttributeList setAttributesParallel(final ExecutorService executor,
                                                        final AttributeList attributes,
                                                        final Duration timeout) throws MBeanException {
        if (attributes.isEmpty())
            return attributes;
        final Collection<Future<Attribute>> completedTasks;
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE, timeout)) {
            switch (attributes.size()) {
                case 1:
                    final Map.Entry<String, M> attribute = Iterables.getFirst(this.attributes.entrySet(), null);
                    assert attribute != null;
                    for (final Attribute newAttribute : attributes.asList())
                        if (attribute.getKey().equals(newAttribute.getName())) {
                            setAttribute(attribute.getValue(), newAttribute.getValue());
                            return toAttributeList(newAttribute);
                        }
                case 0:
                    return new AttributeList();
                default:
                    final Collection<WriteAttributeTask<M>> tasks = new LinkedList<>();
                    for (final Attribute a : attributes.asList()) {
                        final M metadata = this.attributes.get(a.getName());
                        if (metadata != null)
                            tasks.add(new WriteAttributeTask<>(metadata, a, this));
                    }
                    completedTasks = timeout == null ?
                            executor.invokeAll(tasks) :
                            executor.invokeAll(tasks, timeout.toMillis(), TimeUnit.MILLISECONDS);
                    tasks.clear();
            }

        } catch (final Exception e) {
            throw new MBeanException(e);
        } finally {
            metrics.updateWrites();
        }
        return toAttributeList(completedTasks);
    }

    /**
     * Sets the values of several attributes of the managed resource.
     *
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     * @see #setAttributesParallel(ExecutorService, AttributeList, Duration)
     */
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        final AttributeList result = new AttributeList(attributes.size());
        for (final Attribute attr : attributes.asList()) {
            try {
                setAttribute(attr);
            } catch (final Exception ignored) {
                //already logged by setAttribute
                continue;
            }
            result.add(attr);
        }
        return result;
    }

    /**
     * Connects to the specified attribute.
     *
     * @param attributeName The name of the attribute.
     * @param descriptor  Attribute descriptor.
     * @return The description of the attribute; or {@literal null},
     * @throws Exception Internal connector error.
     */
    protected abstract M connectAttribute(final String attributeName,
                                          final AttributeDescriptor descriptor) throws Exception;

    private static boolean equals(final MBeanAttributeInfo metadata,
                                  final String attributeName,
                                  final Descriptor descriptor){
        return attributeName.equals(metadata.getName()) && descriptor.equals(metadata.getDescriptor());
    }

    private M connectAndAdd(final String attributeName,
                            final AttributeDescriptor descriptor) throws Exception{
        final M result = connectAttribute(attributeName, descriptor);
        if (result != null) {
            attributes.put(result);
            attributeAdded(result);
        }
        return result;
    }

    private M addAttributeImpl(final String attributeName,
                               final AttributeDescriptor descriptor) throws Exception {
        M holder = attributes.get(attributeName);
        //if attribute exists then we should check whether the input arguments
        //are equal to the existing attribute options
        if (holder != null) {
            if (equals(holder, attributeName, descriptor))
                return holder;
            else {
                //remove attribute
                attributeRemoved(holder);
                holder = attributes.remove(attributeName);
                disconnectAttribute(holder);
                //...and register again
                holder = connectAndAdd(attributeName, descriptor);
            }
        }
        //this is a new attribute, just connect it
        else {
            holder = connectAndAdd(attributeName, descriptor);
            if (holder == null) throw JMExceptionUtils.attributeNotFound(attributeName);
        }
        return holder;
    }

    /**
     * Registers a new attribute in this manager.
     *
     * @param attributeName    The name of the attribute.
     * @param descriptor Descriptor for created attribute.
     * @return Metadata of created attribute.
     */
    @Override
    public final Optional<M> addAttribute(final String attributeName,
                                final AttributeDescriptor descriptor) {
        M result;
        try {
            result = writeLock.call(SingleResourceGroup.INSTANCE, () -> addAttributeImpl(attributeName, descriptor), null);
        } catch (final Exception e) {
            getLogger().log(Level.SEVERE, String.format("Failed to connect attribute '%s'", attributeName), e);
            result = null;
        }
        return Optional.ofNullable(result);
    }

    /**
     * Obtains the value of a specific attribute of the managed resource.
     *
     * @param metadata The metadata of the attribute.
     * @return The value of the attribute retrieved.
     * @throws Exception Internal connector error.
     */
    protected abstract Object getAttribute(final M metadata) throws Exception;

    private Object getAttributeImpl(final String attributeName) throws Exception {
        if (attributes.containsKey(attributeName))
            return getAttribute(attributes.get(attributeName));
        else
            throw JMExceptionUtils.attributeNotFound(attributeName);
    }

    /**
     * Obtains the value of a specific attribute of the managed resource.
     *
     * @param attributeName The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.MBeanException             Wraps a {@link Exception} thrown by the MBean's getter.
     * @throws javax.management.ReflectionException        Wraps any exception associated with Java Reflection.
     * @see #setAttribute
     */
    @Override
    public final Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        try(final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE, null)) {
            return getAttributeImpl(attributeName);
        } catch (final AttributeNotFoundException e) {
            throw e;
        } catch (final MBeanException | ReflectionException e) {
            failedToGetAttribute(attributeName, e);
            throw e;
        } catch (final Exception e) {
            failedToGetAttribute(attributeName, e);
            throw new MBeanException(e);
        } finally {
            metrics.updateReads();
        }
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    private void failedToGetAttribute(final String attributeName,
                                                 final Exception e){
        getLogger().log(Level.SEVERE, String.format("Failed to get attribute '%s'", attributeName), e);
    }

    /**
     * Sets the value of a specific attribute of the managed resource.
     *
     * @param attribute The attribute of to set.
     * @param value     The value of the attribute.
     * @throws Exception                      Internal connector error.
     * @throws InvalidAttributeValueException Incompatible attribute type.
     */
    protected abstract void setAttribute(final M attribute,
                                         final Object value) throws Exception;

    private void setAttributeImpl(final Attribute attribute) throws Exception{
        if (attributes.containsKey(attribute.getName()))
            setAttribute(attributes.get(attribute.getName()), attribute.getValue());
        else throw JMExceptionUtils.attributeNotFound(attribute.getName());
    }

    /**
     * Set the value of a specific attribute of the managed resource.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws AttributeNotFoundException
     * @throws InvalidAttributeValueException
     * @throws MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public final void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        try {
            readLock.accept(SingleResourceGroup.INSTANCE, attribute, this::setAttributeImpl, (Duration) null);
        } catch (final AttributeNotFoundException e) {
            throw e;
        } catch (final InvalidAttributeValueException | MBeanException | ReflectionException e) {
            failedToSetAttribute(attribute.getName(), attribute.getValue(), e);
            throw e;
        } catch (final Exception e) {
            failedToSetAttribute(attribute.getName(), attribute.getValue(), e);
            throw new MBeanException(e);
        } finally {
            metrics.updateWrites();
        }
    }

    private void failedToSetAttribute(final String attributeName, final Object attributeValue, final Exception e){
        getLogger().log(Level.SEVERE, String.format("Failed to update attribute %s with %s value", attributeName, attributeValue), e);
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeInfo An attribute metadata.
     */
    protected void disconnectAttribute(final M attributeInfo) {
        if (attributeInfo instanceof AutoCloseable) {
            final AutoCloseable closeable = (AutoCloseable) attributeInfo;
            callUnchecked(() -> {
                closeable.close();
                return null;
            });
        }
    }

    private M removeImpl(final String attributeID) {
        final M holder = attributes.get(attributeID);
        if (holder != null)
            attributeRemoved(holder);
        return attributes.remove(attributeID);
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeID The unique identifier of the attribute.
     * @return The metadata of deleted attribute.
     */
    @Override
    public final Optional<M> remove(final String attributeID) {
        final M metadata = writeLock.apply(SingleResourceGroup.INSTANCE, this, attributeID, AbstractAttributeRepository<M>::removeImpl);
        if (metadata == null)
            return Optional.empty();
        else {
            disconnectAttribute(metadata);
            return Optional.of(metadata);
        }
    }

    /**
     * Removes attribute from the managed resource.
     *
     * @param attributeName Name of the attribute to remove.
     * @return An instance of removed attribute; or {@link Optional#empty()}, if attribute with the specified name doesn't exist.
     * @since 2.0
     */
    @Override
    public final Optional<M> removeAttribute(final String attributeName) {
        return remove(attributeName);
    }

    /**
     * Removes all attributes except specified in the collection.
     *
     * @param attributes A set of attributes which should not be deleted.
     * @since 2.0
     */
    @Override
    public final void retainAttributes(final Set<String> attributes) {
        retainAll(attributes);
    }

    private void clearImpl(){
        attributes.values().forEach(metadata -> {
            attributeRemoved(metadata);
            disconnectAttribute(metadata);
        });
        attributes.clear();
    }

    /**
     * Removes all features from this repository.
     */
    @Override
    public final void clear() {
        writeLock.run(SingleResourceGroup.INSTANCE, this::clearImpl);
    }

    /**
     * Gets a set of identifiers.
     *
     * @return A set of identifiers.
     */
    @Override
    public final ImmutableSet<String> getIDs() {
        return readLock.apply(SingleResourceGroup.INSTANCE, attributes, attrs -> ImmutableSet.copyOf(attrs.keySet()));
    }

    /**
     * Gets metrics associated with activity of the features in this repository.
     *
     * @return Metrics associated with activity in this repository.
     */
    @Override
    public final AttributeMetrics getMetrics() {
        return metrics;
    }

    @Override
    public final Optional<M> get(final String attributeID) {
        return getAttributeInfo(attributeID);
    }

    @Override
    @Nonnull
    public final Iterator<M> iterator() {
        return readLock.apply(SingleResourceGroup.INSTANCE, attributes, attributes -> ImmutableList.copyOf(attributes.values()).iterator());
    }

    @Override
    public final void forEach(final Consumer<? super M> action) {
        readLock.accept(SingleResourceGroup.INSTANCE, attributes, action, (attributes, act) -> attributes.values().forEach(act));
    }

    protected final void failedToExpand(final Level level, final Exception e){
        getLogger().log(level, String.format("Unable to expand attributes for resource %s", getResourceName()), e);
    }

    protected final AttributeDescriptor createDescriptor(Consumer<AttributeConfiguration> initializer) {
        final Consumer<AttributeConfiguration> autoAdjust = config -> config.setReadWriteTimeout(AttributeConfiguration.TIMEOUT_FOR_SMART_MODE);
        initializer = autoAdjust.andThen(initializer);
        return createDescriptor(AttributeConfiguration.class, initializer, AttributeDescriptor::new);
    }

    protected final AttributeDescriptor createDescriptor() {
        return createDescriptor(config -> {
        });
    }
}
