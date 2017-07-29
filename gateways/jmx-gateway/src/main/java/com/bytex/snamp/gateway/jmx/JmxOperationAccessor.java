package com.bytex.snamp.gateway.jmx;

import com.bytex.snamp.gateway.modeling.OperationAccessor;
import com.google.common.collect.ImmutableSet;

import javax.management.ImmutableDescriptor;
import javax.management.MBeanOperationInfo;
import java.util.Set;

/**
 * @author Evgeniy Kirichenko
 * @version 2.1
 * @since 2.0
 */
public class JmxOperationAccessor extends OperationAccessor implements JmxFeatureBindingInfo<MBeanOperationInfo> {

    JmxOperationAccessor(final MBeanOperationInfo metadata) {
        super(metadata);
    }

    //cloning metadata is required because RMI class loader will raise ClassNotFoundException: NotificationDescriptor (no security manager: RMI class loader disabled)
    @Override
    public MBeanOperationInfo cloneMetadata(){
        return new MBeanOperationInfo(getMetadata().getName(),
                getMetadata().getDescription(),
                getMetadata().getSignature(),
                getMetadata().getReturnType(),
                getMetadata().getImpact(),
                cloneDescriptor());
    }

    final ImmutableDescriptor cloneDescriptor(){
        return JmxFeatureBindingInfo.cloneDescriptor(getDescriptor());
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
