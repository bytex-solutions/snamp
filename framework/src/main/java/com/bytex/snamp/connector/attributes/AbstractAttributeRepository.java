package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.connector.AbstractFeatureRepository;
import com.bytex.snamp.connector.metrics.AttributeMetrics;
import com.bytex.snamp.connector.metrics.AttributeMetricsWriter;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
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
    private static final class AttributeHolder<M extends MBeanAttributeInfo> extends FeatureHolder<M> {
        private AttributeHolder(final M metadata,
                                final String attributeName,
                                final Duration readWriteTimeout,
                                final CompositeData options) {
            super(metadata, computeIdentity(attributeName, readWriteTimeout, options));
        }

        private boolean equals(final String attributeName,
                               final Duration readWriteTimeout,
                               final CompositeData options) {
            return identity.equals(computeIdentity(attributeName, readWriteTimeout, options));
        }

        private static BigInteger computeIdentity(final String attributeName,
                                                  final Duration readWriteTimeout,
                                                  final CompositeData options) {
            BigInteger result = toBigInteger(attributeName);
            if (readWriteTimeout != null)
                result = result.xor(BigInteger.valueOf(readWriteTimeout.toNanos()));
            for (final String propertyName : options.getCompositeType().keySet())
                result = result
                        .xor(toBigInteger(propertyName))
                        .xor(BigInteger.valueOf(options.get(propertyName).hashCode()));
            return result;
        }
    }

    private final KeyedObjects<String, AttributeHolder<M>> attributes;
    private final AttributeMetricsWriter metrics;

    /**
     * Initializes a new support of management attributes.
     *
     * @param resourceName          The name of the managed resource.
     * @param attributeMetadataType The type of the attribute metadata.
     */
    protected AbstractAttributeRepository(final String resourceName,
                                          final Class<M> attributeMetadataType) {
        super(resourceName, attributeMetadataType);
        attributes = AbstractKeyedObjects.create(holder -> holder.getMetadata().getName());
        metrics = new AttributeMetricsWriter();
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
        return readSupply(() -> toArray(attributes.values()));
    }

    /**
     * Gets attribute metadata.
     *
     * @param attributeName The name of the attribute.
     * @return The attribute metadata; or {@literal null}, if attribute doesn't exist.
     */
    @Override
    public final M getAttributeInfo(final String attributeName) {
        final AttributeHolder<M> holder = readApply(attributes, attributeName, Map::get);
        return holder != null ? holder.getMetadata() : null;
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

    private AttributeHolder<M> addAttributeImpl(final String attributeName,
                            final Duration readWriteTimeout,
                            final CompositeData options) throws Exception {
        AttributeHolder<M> holder = attributes.get(attributeName);
        //if attribute exists then we should check whether the input arguments
        //are equal to the existing attribute options
        if (holder != null) {
            if (holder.equals(attributeName, readWriteTimeout, options))
                return holder;
            else {
                //remove attribute
                attributeRemoved(holder.getMetadata());
                holder = attributes.remove(attributeName);
                //...and register again
                disconnectAttribute(holder.getMetadata());
                final M metadata = connectAttribute(attributeName, new AttributeDescriptor(readWriteTimeout, options));
                if (metadata != null) {
                    attributes.put(holder = new AttributeHolder<>(metadata, attributeName, readWriteTimeout, options));
                    attributeAdded(holder.getMetadata());
                }
            }
        }
        //this is a new attribute, just connect it
        else {
            final M metadata = connectAttribute(attributeName, new AttributeDescriptor(readWriteTimeout, options));
            if (metadata != null) {
                attributes.put(holder = new AttributeHolder<>(metadata, attributeName, readWriteTimeout, options));
                attributeAdded(holder.getMetadata());
            } else throw JMExceptionUtils.attributeNotFound(attributeName);
        }
        return holder;
    }

    /**
     * Registers a new attribute in this manager.
     *
     * @param attributeName    The name of the attribute.
     * @param readWriteTimeout A read/write timeout using for attribute read/write operation.
     * @param options          The attribute discovery options.
     * @return The description of the attribute.
     */
    public final M addAttribute(final String attributeName,
                                final Duration readWriteTimeout,
                                final CompositeData options) {
        AttributeHolder<M> holder;
        try {
            holder = writeCallInterruptibly(() -> addAttributeImpl(attributeName, readWriteTimeout, options));
        } catch (final Exception e) {
            failedToConnectAttribute(attributeName, e);
            holder = null;
        }
        return holder != null ? holder.getMetadata() : null;
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
            return getAttribute(attributes.get(attributeName).getMetadata());
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
            setAttribute(attributes.get(attribute.getName()).getMetadata(), attribute.getValue());
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

    private AttributeHolder<M> removeImpl(final String attributeID) {
        AttributeHolder<M> holder = attributes.get(attributeID);
        if (holder != null) {
            attributeRemoved(holder.getMetadata());
            holder = attributes.remove(attributeID);
        }
        return holder;
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeID The unique identifier of the attribute.
     * @return The metadata of deleted attribute.
     */
    @Override
    public final M remove(final String attributeID) {
        final AttributeHolder<M> holder = writeApply(this, attributeID, AbstractAttributeRepository::removeImpl);
        if (holder != null) {
            disconnectAttribute(holder.getMetadata());
            return holder.getMetadata();
        } else return null;
    }

    private void removeAllImpl(final KeyedObjects<String, AttributeHolder<M>> attributes){
        for (final AttributeHolder<M> holder : attributes.values()) {
            attributeRemoved(holder.getMetadata());
            disconnectAttribute(holder.getMetadata());
        }
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
        return iterator(attributes.values());
    }

    /**
     * Composes a set of scalar attributes into a single container.
     * @param typeName The name of the composite type. Cannot be {@literal null} or empty.
     * @param typeDescription The description of the composite type. Cannot be {@literal null} or empty.
     * @param selector Filter for attribute types. Cannot be {@literal null}.
     * @param attributes A set of attributes to compose.
     * @return Composite type with scalar attributes.
     */
    public static CompositeType compose(final String typeName,
                                        final String typeDescription,
                                        final Predicate<? super OpenType<?>> selector,
                                        final MBeanAttributeInfo... attributes) throws OpenDataException {
        final CompositeTypeBuilder result = new CompositeTypeBuilder(typeName, typeDescription);
        for (final MBeanAttributeInfo attributeInfo : attributes) {
            final OpenType<?> attributeType = AttributeDescriptor.getOpenType(attributeInfo);
            if (selector.test(attributeType))
                result.addItem(attributeInfo.getName(), attributeInfo.getDescription(), attributeType);
        }
        return result.build();
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
