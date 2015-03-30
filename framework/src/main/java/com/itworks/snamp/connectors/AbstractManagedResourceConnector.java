package com.itworks.snamp.connectors;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.itworks.snamp.Descriptive;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.connectors.attributes.AttributeAddedEvent;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeRemovingEvent;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.core.AbstractFrameworkService;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.IllegalStateFlag;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.itworks.snamp.jmx.JMExceptionUtils;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an abstract class for building custom management connectors.
 * <p>
 *     This class provides a base support for the following management mechanisms:
 *     <ul>
 *         <li>{@link com.itworks.snamp.connectors.AbstractManagedResourceConnector.AbstractAttributeSupport} for resource management using attributes.</li>
 *         <li>{@link com.itworks.snamp.connectors.AbstractManagedResourceConnector.AbstractNotificationSupport} to receive management notifications from the managed resource.</li>
 *     </ul>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractManagedResourceConnector extends AbstractFrameworkService implements ManagedResourceConnector, Descriptive {
    private static final class WeakResourceEventListener extends WeakReference<ResourceEventListener> implements Supplier<ResourceEventListener> {

        private WeakResourceEventListener(final ResourceEventListener listener) {
            super(Objects.requireNonNull(listener));
        }
    }

    private static final class ResourceEventListenerList extends LinkedList<WeakResourceEventListener>{
        private static final long serialVersionUID = -9139754747382955308L;

        private ResourceEventListenerList(){

        }

        public boolean add(final ResourceEventListener listener) {
            //remove dead references
            final Iterator<WeakResourceEventListener> listeners = iterator();
            while (listeners.hasNext()){
                final WeakResourceEventListener l = listeners.next();
                if(l.get() == null) listeners.remove();
            }
            //add a new weak reference to the listener
            return add(new WeakResourceEventListener(listener));
        }

        public boolean remove(final ResourceEventListener listener){
            final Iterator<WeakResourceEventListener> listeners = iterator();
            while (listeners.hasNext()){
                final WeakResourceEventListener ref = listeners.next();
                final ResourceEventListener l = ref.get();
                if(l == null) listeners.remove(); //remove dead reference
                else if(Objects.equals(listener, l)){
                    ref.clear();    //help GC
                    listeners.remove();
                    return true;
                }
            }
            return false;
        }

        public void fire(final ResourceEvent event){
            final Iterator<WeakResourceEventListener> listeners = iterator();
            while (listeners.hasNext()){
                final WeakResourceEventListener ref = listeners.next();
                final ResourceEventListener l = ref.get();
                if(l == null) listeners.remove(); //remove dead reference
                else l.handle(event);
            }
        }

        @Override
        public void clear() {
            for(final WeakResourceEventListener listener: this)
                listener.clear(); //help GC
            super.clear();
        }
    }

    /**
     * Represents an abstract class for all modelers of managed resource features.
     * You cannot derive from this class directly.
     * @param <F> Type of the modeling feature.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class AbstractFeatureModeler<F extends MBeanFeatureInfo> extends ThreadSafeObject {
        private static abstract class FeatureHolder<F extends MBeanFeatureInfo>{
            private final F metadata;
            private final BigInteger identity;

            private FeatureHolder(final F metadata,
                                  final BigInteger identity){
                this.metadata = Objects.requireNonNull(metadata);
                this.identity = Objects.requireNonNull(identity);
            }

            final boolean equals(final FeatureHolder<?> other){
                return other != null &&
                        Objects.equals(getClass(), other.getClass()) &&
                        Objects.equals(identity, other.identity);
            }

            final F getMetadata(){
                return metadata;
            }

            @Override
            public final boolean equals(final Object other) {
                return other instanceof FeatureHolder<?> && equals((FeatureHolder<?>)other);
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

        private final Class<F> metadataType;
        private final ResourceEventListenerList resourceEventListeners;
        private final Enum<?> resourceEventListenerSyncGroup;
        private final String resourceName;

        private <G extends Enum<G>> AbstractFeatureModeler(final String resourceName,
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

        private void fireResourceEvent(final ResourceEvent event) {
            try (final LockScope ignored = beginWrite(resourceEventListenerSyncGroup)) {
                resourceEventListeners.fire(event);
            }
        }

        private void removeAllResourceEventListeners() {
            try (final LockScope ignored = beginWrite(resourceEventListenerSyncGroup)) {
                resourceEventListeners.clear();
            }
        }
    }

    /**
     * Provides a base support of management attributes.
     * @param <M> Type of the attribute metadata.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class AbstractAttributeSupport<M extends MBeanAttributeInfo> extends AbstractFeatureModeler<M> implements AttributeSupport {
        private static enum AASResource{
            ATTRIBUTES,
            RESOURCE_EVENT_LISTENERS
        }
        private static final class AttributeHolder<M extends MBeanAttributeInfo> extends AbstractFeatureModeler.FeatureHolder<M>{
            private AttributeHolder(final M metadata,
                                    final String attributeName,
                                    final TimeSpan readWriteTimeout,
                                    final CompositeData options){
                super(metadata, computeIdentity(attributeName, readWriteTimeout, options));
            }

            private boolean equals(final String attributeName,
                                   final TimeSpan readWriteTimeout,
                                   final CompositeData options){
                return super.identity.equals(computeIdentity(attributeName, readWriteTimeout, options));
            }

            private static BigInteger computeIdentity(final String attributeName,
                                                      final TimeSpan readWriteTimeout,
                                                      final CompositeData options){
                BigInteger result = new BigInteger(attributeName.getBytes());
                if(readWriteTimeout != null)
                    result = result.xor(BigInteger.valueOf(readWriteTimeout.toNanos()));
                for(final String propertyName: options.getCompositeType().keySet())
                    result = result
                            .xor(new BigInteger(propertyName.getBytes()))
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
        protected final int attributesCount() {
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
                final M[] result = ObjectArrays.newArray(super.metadataType, attributes.size());
                int index = 0;
                for(final AttributeHolder<M> holder: attributes.values())
                    result[index++] = holder.getMetadata();
                return result;
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
         * @param timeout Synchronization timeout. May be {@link com.itworks.snamp.TimeSpan#INFINITE}.
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
         * @see #getAttributesParallel(java.util.concurrent.ExecutorService, String[], com.itworks.snamp.TimeSpan)
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
         * @param timeout Synchronization timeout. May be {@link com.itworks.snamp.TimeSpan#INFINITE}.
         * @return The list of attributes that were set, with their new values.
         * @throws java.lang.InterruptedException Operation is interrupted.
         * @throws java.util.concurrent.TimeoutException Unable to set attributes in the specified time duration.
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
         * @see #setAttributesSequential(javax.management.AttributeList)
         * @see #setAttributesParallel(java.util.concurrent.ExecutorService, javax.management.AttributeList, com.itworks.snamp.TimeSpan)
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
         * @throws java.lang.Exception Internal connector error.
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
            M result;
            try (final LockScope ignored = beginWrite(AASResource.ATTRIBUTES)) {
                AttributeHolder<M> holder = attributes.get(attributeID);
                //if attribute exists then we should check whether the input arguments
                //are equal to the existing attribute options
                if (holder != null)
                    if (holder.equals(attributeName, readWriteTimeout, options))
                        result = holder.getMetadata();
                    else {
                        //remove attribute
                        attributeRemoved(holder.getMetadata());
                        holder = attributes.remove(attributeID);
                        //...and register again
                        if (disconnectAttribute(holder.getMetadata())) {
                            result = connectAttribute(attributeID, new AttributeDescriptor(attributeName, readWriteTimeout, options));
                            if (result != null) {
                                attributes.put(holder = new AttributeHolder<>(result, attributeName, readWriteTimeout, options));
                                attributeAdded(result = holder.getMetadata());
                            }
                        } else result = null;
                    }
                //this is a new attribute, just connect it
                else if ((result = connectAttribute(attributeID, new AttributeDescriptor(attributeName, readWriteTimeout, options))) != null) {
                    attributes.put(new AttributeHolder<>(result, attributeName, readWriteTimeout, options));
                    attributeAdded(result);
                }
                else throw JMExceptionUtils.attributeNotFound(attributeName);
            } catch (final Exception e) {
                failedToConnectAttribute(attributeID, attributeName, e);
                result = null;
            }
            return result;
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
         * @see #failedToConnectAttribute(java.util.logging.Logger, java.util.logging.Level, String, String, Exception)
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
         * @throws javax.management.MBeanException             Wraps a {@link java.lang.Exception} thrown by the MBean's getter.
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
         * @see #failedToGetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        protected abstract void failedToGetAttribute(final String attributeID,
                                                     final Exception e);

        /**
         * Set the value of a specific attribute of the managed resource.
         * @param attribute The attribute of to set.
         * @param value The value of the attribute.
         * @throws Exception Internal connector error.
         * @throws javax.management.InvalidAttributeValueException Incompatible attribute type.
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
         * @throws javax.management.AttributeNotFoundException
         * @throws javax.management.InvalidAttributeValueException
         * @throws javax.management.MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
         * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
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
         * @see #failedToSetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Object, Exception)
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
        @SuppressWarnings("UnusedParameters")
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
            M result;
            try (final LockScope ignored = beginWrite(AASResource.ATTRIBUTES)) {
                final AttributeHolder<M> holder = attributes.get(attributeID);
                if(holder != null) {
                    attributeRemoved(result = holder.getMetadata());
                    attributes.remove(attributeID);
                }
                else result = null;
            }
            return result != null && disconnectAttribute(result);
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

    /**
     * Represents a base class that allows to enable notification support for the management connector.
     * @param <M> Notification metadata.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class AbstractNotificationSupport<M extends MBeanNotificationInfo> extends AbstractFeatureModeler<M> implements NotificationSupport {
        private static enum ANSResource{
            NOTIFICATIONS,
            RESOURCE_EVENT_LISTENERS
        }
        private static final class NotificationHolder<M extends MBeanNotificationInfo> extends AbstractFeatureModeler.FeatureHolder<M>{
            private NotificationHolder(final M metadata,
                                       final String category,
                                       final CompositeData options) {
                super(metadata, computeIdentity(category, options));
            }

            private boolean equals(final String category, final CompositeData options){
                return super.identity.equals(computeIdentity(category, options));
            }

            private static BigInteger computeIdentity(final String category,
                                                      final CompositeData options) {
                BigInteger result = new BigInteger(category.getBytes());
                for (final String propertyName : options.getCompositeType().keySet())
                    result = result.xor(new BigInteger(propertyName.getBytes()))
                            .xor(BigInteger.valueOf(options.get(propertyName).hashCode()));
                return result;
            }
        }

        private final KeyedObjects<String, NotificationHolder<M>> notifications;
        private final NotificationListenerList listeners;
        private final AtomicLong sequenceCounter;

        /**
         * Initializes a new notification manager.
         * @param resourceName The name of the managed resource.
         * @param notifMetadataType Type of the notification metadata;
         */
        protected AbstractNotificationSupport(final String resourceName,
                                              final Class<M> notifMetadataType) {
            super(resourceName,
                    notifMetadataType,
                    ANSResource.class,
                    ANSResource.RESOURCE_EVENT_LISTENERS);
            notifications = createNotifications();
            listeners = new NotificationListenerList();
            sequenceCounter = new AtomicLong(0L);
        }

        private static <M extends MBeanNotificationInfo> AbstractKeyedObjects<String, NotificationHolder<M>> createNotifications(){
            return new AbstractKeyedObjects<String, NotificationHolder<M>>(10) {
                private static final long serialVersionUID = 6753355822109787406L;

                @Override
                public String getKey(final NotificationHolder<M> holder) {
                    return holder.getMetadata().getNotifTypes()[0];
                }
            };
        }

        /**
         * Gets subscription model.
         * @return The subscription model.
         */
        @Override
        public final NotificationSubscriptionModel getSubscriptionModel(){
            final NotificationListenerInvoker invoker = getListenerInvoker();
            if(invoker instanceof NotificationListenerSequentialInvoker)
                return NotificationSubscriptionModel.MULTICAST_SEQUENTIAL;
            else if(invoker instanceof NotificationListenerParallelInvoker)
                return NotificationSubscriptionModel.MULTICAST_PARALLEL;
            else return NotificationSubscriptionModel.MULTICAST;
        }

        /**
         * Gets the invoker used to executed notification listeners.
         * @return The notification listener invoker.
         */
        protected abstract NotificationListenerInvoker getListenerInvoker();

        /**
         * Invokes all listeners associated with the specified notification category.
         * @param category An event category.
         * @param message The human-readable message associated with the notification.
         * @param userData Advanced object associated with the notification.
         */
        protected final void fire(final String category,
                                  final String message,
                                  final Object userData) {
            final Collection<Notification> notifs;
            try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
                notifs = Lists.newArrayListWithExpectedSize(notifications.size());
                for (final NotificationHolder<M> holder : notifications.values())
                    if (Objects.equals(NotificationDescriptor.getNotificationCategory(holder.getMetadata()), category))
                        for (final String listId : holder.getMetadata().getNotifTypes()) {
                            final Notification n = new Notification(listId,
                                    this,
                                    sequenceCounter.getAndIncrement(),
                                    message);
                            n.setTimeStamp(System.currentTimeMillis());
                            n.setUserData(userData);
                            notifs.add(n);
                        }
            }
            //fire listeners
            for (final Notification n : notifs)
                listeners.handleNotification(getListenerInvoker(), n, null);
        }

        private void notificationAdded(final M metadata){
            super.fireResourceEvent(new NotificationAddedEvent(this, getResourceName(), metadata));
        }

        private void notificationRemoved(final M metadata){
            super.fireResourceEvent(new NotificationRemovingEvent(this, getResourceName(), metadata));
        }

        protected abstract M enableNotifications(final String notifType,
                                                final NotificationDescriptor metadata) throws Exception;

        /**
         * Enables event listening for the specified category of events.
         * <p/>
         * category can be used for enabling notifications for the same category
         * but with different options.
         * <p/>
         * listId parameter
         * is used as a value of {@link javax.management.Notification#getType()}.
         *
         * @param listId   An identifier of the subscription list.
         * @param category The name of the event category to listen.
         * @param options  Event discovery options.
         * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
         */
        public final M enableNotifications(final String listId, final String category, final CompositeData options) {
            M result;
            try(final LockScope ignored = beginWrite(ANSResource.NOTIFICATIONS)) {
                NotificationHolder<M> holder = notifications.get(listId);
                if(holder != null)
                    if(holder.equals(category, options))
                        result = holder.getMetadata();
                    else {
                        //remove notification
                        notificationRemoved(holder.getMetadata());
                        holder = notifications.remove(listId);
                        //and register again
                        if(disableNotifications(holder.getMetadata())){
                            result = enableNotifications(listId, new NotificationDescriptor(category, getSubscriptionModel(), options));
                            if(result != null){
                                notifications.put(holder = new NotificationHolder<>(result, category, options));
                                notificationAdded(result = holder.getMetadata());
                            }
                        }
                        else result = null;
                    }
                else result = null;
            }
            catch (final Exception e) {
                failedToEnableNotifications(listId, category, e);
                result = null;
            }
            return result;
        }

        /**
         * Determines whether all notifications disabled.
         * @return {@literal true}, if all notifications disabled; otherwise, {@literal false}.
         */
        protected final boolean hasNoNotifications() {
            try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
                return notifications.isEmpty();
            }
        }

        protected abstract boolean disableNotifications(final M metadata);

        /**
         * Disables event listening for the specified category of events.
         * <p>
         * This method removes all listeners associated with the specified subscription list.
         * </p>
         *
         * @param listId The identifier of the subscription list.
         * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
         */
        public final boolean disableNotifications(final String listId) {
            M result;
            try (final LockScope ignored = beginWrite(ANSResource.NOTIFICATIONS)) {
                final NotificationHolder<M> holder = notifications.get(listId);
                if(holder != null){
                    notificationRemoved(result = holder.getMetadata());
                    notifications.remove(listId);
                }
                else result = null;
            }
            return result != null && disableNotifications(result);
        }

        /**
         * Adds a listener to this MBean.
         *
         * @param listener The listener object which will handle the
         *                 notifications emitted by the broadcaster.
         * @param filter   The filter object. If filter is null, no
         *                 filtering will be performed before handling notifications.
         * @param handback An opaque object to be sent back to the
         *                 listener when a notification is emitted. This object cannot be
         *                 used by the Notification broadcaster object. It should be
         *                 resent unchanged with the notification to the listener.
         * @throws IllegalArgumentException Listener parameter is null.
         * @see #removeNotificationListener
         */
        @Override
        public final void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws IllegalArgumentException {
            listeners.addNotificationListener(listener, filter, handback);
        }

        /**
         * Removes a listener from this MBean.  If the listener
         * has been registered with different handback objects or
         * notification filters, all entries corresponding to the listener
         * will be removed.
         *
         * @param listener A listener that was previously added to this
         *                 MBean.
         * @throws javax.management.ListenerNotFoundException The listener is not
         *                                                    registered with the MBean.
         * @see #addNotificationListener
         * @see javax.management.NotificationEmitter#removeNotificationListener
         */
        @Override
        public final void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
            listeners.removeNotificationListener(listener);
        }

        /**
         * <p>Returns an array indicating, for each notification this
         * MBean may send, the name of the Java class of the notification
         * and the notification type.</p>
         * <p/>
         * <p>It is not illegal for the MBean to send notifications not
         * described in this array.  However, some clients of the MBean
         * server may depend on the array being complete for their correct
         * functioning.</p>
         *
         * @return the array of possible notifications.
         */
        @Override
        public final M[] getNotificationInfo() {
            try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
                final M[] result = ObjectArrays.newArray(super.metadataType, notifications.size());
                int index = 0;
                for(final NotificationHolder<M> holder: notifications.values())
                    result[index++] = holder.getMetadata();
                return result;
            }
        }

        protected final M getNotificationInfo(final String listID) {
            try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
                final NotificationHolder<M> holder = notifications.get(listID);
                return holder != null ? holder.getMetadata() : null;
            }
        }

        /**
         * Reports an error when enabling notifications.
         * @param logger The logger instance. Cannot be {@literal null}.
         * @param logLevel Logging level.
         * @param listID Subscription list identifier.
         * @param category An event category.
         * @param e Internal connector error.
         */
        protected static void failedToEnableNotifications(final Logger logger,
                                                          final Level logLevel,
                                                          final String listID,
                                                          final String category,
                                                          final Exception e){
            logger.log(logLevel, String.format("Failed to enable notifications %s for %s subscription list. Context: %s",
                    category, listID, LogicalOperation.current()), e);
        }

        /**
         * Reports an error when enabling notifications.
         * @param listID Subscription list identifier.
         * @param category An event category.
         * @param e Internal connector error.
         * @see #failedToEnableNotifications(java.util.logging.Logger, java.util.logging.Level, String, String, Exception)
         */
        protected abstract void failedToEnableNotifications(final String listID,
                                                            final String category,
                                                            final Exception e);

        /**
         * Disables all notifications registered in this manager.
         * @param removeNotificationListeners {@literal true} to remove all notification listeners.
         * @param removeResourceEventListeners {@literal true} to remove all notification model listeners.
         */
        public final void clear(final boolean removeNotificationListeners,
                                final boolean removeResourceEventListeners){
            try(final LockScope ignored = beginWrite(ANSResource.NOTIFICATIONS)){
                for(final NotificationHolder<M> holder: notifications.values())
                    if(disableNotifications(holder.getMetadata()))
                        notificationRemoved(holder.getMetadata());
                notifications.clear();
            }
            if(removeNotificationListeners)
                listeners.clear();
            if(removeResourceEventListeners)
                super.removeAllResourceEventListeners();
        }
    }

    private final IllegalStateFlag closed = new IllegalStateFlag() {
        @Override
        public final IllegalStateException create() {
            return new IllegalStateException("Management connector is closed.");
        }
    };

    /**
     *  Throws an {@link IllegalStateException} if the connector is not initialized.
     *  <p>
     *      You should call the base implementation from the overridden method.
     *  </p>
     *  @throws IllegalStateException Connector is not initialized.
     */
    protected void verifyInitialization() throws IllegalStateException{
        closed.verify();
    }

    private void verifyInitializationChecked() throws MBeanException{
        try{
            verifyInitialization();
        }
        catch (final IllegalStateException e){
            throw new MBeanException(e);
        }
    }
    /**
     * Releases all resources associated with this connector.
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    @ThreadSafe(false)
    public void close() throws Exception {
        //change state of the connector
        closed.set();
    }

    /**
     * Obtain the value of a specific attribute of the managed resource.
     *
     * @param attribute The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.MBeanException             Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @throws javax.management.ReflectionException        Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute(javax.management.Attribute)
     */
    @Override
    public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        verifyInitializationChecked();
        final AttributeSupport attributeSupport = queryObject(AttributeSupport.class);
        if(attributeSupport != null)
            return attributeSupport.getAttribute(attribute);
        else throw JMExceptionUtils.attributeNotFound(attribute);
    }

    /**
     * Set the value of a specific attribute of the managed resource.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.InvalidAttributeValueException
     * @throws javax.management.MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        verifyInitializationChecked();
        final AttributeSupport attributeSupport = queryObject(AttributeSupport.class);
        if(attributeSupport != null)
            attributeSupport.setAttribute(attribute);
        else throw JMExceptionUtils.attributeNotFound(attribute.getName());
    }

    /**
     * Get the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #setAttributes
     */
    @Override
    public AttributeList getAttributes(final String[] attributes) {
        verifyInitialization();
        final AttributeSupport attributeSupport = queryObject(AttributeSupport.class);
        return attributeSupport != null ? attributeSupport.getAttributes(attributes) : new AttributeList();
    }

    /**
     * Sets the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     * @see #getAttributes
     */
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        verifyInitialization();
        final AttributeSupport attributeSupport = queryObject(AttributeSupport.class);
        return attributeSupport != null ? attributeSupport.setAttributes(attributes) : new AttributeList();
    }

    /**
     * Allows an action to be invoked on the Dynamic MBean.
     *
     * @param actionName The name of the action to be invoked.
     * @param params     An array containing the parameters to be set when the action is
     *                   invoked.
     * @param signature  An array containing the signature of the action. The class objects will
     *                   be loaded through the same class loader as the one used for loading the
     *                   MBean on which the action is invoked.
     * @return The object returned by the action, which represents the result of
     * invoking the action on the MBean specified.
     * @throws javax.management.MBeanException      Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's invoked method.
     * @throws javax.management.ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        throw new MBeanException(new UnsupportedOperationException("Operation invocation is not supported."));
    }

    private String getClassName(){
        return getClass().getName();
    }

    /**
     * Returns the localized description of this connector.
     *
     * @param locale The locale of the description. If it is {@literal null} then returns description
     *               in the default locale.
     * @return The localized description of this connector.
     */
    @Override
    public String getDescription(final Locale locale) {
        return getClassName();
    }

    /**
     * Gets an array of supported attributes.
     * @return An array of supported attributes.
     */
    public MBeanAttributeInfo[] getAttributeInfo() {
        final AttributeSupport attributes = queryObject(AttributeSupport.class);
        return attributes != null ? attributes.getAttributeInfo() : new MBeanAttributeInfo[0];
    }

    /**
     * Gets an array of supported notifications.
     * @return An array of supported notifications.
     */
    public MBeanNotificationInfo[] getNotificationInfo(){
        final NotificationSupport notifs = queryObject(NotificationSupport.class);
        return notifs != null ? notifs.getNotificationInfo() : new MBeanNotificationInfo[0];
    }

    /**
     * Gets an array of supported operations.
     * @return An array of supported operations.
     */
    public MBeanOperationInfo[] getOperationInfo(){
        return new MBeanOperationInfo[0];
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     * exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public final MBeanInfo getMBeanInfo() {
        return new MBeanInfo(getClassName(),
                getDescription(Locale.getDefault()),
                getAttributeInfo(),
                new MBeanConstructorInfo[0],
                getOperationInfo(),
                getNotificationInfo());
    }

    /**
     * This method may be used for implementing {@link #addResourceEventListener(ResourceEventListener)}
     * method.
     * <p>
     *     You can use instances of {@link AbstractAttributeSupport} and {@link AbstractNotificationSupport}
     *     as arguments for this method.
     *
     * @param listener The listener to be added to the specified modelers.
     * @param modelers A set of modelers.
     */
    protected static void addResourceEventListener(final ResourceEventListener listener,
                                                   final AbstractFeatureModeler<?>... modelers){
        for(final AbstractFeatureModeler<?> modeler: modelers)
            modeler.addModelEventListener(listener);
    }

    /**
     * This method may be used for implementing {@link #removeResourceEventListener(ResourceEventListener)}
     * method.
     * @param listener The listener to be removed from the specified modelers.
     * @param modelers A set of modelers.
     */
    protected static void removeResourceEventListener(final ResourceEventListener listener,
                                                      final AbstractFeatureModeler<?>... modelers){
        for(final AbstractFeatureModeler<?> modeler: modelers)
            modeler.removeModelEventListener(listener);
    }

    /**
     * Updates resource connector with a new connection options.
     * <p>
     *     In the default implementation this method always throws
     *     {@link UnsupportedUpdateOperationException}.
     * @param connectionString     A new connection string.
     * @param connectionParameters A new connection parameters.
     * @throws Exception Internal connector non-recoverable error.                                                                                 Unable to update managed resource connector.
     * @throws UnsupportedUpdateOperationException This operation is not supported
     *                                                                                                   by this resource connector.
     */
    @Override
    public void update(final String connectionString, final Map<String, String> connectionParameters) throws Exception {
        throw new UnsupportedUpdateOperationException("Update operation is not supported");
    }

    /**
     * Returns logger name based on the management connector name.
     * @param connectorName The name of the connector.
     * @return The logger name.
     */
    public static String getLoggerName(final String connectorName){
        return String.format("com.itworks.snamp.connectors.%s", connectorName);
    }

    /**
     * Returns a logger associated with the specified management connector.
     * @param connectorName The name of the connector.
     * @return An instance of the logger.
     */
    public static Logger getLogger(final String connectorName){
        return Logger.getLogger(getLoggerName(connectorName));
    }
}
