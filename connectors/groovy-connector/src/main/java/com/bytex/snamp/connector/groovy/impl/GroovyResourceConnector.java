package com.bytex.snamp.connector.groovy.impl;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.groovy.*;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.*;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LongCounter;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.osgi.framework.BundleContext;

import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class GroovyResourceConnector extends AbstractManagedResourceConnector {
    private static final class GroovyNotificationInfo extends CustomNotificationInfo implements AutoCloseable{
        private static final long serialVersionUID = -6413432323063142285L;
        private NotificationEmitter emitter;

        private GroovyNotificationInfo(final String notifType,
                                       final NotificationDescriptor descriptor,
                                       final NotificationEmitter emitter){
            super(notifType, getDescription(descriptor, notifType), descriptor);
            this.emitter = Objects.requireNonNull(emitter);
        }

        private static String getDescription(final NotificationDescriptor descriptor,
                                             final String fallback){
            final String result = descriptor.getDescription();
            return isNullOrEmpty(result) ?
                    fallback:
                    result;
        }

        @Override
        public void close() throws Exception {
            final NotificationEmitter emitter = this.emitter;
            this.emitter = null;
            if (emitter != null)
                emitter.close();
        }
    }

    private static final class GroovyNotificationRepository extends AccurateNotificationRepository<GroovyNotificationInfo> {
        private final EventConnector connector;
        private final NotificationListenerInvoker listenerInvoker;
        private final LongCounter sequenceNumberGenerator;

        private final class NotificationEmitterSlim implements NotificationEmitter{
            private final String category;

            private NotificationEmitterSlim(final String category){
                this.category = Objects.requireNonNull(category);
            }

            @Override
            public void emitNotification(final String message) {
                emitNotification(message, null);
            }

            @Override
            public void emitNotification(final String message, final Object userData) {
                fire(category, message, sequenceNumberGenerator, userData);
            }

            @Override
            @MethodStub
            public void close() {

            }
        }

        private GroovyNotificationRepository(final String resourceName,
                                             final EventConnector connector,
                                             final ExecutorService threadPool,
                                             final BundleContext context) {
            super(resourceName,
                    GroovyNotificationInfo.class,
                    false);
            this.connector = Objects.requireNonNull(connector);
            this.listenerInvoker = createListenerInvoker(threadPool);
            this.sequenceNumberGenerator = DistributedServices.getDistributedCounter(context, "notifications-".concat(resourceName));
        }

        private static NotificationListenerInvoker createListenerInvoker(final Executor executor) {
            return NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker(executor, (e, source) -> getLoggerImpl().log(Level.SEVERE, "Unable to process JMX notification.", e));
        }

        /**
         * Gets the invoker used to executed notification listeners.
         *
         * @return The notification listener invoker.
         */
        @Override
        protected NotificationListenerInvoker getListenerInvoker() {
            return listenerInvoker;
        }

        @Override
        protected GroovyNotificationInfo connectNotifications(final String notifType,
                                                             final NotificationDescriptor metadata) throws ResourceException, ScriptException {
            final NotificationEmitter emitter = connector.loadEvent(notifType, metadata, new NotificationEmitterSlim(metadata.getName(notifType)));
            return new GroovyNotificationInfo(notifType, metadata, emitter);
        }

        @Override
        protected void disconnectNotifications(final GroovyNotificationInfo metadata) {
            try {
                metadata.close();
            } catch (final Exception e) {
                getLoggerImpl().log(Level.SEVERE, "Unexpected exception", e);
            }
        }

        /**
         * Reports an error when enabling notifications.
         *
         * @param category An event category.
         * @param e        Internal connector error.
         * @see #failedToEnableNotifications(Logger, Level, String, Exception)
         */
        @Override
        protected void failedToEnableNotifications(final String category, final Exception e) {
            failedToEnableNotifications(getLoggerImpl(), Level.SEVERE, category, e);
        }
    }

    private static final class GroovyAttributeInfo extends AbstractOpenAttributeInfo implements AutoCloseable{
        private static final long serialVersionUID = 2519548731335827051L;
        private final AttributeAccessor accessor;

        private GroovyAttributeInfo(final String attributeName,
                                    final AttributeDescriptor descriptor,
                                    final AttributeAccessor accessor){
            super(attributeName,
                    accessor.type(),
                    getDescription(descriptor, attributeName),
                    accessor.specifier(),
                    descriptor);
            this.accessor = accessor;
        }

        private static String getDescription(final AttributeDescriptor descriptor,
                                             final String fallback){
            final String result = descriptor.getDescription();
            return isNullOrEmpty(result) ? fallback : result;
        }

        @Override
        public void close() throws Exception {
            accessor.close();
        }
    }

    private static final class GroovyAttributeRepository extends AbstractAttributeRepository<GroovyAttributeInfo> {
        private final AttributeConnector connector;

        private GroovyAttributeRepository(final String resourceName,
                                          final AttributeConnector connector){
            super(resourceName, GroovyAttributeInfo.class, false);
            this.connector = Objects.requireNonNull(connector);
        }
        @Override
        protected GroovyAttributeInfo connectAttribute(final String attributeName,
                                                       final AttributeDescriptor descriptor) throws ResourceException, ScriptException {
            final AttributeAccessor accessor = connector.loadAttribute(attributeName, descriptor);
            //create wrapper
            return new GroovyAttributeInfo(attributeName, descriptor, accessor);
        }
        @Override
        protected void failedToConnectAttribute(final String attributeName, final Exception e) {
            failedToConnectAttribute(getLoggerImpl(), Level.SEVERE, attributeName, e);
        }

        private static Object getAttribute(final AttributeAccessor accessor) throws Exception {
            final Object result = accessor.getValue();
            if(accessor.type().isValue(result))
                return result;
            else throw new InvalidAttributeValueException(String.format("Unable cast '%s' to '%s'", result, accessor.type()));
        }

        @Override
        protected Object getAttribute(final GroovyAttributeInfo metadata) throws ReflectionException, InvalidAttributeValueException {
            try {
                return getAttribute(metadata.accessor);
            } catch (final InvalidAttributeValueException e) {
                throw e;
            } catch (final Exception e) {
                throw new ReflectionException(e);
            }
        }

        @Override
        protected void failedToGetAttribute(final String attributeName, final Exception e) {
            failedToGetAttribute(getLoggerImpl(), Level.SEVERE, attributeName, e);
        }

        private static void setAttribute(final AttributeAccessor accessor,
                                         final Object value) throws Exception{
            if(accessor.type().isValue(value))
                accessor.setValue(value);
            else throw new InvalidAttributeValueException(String.format("Unable cast '%s' to '%s'", value, accessor.type()));
        }

        @Override
        protected void setAttribute(final GroovyAttributeInfo attribute, final Object value) throws ReflectionException, InvalidAttributeValueException {
            try {
                setAttribute(attribute.accessor, value);
            } catch (final InvalidAttributeValueException e) {
                throw e;
            } catch (final Exception e) {
                throw new ReflectionException(e);
            }
        }

        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(getLoggerImpl(), Level.SEVERE, attributeID, value, e);
        }

        /**
         * Removes the attribute from the connector.
         *
         * @param attributeInfo An attribute metadata.
         */
        @Override
        protected void disconnectAttribute(final GroovyAttributeInfo attributeInfo) {
            try {
                attributeInfo.close();
            } catch (final Exception e) {
                getLoggerImpl().log(Level.WARNING, String.format("Unable to disconnect attribute %s", attributeInfo.getName()), e);
            }
        }
    }

    private static final String RESOURCE_NAME_VAR = ManagedResourceScriptBase.RESOURCE_NAME_VAR;
    @Aggregation(cached = true)
    private final GroovyAttributeRepository attributes;
    private final ManagedResourceInfo groovyConnector;
    @Aggregation(cached = true)
    private final GroovyNotificationRepository events;

    static Properties toProperties(final Map<String, String> params){
        final Properties props = new Properties();
        props.putAll(params);
        return props;
    }

    GroovyResourceConnector(final String resourceName,
                            final String connectionString,
                            final Map<String, String> params) throws IOException, ResourceException, ScriptException {
        final String[] paths = IOUtils.splitPath(connectionString);
        final ManagedResourceScriptEngine engine = new ManagedResourceScriptEngine(getClass().getClassLoader(),
                toProperties(params),
                paths);
        engine.setGlobalVariable(RESOURCE_NAME_VAR, resourceName);
        final String initScript = GroovyResourceConfigurationDescriptor.getInitScriptFile(params);
        groovyConnector = isNullOrEmpty(initScript) ?
                null :
                engine.init(initScript, params);
        attributes = new GroovyAttributeRepository(resourceName, engine);
        final ExecutorService threadPool = GroovyResourceConfigurationDescriptor.getInstance().parseThreadPool(params);
        events = new GroovyNotificationRepository(resourceName, engine, threadPool, Utils.getBundleContextOfObject(this));
    }

    @Aggregation
    @SpecialUse
    protected MetricsSupport createMetricsReader(){
        return assembleMetricsReader(attributes, events);
    }

    private static Logger getLoggerImpl(){
        return ResourceConnectorInfo.getLogger();
    }

    /**
     * Gets a logger associated with this platform service.
     *
     * @return A logger associated with this platform service.
     */
    @Override
    public Logger getLogger() {
        return getLoggerImpl();
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        super.close();
        attributes.close();
        events.close();
        if(groovyConnector != null)
            groovyConnector.close();
    }
}