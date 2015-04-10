package com.itworks.snamp.connectors.attributes;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AbstractFeatureModeler;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.itworks.snamp.jmx.JMExceptionUtils;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a base support of management attributes.
 * @param <M> Type of the attribute metadata.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractAttributeSupport<M extends MBeanAttributeInfo> extends AbstractFeatureModeler<M> implements AttributeSupport {
    private enum AASResource{
        ATTRIBUTES,
        RESOURCE_EVENT_LISTENERS
    }

    private static final class AttributeHolder<M extends MBeanAttributeInfo> extends FeatureHolder<M>{
        private AttributeHolder(final M metadata,
                                final String attributeName,
                                final TimeSpan readWriteTimeout,
                                final CompositeData options){
            super(metadata, computeIdentity(attributeName, readWriteTimeout, options));
        }

        private boolean equals(final String attributeName,
                               final TimeSpan readWriteTimeout,
                               final CompositeData options){
            return identity.equals(computeIdentity(attributeName, readWriteTimeout, options));
        }

        private static BigInteger toBigInteger(final String value){
            return value == null || value.isEmpty() ?
                    BigInteger.ZERO:
                    new BigInteger(value.getBytes());
        }

        private static BigInteger computeIdentity(final String attributeName,
                                                  final TimeSpan readWriteTimeout,
                                                  final CompositeData options){
            BigInteger result = toBigInteger(attributeName);
            if(readWriteTimeout != null)
                result = result.xor(BigInteger.valueOf(readWriteTimeout.toNanos()));
            for(final String propertyName: options.getCompositeType().keySet())
                result = result
                        .xor(toBigInteger(propertyName))
                        .xor(BigInteger.valueOf(options.get(propertyName).hashCode()));
            return result;
        }
    }

    private final KeyedObjects<String, AttributeHolder<M>> attributes;

    /**
     * Initializes a new support of management attributes.
     * @param resourceName The name of the managed resource.
     * @param attributeMetadataType The type of the attribute metadata.
     */
    protected AbstractAttributeSupport(final String resourceName,
                                       final Class<M> attributeMetadataType) {
        super(resourceName,
                attributeMetadataType,
                AASResource.class,
                AASResource.RESOURCE_EVENT_LISTENERS);
        attributes = createAttributes();
    }

    private static <M extends MBeanAttributeInfo> AbstractKeyedObjects<String, AttributeHolder<M>> createAttributes(){
        return new AbstractKeyedObjects<String, AttributeHolder<M>>(10) {
            private static final long serialVersionUID = 6284468803876344036L;

            @Override
            public String getKey(final AttributeHolder<M> holder) {
                return holder.getMetadata().getName();
            }
        };
    }

    //this method should be called AFTER registering attribute in this manager
    private void attributeAdded(final M metadata) {
        super.fireResourceEvent(new AttributeAddedEvent(this, getResourceName(), metadata));
    }

    //this method should be called before removing attribute from this manager
    private void attributeRemoved(final M metadata){
        super.fireResourceEvent(new AttributeRemovingEvent(this, getResourceName(), metadata));
    }

    /**
     * Returns a count of connected managementAttributes.
     *
     * @return The count of connected managementAttributes.
     */
    @ThreadSafe
    public final int attributesCount() {
        try (final LockScope ignored = beginRead(AASResource.ATTRIBUTES)) {
            return attributes.size();
        }
    }

    /**
     * Gets an array of connected attributes.
     *
     * @return An array of connected attributes.
     */
    @Override
    public final M[] getAttributeInfo() {
        try(final LockScope ignored = beginRead(AASResource.ATTRIBUTES)) {
            return toArray(attributes.values());
        }
    }

    /**
     * Gets attribute metadata.
     *
     * @param attributeName The name of the attribute.
     * @return The attribute metadata; or {@literal null}, if attribute doesn't exist.
     */
    @Override
    public final M getAttributeInfo(final String attributeName) {
        try(final LockScope ignored = beginRead(AASResource.ATTRIBUTES)){
            final AttributeHolder<M> holder = attributes.get(attributeName);
            return holder != null ? holder.getMetadata() : null;
        }
    }

    /**
     * Gets a set of attributes in sequential manner.
     * @param attributes A set of attributes to read. Cannot be {@literal null}.
     * @return output A list of obtained attributes.
     */
    protected final AttributeList getAttributesSequential(final String[] attributes) {
        final List<Attribute> result = Lists.newArrayListWithExpectedSize(attributes.length);
        for(final String attributeID: attributes)
            try {
                result.add(new Attribute(attributeID, getAttribute(attributeID)));
            } catch (final JMException e) {
                failedToGetAttribute(attributeID, e);
            }
        return new AttributeList(result);
    }

    /**
     * Gets a set of attributes in parallel manner.
     * @param executor The executor used to schedule attribute reader. Cannot be {@literal null}.
     * @param attributes A set of attributes to read. Cannot be {@literal null}.
     * @param timeout Synchronization timeout. May be {@link TimeSpan#INFINITE}.
     * @return  A list of obtained attributes.
     * @throws InterruptedException Operation is interrupted.
     * @throws TimeoutException Unable to read attributes in the specified time duration.
     */
    protected final AttributeList getAttributesParallel(final ExecutorService executor,
                                                        final String[] attributes,
                                                        final TimeSpan timeout) throws InterruptedException, TimeoutException {
        final List<Attribute> result = Collections.
                synchronizedList(Lists.<Attribute>newArrayListWithExpectedSize(attributes.length));
        final CountDownLatch synchronizer = new CountDownLatch(attributes.length);
        for (final String attributeID : attributes)
            executor.submit(new Callable<Object>() {
                @Override
                public Object call() throws JMException {
                    try {
                        return result.add(new Attribute(attributeID, getAttribute(attributeID)));
                    }
                    catch (final JMException e){
                        failedToGetAttribute(attributeID, e);
                        return null;
                    }
                    finally {
                        synchronizer.countDown();
                    }
                }
            });
        if (timeout == null)
            synchronizer.await();
        else if (!synchronizer.await(timeout.duration, timeout.unit))
            throw new TimeoutException();
        return new AttributeList(result);
    }

    /**
     * Get the values of several attributes of the managed resource.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #getAttributesSequential(String[])
     * @see #getAttributesParallel(ExecutorService, String[], TimeSpan)
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
        for(final Attribute attr: attributes.asList()) {
            try {
                setAttribute(attr);
                result.add(attr);
            }
            catch (final JMException e){
                failedToSetAttribute(attr.getName(), attr.getValue(), e);
            }
        }
        return new AttributeList(result);
    }

    /**
     * Sets the values of several attributes of the managed resource in sequential manner.
     *
     * @param executor The executor used to schedule attribute writer. Cannot be {@literal null}.
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @param timeout Synchronization timeout. May be {@link TimeSpan#INFINITE}.
     * @return The list of attributes that were set, with their new values.
     * @throws InterruptedException Operation is interrupted.
     * @throws TimeoutException Unable to set attributes in the specified time duration.
     */
    protected final AttributeList setAttributesParallel(final ExecutorService executor,
                                                        final AttributeList attributes,
                                                        final TimeSpan timeout) throws TimeoutException, InterruptedException {
        if(attributes.isEmpty()) return attributes;
        final List<Attribute> result =
                Collections.synchronizedList(Lists.<Attribute>newArrayListWithExpectedSize(attributes.size()));
        final CountDownLatch synchronizer = new CountDownLatch(attributes.size());
        for (final Attribute attr : attributes.asList())
            executor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    try {
                        setAttribute(attr);
                        return result.add(new Attribute(attr.getName(), attr.getValue()));
                    }
                    catch (final JMException e){
                        failedToSetAttribute(attr.getName(), attr.getValue(), e);
                        return null;
                    }
                    finally {
                        synchronizer.countDown();
                    }
                }
            });
        if(timeout == null)
            synchronizer.await();
        else if(!synchronizer.await(timeout.duration, timeout.unit))
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
     * @see #setAttributesParallel(ExecutorService, AttributeList, TimeSpan)
     */
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        return setAttributesSequential(attributes);
    }

    /**
     * Connects to the specified attribute.
     *
     * @param attributeID The id of the attribute.
     * @param descriptor Attribute descriptor.
     * @return The description of the attribute; or {@literal null},
     * @throws Exception Internal connector error.
     */
    protected abstract M connectAttribute(final String attributeID,
                                                           final AttributeDescriptor descriptor) throws Exception;

    /**
     * Registers a new attribute in this manager.
     *
     * @param attributeID               A key string that is used to invoke attribute from this connector.
     * @param attributeName    The name of the attribute.
     * @param readWriteTimeout A read/write timeout using for attribute read/write operation.
     * @param options          The attribute discovery options.
     * @return The description of the attribute.
     */
    public final M addAttribute(final String attributeID,
                                final String attributeName,
                                final TimeSpan readWriteTimeout,
                                final CompositeData options) {
        AttributeHolder<M> holder;
        try (final LockScope ignored = beginWrite(AASResource.ATTRIBUTES)) {
            holder = attributes.get(attributeID);
            //if attribute exists then we should check whether the input arguments
            //are equal to the existing attribute options
            if (holder != null) {
                if (holder.equals(attributeName, readWriteTimeout, options))
                    return holder.getMetadata();
                else {
                    //remove attribute
                    attributeRemoved(holder.getMetadata());
                    holder = attributes.remove(attributeID);
                    //...and register again
                    if (disconnectAttribute(holder.getMetadata())) {
                        final M metadata = connectAttribute(attributeID, new AttributeDescriptor(attributeName, readWriteTimeout, options));
                        if (metadata != null) {
                            attributes.put(holder = new AttributeHolder<>(metadata, attributeName, readWriteTimeout, options));
                            attributeAdded(holder.getMetadata());
                        }
                    } else holder = null;
                }
            }
            //this is a new attribute, just connect it
            else {
                final M metadata = connectAttribute(attributeID, new AttributeDescriptor(attributeName, readWriteTimeout, options));
                if(metadata != null) {
                    attributes.put(holder = new AttributeHolder<>(metadata, attributeName, readWriteTimeout, options));
                    attributeAdded(holder.getMetadata());
                }
                else throw JMExceptionUtils.attributeNotFound(attributeName);
            }
        } catch (final Exception e) {
            failedToConnectAttribute(attributeID, attributeName, e);
            holder = null;
        }
        return holder != null ? holder.getMetadata() : null;
    }

    /**
     * Reports an error when connecting attribute.
     * @param logger The logger instance. Cannot be {@literal null}.
     * @param logLevel Logging level.
     * @param attributeID The attribute identifier.
     * @param attributeName The name of the attribute.
     * @param e Internal connector error.
     */
    protected static void failedToConnectAttribute(final Logger logger,
                                                   final Level logLevel,
                                                   final String attributeID,
                                                   final String attributeName,
                                                   final Exception e){
        logger.log(logLevel, String.format("Failed to connect attribute %s with ID %s. Context: %s",
                attributeName, attributeID, LogicalOperation.current()), e);
    }

    /**
     * Reports an error when connecting attribute.
     * @param attributeID The attribute identifier.
     * @param attributeName The name of the attribute.
     * @param e Internal connector error.
     * @see #failedToConnectAttribute(Logger, Level, String, String, Exception)
     */
    protected abstract void failedToConnectAttribute(final String attributeID,
                                                     final String attributeName,
                                                     final Exception e);

    /**
     * Obtains the value of a specific attribute of the managed resource.
     * @param metadata The metadata of the attribute.
     * @return The value of the attribute retrieved.
     * @throws Exception Internal connector error.
     */
    protected abstract Object getAttribute(final M metadata) throws Exception;

    private Object getAttribute(final AttributeHolder<M> holder) throws Exception{
        return getAttribute(holder.getMetadata());
    }

    /**
     * Obtains the value of a specific attribute of the managed resource.
     *
     * @param attributeID The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.MBeanException             Wraps a {@link Exception} thrown by the MBean's getter.
     * @throws javax.management.ReflectionException Wraps any exception associated with Java Reflection.
     * @see #setAttribute
     */
    @Override
    public final Object getAttribute(final String attributeID) throws AttributeNotFoundException, MBeanException, ReflectionException {
        try (final LockScope ignored = beginRead(AASResource.ATTRIBUTES)) {
            if (attributes.containsKey(attributeID))
                return getAttribute(attributes.get(attributeID));
            else throw JMExceptionUtils.attributeNotFound(attributeID);
        } catch (final AttributeNotFoundException e) {
            throw e;
        } catch (final MBeanException | ReflectionException e) {
            failedToGetAttribute(attributeID, e);
            throw e;
        } catch (final Exception e) {
            failedToGetAttribute(attributeID, e);
            throw new MBeanException(e);
        }
    }

    /**
     * Reports an error when getting attribute.
     * @param logger The logger instance. Cannot be {@literal null}.
     * @param logLevel Logging level.
     * @param attributeID The attribute identifier.
     * @param e Internal connector error.
     */
    protected static void failedToGetAttribute(final Logger logger,
                                               final Level logLevel,
                                               final String attributeID,
                                               final Exception e){
        logger.log(logLevel, String.format("Failed to get attribute %s. Context: %s",
                attributeID, LogicalOperation.current()), e);
    }

    /**
     * Reports an error when getting attribute.
     * @param attributeID The attribute identifier.
     * @param e Internal connector error.
     * @see #failedToGetAttribute(Logger, Level, String, Exception)
     */
    protected abstract void failedToGetAttribute(final String attributeID,
                                                 final Exception e);

    /**
     * Set the value of a specific attribute of the managed resource.
     * @param attribute The attribute of to set.
     * @param value The value of the attribute.
     * @throws Exception Internal connector error.
     * @throws InvalidAttributeValueException Incompatible attribute type.
     */
    protected abstract void setAttribute(final M attribute,
                                         final Object value) throws Exception;

    private void setAttribute(final AttributeHolder<M> holder,
                              final Object value) throws Exception{
        setAttribute(holder.getMetadata(), value);
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
        try(final LockScope ignored = beginRead(AASResource.ATTRIBUTES)){
            if(attributes.containsKey(attribute.getName()))
                setAttribute(attributes.get(attribute.getName()), attribute.getValue());
            else throw JMExceptionUtils.attributeNotFound(attribute.getName());
        }
        catch (final AttributeNotFoundException e){
            throw e;
        }
        catch (final InvalidAttributeValueException | MBeanException | ReflectionException e){
            failedToSetAttribute(attribute.getName(), attribute.getValue(), e);
            throw e;
        }
        catch (final Exception e){
            failedToSetAttribute(attribute.getName(), attribute.getValue(), e);
            throw new MBeanException(e);
        }
    }

    /**
     * Reports an error when updating attribute.
     * @param logger The logger instance. Cannot be {@literal null}.
     * @param logLevel Logging level.
     * @param attributeID The attribute identifier.
     * @param value The value of the attribute.
     * @param e Internal connector error.
     */
    protected static void failedToSetAttribute(final Logger logger,
                                               final Level logLevel,
                                               final String attributeID,
                                               final Object value,
                                               final Exception e){
        logger.log(logLevel, String.format("Failed to update attribute %s with %s value. Context: %s",
                attributeID, value, LogicalOperation.current()), e);
    }

    /**
     * Reports an error when updating attribute.
     * @param attributeID The attribute identifier.
     * @param value The value of the attribute.
     * @param e Internal connector error.
     * @see #failedToSetAttribute(Logger, Level, String, Object, Exception)
     */
    protected abstract void failedToSetAttribute(final String attributeID,
                                                 final Object value,
                                                 final Exception e);

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeInfo An attribute metadata.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    protected boolean disconnectAttribute(final M attributeInfo) {
        return true;
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeID The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    @ThreadSafe
    public final boolean removeAttribute(final String attributeID) {
        AttributeHolder<M> holder;
        try (final LockScope ignored = beginWrite(AASResource.ATTRIBUTES)) {
            holder = attributes.get(attributeID);
            if(holder != null) {
                attributeRemoved(holder.getMetadata());
                attributes.remove(attributeID);
            }
        }
        return holder != null && disconnectAttribute(holder.getMetadata());
    }

    /**
     * Removes all attributes.
     * @param removeAttributeEventListeners {@literal true} to remove all attribute listeners; otherwise, {@literal false}.
     */
    public final void clear(final boolean removeAttributeEventListeners) {
        try (final LockScope ignored = beginWrite(AASResource.ATTRIBUTES)) {
            for (final AttributeHolder<M> holder : attributes.values())
                if (disconnectAttribute(holder.getMetadata()))
                    attributeRemoved(holder.getMetadata());
            attributes.clear();
        }
        if (removeAttributeEventListeners)
            super.removeAllResourceEventListeners();
    }
}
