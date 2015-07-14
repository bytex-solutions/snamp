package com.itworks.snamp.connectors.openstack;

import com.google.common.base.Function;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.ConfigParameters;
import com.itworks.snamp.connectors.AbstractFeatureModeler;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ResourceEventListener;
import com.itworks.snamp.connectors.attributes.AbstractAttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;

import javax.annotation.Nullable;
import javax.management.MBeanFeatureInfo;
import javax.management.openmbean.CompositeData;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.itworks.snamp.connectors.openstack.OpenStackResourceConnectorConfigurationDescriptor.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class OpenStackResourceConnector extends AbstractManagedResourceConnector {
    static final String NAME = "openstack";

    static {
        OSFactory.useJDKLogger();
    }

    private static final class OpenStackAttributeSupport extends AbstractAttributeSupport<OpenStackResourceAttribute> {
        private final Logger logger;
        private final OpenStackResourceType resourceType;
        private final OSClient client;
        private final String entityID;
        private static final Class<OpenStackResourceAttribute> FEATURE_TYPE = OpenStackResourceAttribute.class;

        private OpenStackAttributeSupport(final String resourceName,
                                          final String entityID,
                                          final OpenStackResourceType resourceType,
                                          final OSClient client,
                                          final Logger logger) {
            super(resourceName, FEATURE_TYPE);
            this.logger = logger;
            this.resourceType = resourceType;
            this.client = client;
            this.entityID = entityID;
        }

        @Override
        public Collection<OpenStackResourceAttribute> expand() {
            final List<OpenStackResourceAttribute> result = new LinkedList<>();
            for(final String attributeName: resourceType.getAttributes()) {
                final OpenStackResourceAttribute attr = addAttribute(attributeName, attributeName, TimeSpan.INFINITE, ConfigParameters.empty());
                if(attr != null) result.add(attr);
            }
            return result;
        }

        private static boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType){
            return featureType.isAssignableFrom(FEATURE_TYPE);
        }

        /**
         * Connects to the specified attribute.
         *
         * @param attributeID The id of the attribute.
         * @param descriptor  Attribute descriptor.
         * @return The description of the attribute; or {@literal null},
         * @throws Exception Internal connector error.
         */
        @Override
        protected OpenStackResourceAttribute connectAttribute(final String attributeID, final AttributeDescriptor descriptor) throws Exception {
            return resourceType.connectAttribute(entityID, attributeID, descriptor, client);
        }

        /**
         * Reports an error when connecting attribute.
         *
         * @param attributeID   The attribute identifier.
         * @param attributeName The name of the attribute.
         * @param e             Internal connector error.
         * @see #failedToConnectAttribute(Logger, Level, String, String, Exception)
         */
        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.SEVERE, attributeID, attributeName, e);
        }

        /**
         * Obtains the value of a specific attribute of the managed resource.
         *
         * @param metadata The metadata of the attribute.
         * @return The value of the attribute retrieved.
         * @throws Exception Internal connector error.
         */
        @Override
        protected Object getAttribute(final OpenStackResourceAttribute metadata) throws Exception {
            return metadata.getValue();
        }

        /**
         * Reports an error when getting attribute.
         *
         * @param attributeID The attribute identifier.
         * @param e           Internal connector error.
         * @see #failedToGetAttribute(Logger, Level, String, Exception)
         */
        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(logger, Level.SEVERE, attributeID, e);
        }

        /**
         * Set the value of a specific attribute of the managed resource.
         *
         * @param attribute The attribute of to set.
         * @param value     The value of the attribute.
         * @throws Exception                      Internal connector error.
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void setAttribute(final OpenStackResourceAttribute attribute, final Object value) throws Exception {
            attribute.setValue(value);
        }

        /**
         * Reports an error when updating attribute.
         *
         * @param attributeID The attribute identifier.
         * @param value       The value of the attribute.
         * @param e           Internal connector error.
         * @see #failedToSetAttribute(Logger, Level, String, Object, Exception)
         */
        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(logger, Level.SEVERE, attributeID, value, e);
        }

        /**
         * Removes the attribute from the connector.
         *
         * @param attributeInfo An attribute metadata.
         * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
         */
        @Override
        protected boolean disconnectAttribute(final OpenStackResourceAttribute attributeInfo) {
            attributeInfo.disconnect();
            return true;
        }

        @Override
        public String toString() {
            return "resource = " + getResourceName() +
                    "entity = " + entityID +
                    "type = " + resourceType;
        }
    }

    private final OpenStackAttributeSupport attributes;
    private final boolean smartMode;

    OpenStackResourceConnector(final String resourceName,
                               final boolean smartMode,
                               final OpenStackResourceType resourceType,
                               final String entityID,
                               final OSClient client) throws UnsupportedOpenStackFeatureException {
        if (!resourceType.checkCapability(client))
            throw new UnsupportedOpenStackFeatureException(resourceType);
        attributes = new OpenStackAttributeSupport(resourceName,
                entityID,
                resourceType,
                client,
                getLoggerImpl());
        this.smartMode = smartMode;
    }

    OpenStackResourceConnector(final String resourceName,
                               final Map<String, String> parameters) throws OpenStackAbsentConfigurationParameterException, UnsupportedOpenStackFeatureException {
        this(resourceName, isSmartModeEnabled(parameters), getResourceType(parameters), getEntityID(parameters), createClient(parameters));
    }

    private static Logger getLoggerImpl(){
        return getLogger(NAME);
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

    void addAttribute(final String attributeID,
                      final String attributeName,
                      final TimeSpan readWriteTimeout,
                      final CompositeData options){
        verifyInitialization();
        attributes.addAttribute(attributeID, attributeName, readWriteTimeout, options);
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
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType, new Function<Class<T>, T>() {
            @Nullable
            @Override
            public T apply(final Class<T> objectType) {
                return OpenStackResourceConnector.super.queryObject(objectType);
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
    }

    @Override
    public boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType) {
        return smartMode && (OpenStackAttributeSupport.canExpandWith(featureType));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F extends MBeanFeatureInfo> Collection<? extends F> expand(final Class<F> featureType) {
        if(smartMode)
            if(attributes.canExpandWith(featureType))
                return (Collection<F>)attributes.expand();
        return Collections.emptyList();
    }
}
