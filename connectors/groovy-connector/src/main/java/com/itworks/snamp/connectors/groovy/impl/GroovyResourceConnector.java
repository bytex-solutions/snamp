package com.itworks.snamp.connectors.groovy.impl;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.GroupedThreadFactory;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ResourceEventListener;
import com.itworks.snamp.connectors.attributes.AbstractAttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.OpenTypeAttributeInfo;
import com.itworks.snamp.connectors.groovy.*;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.MethodStub;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.osgi.framework.BundleContext;

import javax.management.InvalidAttributeValueException;
import javax.management.NotificationListener;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GroovyResourceConnector extends AbstractManagedResourceConnector {
    static final String NAME = ResourceConnectorInfo.NAME;

    private static final class GroovyNotificationInfo extends CustomNotificationInfo implements AutoCloseable{
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
            return Strings.isNullOrEmpty(result) ?
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

    private static final class GroovyNotificationSupport extends AbstractNotificationSupport<GroovyNotificationInfo>{
        private final EventConnector connector;
        private final NotificationListenerInvoker listenerInvoker;

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
                fire(category, message, userData);
            }

            @Override
            @MethodStub
            public void close() {

            }
        }

        private GroovyNotificationSupport(final String resourceName,
                                          final EventConnector connector){
            super(resourceName, GroovyNotificationInfo.class);
            this.connector = Objects.requireNonNull(connector);
            final ExecutorService executor = Executors.newSingleThreadExecutor(new GroupedThreadFactory("notifs-" + resourceName));
            this.listenerInvoker = createListenerInvoker(executor);
        }

        private static NotificationListenerInvoker createListenerInvoker(final Executor executor){
            return NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker(executor, new NotificationListenerInvokerFactory.ExceptionHandler() {
                @Override
                public final void handle(final Throwable e, final NotificationListener source) {
                    getLoggerImpl().log(Level.SEVERE, "Unable to process JMX notification.", e);
                }
            });
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
        protected GroovyNotificationInfo enableNotifications(final String notifType,
                                                             final NotificationDescriptor metadata) throws ResourceException, ScriptException {
            final NotificationEmitter emitter = connector.loadEvent(metadata,
                    new NotificationEmitterSlim(metadata.getNotificationCategory()));
            return new GroovyNotificationInfo(notifType, metadata, emitter);
        }

        @Override
        protected boolean disableNotifications(final GroovyNotificationInfo metadata) {
            try {
                metadata.close();
                return true;
            } catch (final Exception ignored) {
                return false;
            }
        }

        /**
         * Reports an error when enabling notifications.
         *
         * @param listID   Subscription list identifier.
         * @param category An event category.
         * @param e        Internal connector error.
         * @see #failedToEnableNotifications(Logger, Level, String, String, Exception)
         */
        @Override
        protected void failedToEnableNotifications(final String listID, final String category, final Exception e) {
            failedToEnableNotifications(getLoggerImpl(), Level.SEVERE, listID, category, e);
        }
    }

    private static final class GroovyAttributeInfo extends OpenTypeAttributeInfo implements AutoCloseable{
        private final AttributeAccessor accessor;

        private GroovyAttributeInfo(final String attributeID,
                                    final AttributeDescriptor descriptor,
                                    final AttributeAccessor accessor){
            super(attributeID,
                    accessor.type(),
                    getDescription(descriptor, attributeID),
                    accessor.specifier());
            this.accessor = accessor;
        }

        private static String getDescription(final AttributeDescriptor descriptor,
                                             final String fallback){
            final String result = descriptor.getDescription();
            return Strings.isNullOrEmpty(result) ? fallback : result;
        }

        @Override
        public void close() throws Exception {
            accessor.close();
        }
    }

    private static final class GroovyAttributeSupport extends AbstractAttributeSupport<GroovyAttributeInfo>{
        private final AttributeConnector connector;

        private GroovyAttributeSupport(final String resourceName,
                                       final AttributeConnector connector){
            super(resourceName, GroovyAttributeInfo.class);
            this.connector = Objects.requireNonNull(connector);
        }
        @Override
        protected GroovyAttributeInfo connectAttribute(final String attributeID,
                                                       final AttributeDescriptor descriptor) throws ResourceException, ScriptException {
            final AttributeAccessor accessor = connector.loadAttribute(descriptor);
            //create wrapper
            return new GroovyAttributeInfo(attributeID, descriptor, accessor);
        }

        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(getLoggerImpl(), Level.SEVERE, attributeID, attributeName, e);
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
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(getLoggerImpl(), Level.SEVERE, attributeID, e);
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
         * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
         */
        @Override
        protected boolean disconnectAttribute(final GroovyAttributeInfo attributeInfo) {
            boolean success = true;
            try {
                attributeInfo.close();
            } catch (final Exception e) {
                try(final OSGiLoggingContext logger = OSGiLoggingContext.get(getLoggerImpl(), getBundleContext())){
                    logger.log(Level.WARNING, String.format("Unable to disconnect attribute %s", attributeInfo.getName()), e);
                }
                finally {
                    success = false;
                }
            }
            return success;
        }

        private BundleContext getBundleContext(){
            return Utils.getBundleContextByObject(this);
        }
    }

    private static final String RESOURCE_NAME_VAR = "resourceName";
    private final GroovyAttributeSupport attributes;
    private static final Splitter PATH_SPLITTER;
    private final ManagedResourceInfo groovyConnector;
    private final GroovyNotificationSupport events;

    static {
        final String pathSeparator = StandardSystemProperty.PATH_SEPARATOR.value();
        PATH_SPLITTER = Splitter.on(Strings.isNullOrEmpty(pathSeparator) ? ":" : pathSeparator);
    }

    static Properties toProperties(final Map<String, String> params){
        final Properties props = new Properties();
        props.putAll(params);
        return props;
    }

    static String[] getPaths(final String connectionString){
        return ArrayUtils.toArray(PATH_SPLITTER.trimResults().splitToList(connectionString),
                String.class);
    }

    GroovyResourceConnector(final String resourceName,
                            final String connectionString,
                            final Map<String, String> params) throws IOException, ResourceException, ScriptException {
        final String[] paths = getPaths(connectionString);
        final ManagementScriptEngine engine = new ManagementScriptEngine(getClass().getClassLoader(),
                toProperties(params),
                paths);
        engine.setGlobalVariable(RESOURCE_NAME_VAR, resourceName);
        final String initScript = GroovyResourceConfigurationDescriptor.getInitScriptFile(params);
        groovyConnector = Strings.isNullOrEmpty(initScript) ?
                null :
                engine.init(initScript, params);
        attributes = new GroovyAttributeSupport(resourceName, engine);
        events = new GroovyNotificationSupport(resourceName, engine);
    }

    static Logger getLoggerImpl(){
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

    void addAttribute(final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
        verifyInitialization();
        attributes.addAttribute(attributeID, attributeName, readWriteTimeout, options);
    }

    void enableNotifications(final String listID,
                             final String category,
                             final CompositeData options){
        verifyInitialization();
        events.enableNotifications(listID, category, options);
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType,
                new Function<Class<T>, T>() {
                    @Override
                    public T apply(final Class<T> objectType) {
                        return GroovyResourceConnector.super.queryObject(objectType);
                    }
                }, attributes, events);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        super.close();
        attributes.clear(true);
        events.clear(true, true);
        if(groovyConnector != null)
            groovyConnector.close();
    }
}