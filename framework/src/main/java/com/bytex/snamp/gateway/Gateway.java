package com.bytex.snamp.gateway;

import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Multimap;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.wiring.BundleRevision;

import javax.annotation.Nonnull;
import javax.management.MBeanFeatureInfo;
import java.io.Closeable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface Gateway extends FrameworkService, ServiceListener, Closeable {
    /**
     * This namespace must be defined in Provide-Capability manifest header inside of the bundle containing implementation
     * of gateway.
     * <p>
     *     Example: Provide-Capability: com.bytex.snamp.gateway; type=snmp
     */
    String CAPABILITY_NAMESPACE = "com.bytex.snamp.gateway";

    /**
     * This property must be defined in Provide-Capability manifest header and specify type of gateway.
     * @see #CAPABILITY_NAMESPACE
     */
    String TYPE_CAPABILITY_ATTRIBUTE = "type";

    String NAME_PROPERTY = "instanceName";

    /**
     * Represents binding of the feature from the connected resource.
     * <p>
     *  Feature binding is an information for external consumers about how the gateway
     *  transforms the feature for its own purposes. For example, {@link javax.management.MBeanAttributeInfo}
     *  and {@link com.bytex.snamp.connector.attributes.AttributeSupport} are the forms in which
     *  resource connector representing attribute. {@link com.bytex.snamp.gateway.modeling.AttributeAccessor}
     *  is about representation of the attribute in the internal structure of the gateway.
     *  This interface provides information about how the {@link com.bytex.snamp.gateway.modeling.AttributeAccessor}
     *  transformed into the final representation of the attribute depends on the management protocol and model supported
     *  by the gateway.
     * @param <M>
     */
    interface FeatureBindingInfo<M extends MBeanFeatureInfo>{
        /**
         * The key in the binding details that stores a data type of the attribute or notification attachment
         * used internally by the gateway.
         */
        String MAPPED_TYPE = "mappedType";

        /**
         * Gets metadata of the feature as it is supplied by connected resources.
         * @return The metadata of the feature.
         */
        M getMetadata();

        /**
         * Gets binding property such as URL, OID or any other information
         * describing how this feature is exposed to the outside world.
         * @param propertyName The name of the binding property.
         * @return The value of the binding property.
         */
        Object getProperty(final String propertyName);

        /**
         * Gets all supported properties.
         * @return A set of all supported properties.
         */
        Set<String> getProperties();

        /**
         * Overwrite property value.
         * <p>
         *     This operation may change behavior of the gateway.
         * @param propertyName The name of the property to change.
         * @param value A new property value.
         * @return {@literal true}, if the property supports modification and changed successfully; otherwise, {@literal false}.
         */
        boolean setProperty(final String propertyName, final Object value);
    }

    /**
     * Gets configuration of this gateway.
     * @return Configuration of this gateway.
     */
    @Nonnull
    @Override
    Map<String, Object> getConfiguration();

    /**
     * Updates configuration of this gateway.
     * @param configuration A new configuration to update
     * @throws Exception Unable to update this gateway.
     */
    void update(final Map<String, String> configuration) throws Exception;

    /**
     * Gets state of this instance.
     * @return The state of this instance.
     */
    GatewayState getState();

    /**
     * Gets a collection of features contained in this instance of the gateway.
     * @param featureType Type of the feature.
     * @param <M> Feature class.
     * @return A collection of features associated with resource name.
     * @see Gateway.FeatureBindingInfo
     */
    <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType);

    static String getGatewayType(final Class<? extends Gateway> gatewayType) {
        final BundleContext context = Utils.getBundleContext(gatewayType);
        assert context != null;
        return getGatewayType(context.getBundle());
    }

    static String getGatewayType(final Bundle bnd) {
        final BundleRevision revision = bnd.adapt(BundleRevision.class);
        assert revision != null;
        return revision.getCapabilities(CAPABILITY_NAMESPACE)
                .stream()
                .map(capability -> capability.getAttributes().get(TYPE_CAPABILITY_ATTRIBUTE))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElse("");
    }

    static boolean isGatewayBundle(final Bundle bnd) {
        return bnd != null && !isNullOrEmpty(getGatewayType(bnd));
    }
}
