package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.connector.AbstractFeatureRepository;
import com.bytex.snamp.connector.metrics.AttributeMetrics;
import com.bytex.snamp.connector.metrics.AttributeMetricsWriter;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import javax.management.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a base support of management attributes.
 * @param <M> Type of the attribute metadata.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public abstract class AbstractAttributeRepository<M extends MBeanAttributeInfo> extends AbstractFeatureRepository<M> implements AttributeSupport, SafeCloseable {

    private final KeyedObjects<String, M> attributes;
    private final AttributeMetricsWriter metrics;
    private final boolean expandable;

    /**
     * Initializes a new support of management attributes.
     *
     * @param resourceName          The name of the managed resource.
     * @param attributeMetadataType The type of the attribute metadata.
     * @param expandable {@literal true}, if repository can be populated automatically; otherwise, {@literal false}.
     */
    protected AbstractAttributeRepository(final String resourceName,
                                          final Class<M> attributeMetadataType,
                                          final boolean expandable) {
        super(resourceName, attributeMetadataType);
        attributes = AbstractKeyedObjects.create(MBeanAttributeInfo::getName);
        metrics = new AttributeMetricsWriter();
        this.expandable = expandable;
    }

    //this method should be called AFTER registering attribute in this manager
    private void attributeAdded(final M metadata) {
        super.fireResourceEvent(new AttributeAddedEvent(this, getResourceName(), metadata));
    }

    //this method should be called before removing attribute from this manager
    private void attributeRemoved(final M metadata) {
        super.fireResourceEvent(new AttributeRemovingEvent(this, getResourceName(), metadata));
    }

    /**
     * Returns a count of connected managementAttributes.
     *
     * @return The count of connected managementAttributes.
     */
    @ThreadSafe
    @Override
    public final int size() {
        return readSupply(attributes::size);
    }

    /**
     * Gets an array of connected attributes.
     *
     * @return An array of connected attributes.
     */
    @Override
    public final M[] getAttributeInfo() {
        return readApply(attributes.values(), this::toArray);
    }

    /**
     * Gets attribute metadata.
     *
     * @param attributeName The name of the attribute.
     * @return The attribute metadata; or {@literal null}, if attribute doesn't exist.
     */
    @Override
    public final M getAttributeInfo(final String attributeName) {
        return readApply(attributes, attributeName, Map::get);
    }

    /**
     * Gets a set of attributes in sequential manner.
     *
     * @param attributes A set of attributes to read. Cannot be {@literal null}.
     * @return output A list of obtained attributes.
     */
    protected final AttributeList getAttributesSequential(final String[] attributes) {
        final List<Attribute> result = Lists.newArrayListWithExpectedSize(attributes.length);
        for (final String attributeName : attributes)
            try {
                result.add(new Attribute(attributeName, getAttribute(attributeName)));
            } catch (final JMException e) {
                failedToGetAttribute(attributeName, e);
            }
        return new AttributeList(result);
    }

    /**
     * Gets a set of attributes in parallel manner.
     *
     * @param executor   The executor used to schedule attribute reader. Cannot be {@literal null}.
     * @param attributes A set of attributes to read. Cannot be {@literal null}.
     * @param timeout    Synchronization timeout. May be {@literal null}.
     * @return A list of obtained attributes.
     * @throws InterruptedException Operation is interrupted.
     * @throws TimeoutException     Unable to read attributes in the specified time duration.
     */
    protected final AttributeList getAttributesParallel(final ExecutorService executor,
                                                        final String[] attributes,
                                                        final Duration timeout) throws InterruptedException, TimeoutException {
        final List<Attribute> result = Collections.
                synchronizedList(Lists.<Attribute>newArrayListWithExpectedSize(attributes.length));
        final CountDownLatch synchronizer = new CountDownLatch(attributes.length);
        for (final String attributeName : attributes)
            executor.submit(() -> {
                try {
                    return result.add(new Attribute(attributeName, getAttribute(attributeName)));
                } catch (final JMException e) {
                    failedToGetAttribute(attributeName, e);
                    return null;
                } finally {
                    synchronizer.countDown();
                }
            });
        if (timeout == null)
            synchronizer.await();
        else if (!synchronizer.await(timeout.toNanos(), TimeUnit.NANOSECONDS))
            throw new TimeoutException();
        return new AttributeList(result);
    }

    /**
     * Get the values of several attributes of the managed resource.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #getAttributesSequential(String[])
     * @see #getAttributesParallel(ExecutorService, String[], Duration)
     */
    @Override
    public AttributeList getAttributes(final String[] attributes) {
        return getAttributesSequential(attributes);
    }

    /**
     * Sets the values of several attributes of the managed resource in sequential manner.
     *
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     */
    protected final AttributeList setAttributesSequential(final AttributeList attributes) {
        final List<Attribute> result = Lists.newArrayListWithExpectedSize(attributes.size());
        for (final Attribute attr : attributes.asList()) {
            try {
                setAttribute(attr);
                result.add(attr);
            } catch (final JMException e) {
                failedToSetAttribute(attr.getName(), attr.getValue(), e);
            }
        }
        return new AttributeList(result);
    }

    /**
     * Sets the values of several attributes of the managed resource in sequential manner.
     *
     * @param executor   The executor used to schedule attribute writer. Cannot be {@literal null}.
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @param timeout    Synchronization timeout. May be {@literal null}.
     * @return The list of attributes that were set, with their new values.
     * @throws InterruptedException Operation is interrupted.
     * @throws TimeoutException     Unable to set attributes in the specified time duration.
     */
    protected final AttributeList setAttributesParallel(final ExecutorService executor,
                                                        final AttributeList attributes,
                                                        final Duration timeout) throws TimeoutException, InterruptedException {
        if (attributes.isEmpty()) return attributes;
        final List<Attribute> result =
                Collections.synchronizedList(Lists.<Attribute>newArrayListWithExpectedSize(attributes.size()));
        final CountDownLatch synchronizer = new CountDownLatch(attributes.size());
        for (final Attribute attr : attributes.asList())
            executor.submit(() -> {
                try {
                    setAttribute(attr);
                    return result.add(new Attribute(attr.getName(), attr.getValue()));
                } catch (final JMException e) {
                    failedToSetAttribute(attr.getName(), attr.getValue(), e);
                    return null;
                } finally {
                    synchronizer.countDown();
                }
            });
        if (timeout == null)
            synchronizer.await();
        else if (!synchronizer.await(timeout.toNanos(), TimeUnit.NANOSECONDS))
            throw new TimeoutException();
        return new AttributeList(result);
    }

    /**
     * Sets the values of several attributes of the managed resource.
     *
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     * @see #setAttributesSequential(AttributeList)
     * @see #setAttributesParallel(ExecutorService, AttributeList, Duration)
     */
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        return setAttributesSequential(attributes);
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
    public final M addAttribute(final String attributeName,
                                final AttributeDescriptor descriptor) {
        try {
            return writeCallInterruptibly(() -> addAttributeImpl(attributeName, descriptor));
        } catch (final Exception e) {
            failedToConnectAttribute(attributeName, e);
            return null;
        }
    }

    /**
     * Reports an error when connecting attribute.
     *
     * @param logger        The logger instance. Cannot be {@literal null}.
     * @param logLevel      Logging level.
     * @param attributeName The name of the attribute.
     * @param e             Internal connector error.
     */
    protected static void failedToConnectAttribute(final Logger logger,
                                                   final Level logLevel,
                                                   final String attributeName,
                                                   final Exception e) {
        logger.log(logLevel, String.format("Failed to connect attribute '%s'", attributeName), e);
    }

    /**
     * Reports an error when connecting attribute.
     *
     * @param attributeName The name of the attribute.
     * @param e             Internal connector error.
     * @see #failedToConnectAttribute(Logger, Level, String, Exception)
     */
    protected abstract void failedToConnectAttribute(final String attributeName,
                                                     final Exception e);

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
        try(final SafeCloseable ignored = acquireReadLockInterruptibly(SingleResourceGroup.INSTANCE)) {
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

    /**
     * Reports an error when getting attribute.
     *
     * @param logger      The logger instance. Cannot be {@literal null}.
     * @param logLevel    Logging level.
     * @param attributeName The attribute identifier.
     * @param e           Internal connector error.
     */
    protected static void failedToGetAttribute(final Logger logger,
                                               final Level logLevel,
                                               final String attributeName,
                                               final Exception e) {
        logger.log(logLevel, String.format("Failed to get attribute '%s'", attributeName), e);
    }

    /**
     * Reports an error when getting attribute.
     *
     * @param attributeID The attribute identifier.
     * @param e           Internal connector error.
     * @see #failedToGetAttribute(Logger, Level, String, Exception)
     */
    protected abstract void failedToGetAttribute(final String attributeID,
                                                 final Exception e);

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
            readAcceptInterruptibly(attribute, this::setAttributeImpl);
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

    /**
     * Reports an error when updating attribute.
     *
     * @param logger      The logger instance. Cannot be {@literal null}.
     * @param logLevel    Logging level.
     * @param attributeID The attribute identifier.
     * @param value       The value of the attribute.
     * @param e           Internal connector error.
     */
    protected static void failedToSetAttribute(final Logger logger,
                                               final Level logLevel,
                                               final String attributeID,
                                               final Object value,
                                               final Exception e) {
        logger.log(logLevel, String.format("Failed to update attribute %s with %s value", attributeID, value), e);
    }

    /**
     * Reports an error when updating attribute.
     *
     * @param attributeID The attribute identifier.
     * @param value       The value of the attribute.
     * @param e           Internal connector error.
     * @see #failedToSetAttribute(Logger, Level, String, Object, Exception)
     */
    protected abstract void failedToSetAttribute(final String attributeID,
                                                 final Object value,
                                                 final Exception e);

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeInfo An attribute metadata.
     */
    protected void disconnectAttribute(final M attributeInfo) {
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
    public final M remove(final String attributeID) {
        final M metadata = writeApply(this, attributeID, AbstractAttributeRepository::removeImpl);
        if (metadata != null)
            disconnectAttribute(metadata);
        return metadata;
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

    private void removeAllImpl(final Map<String, M> attributes){
        attributes.values().forEach(metadata -> {
            attributeRemoved(metadata);
            disconnectAttribute(metadata);
        });
        attributes.clear();
    }

    /**
     * Removes all attributes.
     *
     * @param removeAttributeEventListeners {@literal true} to remove all attribute listeners; otherwise, {@literal false}.
     */
    public final void removeAll(final boolean removeAttributeEventListeners) {
        writeAccept(attributes, this::removeAllImpl);
        if (removeAttributeEventListeners)
            super.removeAllResourceEventListeners();
    }

    /**
     * Gets a set of identifiers.
     *
     * @return A set of identifiers.
     */
    @Override
    public final ImmutableSet<String> getIDs() {
        return readApply(attributes, attrs -> ImmutableSet.copyOf(attrs.keySet()));
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
    public final M get(final String attributeID) {
        return getAttributeInfo(attributeID);
    }

    @Override
    public final Iterator<M> iterator() {
        return readApply(attributes.values(), Collection::iterator);
    }

    /**
     * Populate this repository with attributes.
     *
     * @return A collection of registered attributes; or empty collection if nothing tot populate.
     */
    @Override
    public Collection<? extends M> expandAttributes() {
        return Collections.emptyList();
    }

    /**
     * Determines whether this repository can be populated with attributes using call of {@link #expandAttributes()}.
     *
     * @return {@literal true}, if this repository can be populated; otherwise, {@literal false}.
     * @since 2.0
     */
    @Override
    public final boolean canExpandAttributes() {
        return expandable;
    }

    protected final void failedToExpand(final Logger logger, final Level level, final Exception e){
        logger.log(level, String.format("Unable to expand attributes for resource %s", getResourceName()), e);
    }

    /**
     * Removes all attributes from this repository.
     */
    @Override
    public void close() {
        removeAll(true);
    }
}
