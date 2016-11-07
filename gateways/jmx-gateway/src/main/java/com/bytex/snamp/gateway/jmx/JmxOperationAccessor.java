package com.bytex.snamp.gateway.jmx;

import com.bytex.snamp.gateway.modeling.OperationAccessor;
import com.google.common.collect.ImmutableSet;

import javax.management.MBeanOperationInfo;
import java.util.Set;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;

/**
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
final class JmxOperationAccessor extends OperationAccessor implements FeatureBindingInfo<MBeanOperationInfo> {
    private final String resourceName;

    JmxOperationAccessor(final String resourceName,
                            final MBeanOperationInfo metadata) {
        super(metadata);
        this.resourceName = resourceName;
    }

    //cloning metadata is required because RMI class loader will raise ClassNotFoundException: NotificationDescriptor (no security manager: RMI class loader disabled)
    MBeanOperationInfo cloneMetadata() {
        return new MBeanOperationInfo(getMetadata().getName(),
                getMetadata().getDescription(),
                getMetadata().getSignature(),
                getMetadata().getReturnType(),
                getMetadata().getImpact(),
                getMetadata().getDescriptor());
    }

    /**
     * Gets binding property such as URL, OID or any other information
     * describing how this feature is exposed to the outside world.
     *
     * @param propertyName The name of the binding property.
     * @return The value of the binding property.
     */
    @Override
    public Object getProperty(final String propertyName) {
        return null;
    }

    /**
     * Gets all supported properties.
     *
     * @return A set of all supported properties.
     */
    @Override
    public Set<String> getProperties() {
        return ImmutableSet.of();
    }

    /**
     * Overwrite property value.
     * <p/>
     * This operation may change behavior of the gateway.
     *
     * @param propertyName The name of the property to change.
     * @param value        A new property value.
     * @return {@literal true}, if the property supports modification and changed successfully; otherwise, {@literal false}.
     */
    @Override
    public boolean setProperty(final String propertyName, final Object value) {
        return false;
    }

}
