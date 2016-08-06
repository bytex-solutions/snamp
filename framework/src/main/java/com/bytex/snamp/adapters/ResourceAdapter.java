package com.bytex.snamp.adapters;

import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Multimap;
import com.bytex.snamp.core.FrameworkService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;

import javax.management.MBeanFeatureInfo;
import java.io.Closeable;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents resource adapter.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface ResourceAdapter extends FrameworkService, ServiceListener, Closeable {
    /**
     * This namespace must be defined in Provide-Capability manifest header inside of the bundle containing implementation
     * of Resource Adapter.
     * <p>
     *     Example: Provide-Capability: com.bytex.snamp.adapters; type=snmp
     */
    String CAPABILITY_NAMESPACE = "com.bytex.snamp.adapters";

    /**
     * This property must be defined in Provide-Capability manifest header and specify type of Resource Adapter.
     * @see #CAPABILITY_NAMESPACE
     */
    String TYPE_CAPABILITY_ATTRIBUTE = "type";

    /**
     * Represents binding of the feature from the connected resource.
     * <p>
     *  Feature binding is an information for external consumers about how the adapter
     *  transforms the feature for its own purposes. For example, {@link javax.management.MBeanAttributeInfo}
     *  and {@link com.bytex.snamp.connectors.attributes.AttributeSupport} are the forms in which
     *  resource connector representing attribute. {@link com.bytex.snamp.adapters.modeling.AttributeAccessor}
     *  is about representation of the attribute in the internal structure of the resource adapter.
     *  This interface provides information about how the {@link com.bytex.snamp.adapters.modeling.AttributeAccessor}
     *  transformed into the final representation of the attribute depends on the management protocol and model supported
     *  by the resource adapter.
     * @param <M>
     */
    interface FeatureBindingInfo<M extends MBeanFeatureInfo>{
        /**
         * The key in the binding details that stores a data type of the attribute or notification attachment
         * used internally by the resource adapter.
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
         *     This operation may change behavior of the resource adapter.
         * @param propertyName The name of the property to change.
         * @param value A new property value.
         * @return {@literal true}, if the property supports modification and changed successfully; otherwise, {@literal false}.
         */
        boolean setProperty(final String propertyName, final Object value);
    }

    /**
     * Gets name of the resource adapter instance.
     * @return The name of the resource adapter instance.
     */
    String getInstanceName();

    /**
     * Gets state of this resource adapter.
     * @return The state of this resource adapter.
     */
    AdapterState getState();

    /**
     * Gets a collection of features contained in this instance of the adapter.
     * @param featureType Type of the feature.
     * @param <M> Feature class.
     * @return A collection of features associated with resource name.
     * @see com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo
     */
    <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType);

    static String getResourceAdapterType(final Bundle bnd){
        final BundleRevision revision = bnd.adapt(BundleRevision.class);
        assert revision != null;
        return revision.getCapabilities(CAPABILITY_NAMESPACE)
                .stream()
                .map(capability -> capability.getAttributes().get(TYPE_CAPABILITY_ATTRIBUTE))
                .map(name -> Objects.toString(name, ""))
                .findFirst()
                .orElseGet(() -> "");
    }

    static boolean isResourceAdapterBundle(final Bundle bnd) {
        return bnd != null && !isNullOrEmpty(getResourceAdapterType(bnd));
    }

    static boolean isResourceAdapter(final ServiceReference<?> ref){
        return Utils.isInstanceOf(ref, ManagedResourceConnector.class) && isResourceAdapterBundle(ref.getBundle());
    }
}
