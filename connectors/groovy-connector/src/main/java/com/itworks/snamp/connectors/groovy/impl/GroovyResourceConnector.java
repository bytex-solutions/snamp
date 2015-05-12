package com.itworks.snamp.connectors.groovy.impl;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ResourceEventListener;
import com.itworks.snamp.connectors.attributes.AbstractAttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.OpenTypeAttributeInfo;
import com.itworks.snamp.connectors.groovy.*;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.internal.Utils;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GroovyResourceConnector extends AbstractManagedResourceConnector {
    static final String NAME = ResourceConnectorInfo.NAME;

    private static final class GroovyAttributeInfo extends OpenTypeAttributeInfo{
        private final AttributeScript accessor;

        private GroovyAttributeInfo(final String attributeID,
                                    final AttributeDescriptor descriptor,
                                    final AttributeScript accessor){
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
            final AttributeScript accessor = connector.loadAttribute(descriptor);
            //initialize script
            accessor.run();
            //create wrapper
            return new GroovyAttributeInfo(attributeID, descriptor, accessor);
        }

        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(getLoggerImpl(), Level.SEVERE, attributeID, attributeName, e);
        }

        @Override
        protected Object getAttribute(final GroovyAttributeInfo metadata) throws Exception {
            return metadata.accessor.getValue();
        }

        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(getLoggerImpl(), Level.SEVERE, attributeID, e);
        }

        @Override
        protected void setAttribute(final GroovyAttributeInfo attribute, final Object value) throws Exception {
            attribute.accessor.setValue(value);
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
                attributeInfo.accessor.close();
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

    private final GroovyAttributeSupport attributes;
    private static final Splitter PATH_SPLITTER;
    private final InitializationScript groovyConnector;

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
        final ManagementScriptEngine engine = new ManagementScriptEngine(toProperties(params), paths);
        final String initScript = GroovyResourceConfigurationDescriptor.getInitScriptFile(params);
        groovyConnector = Strings.isNullOrEmpty(initScript) ?
                null :
                engine.init(initScript, params);
        attributes = new GroovyAttributeSupport(resourceName, engine);
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
                }, attributes);
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
        if(groovyConnector != null)
            groovyConnector.close();
    }
}
