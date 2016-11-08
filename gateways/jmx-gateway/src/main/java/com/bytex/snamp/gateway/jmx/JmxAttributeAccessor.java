package com.bytex.snamp.gateway.jmx;

import com.bytex.snamp.connector.notifications.Severity;
import com.google.common.collect.ImmutableSet;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;

import javax.management.ImmutableDescriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import java.util.Map;
import java.util.Set;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;
import static com.bytex.snamp.jmx.DescriptorUtils.toMap;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class JmxAttributeAccessor extends AttributeAccessor implements JmxFeatureBindingInfo<MBeanAttributeInfo> {

    JmxAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    @Override
    public abstract OpenMBeanAttributeInfoSupport cloneMetadata();

    final ImmutableDescriptor cloneDescriptor(){
        return JmxFeatureBindingInfo.cloneDescriptor(getDescriptor());
    }

    @Override
    protected abstract Object interceptSet(final Object value) throws InvalidAttributeValueException, InterceptionException;

    @Override
    protected abstract Object interceptGet(final Object value) throws InterceptionException;

    /**
     * Gets binding property such as URL, OID or any other information
     * describing how this feature is exposed to the outside world.
     *
     * @param propertyName The name of the binding property.
     * @return The value of the binding property.
     */
    @Override
    public final Object getProperty(final String propertyName) {
        switch (propertyName){
            case MAPPED_TYPE: return cloneMetadata().getOpenType();
            default: return null;
        }
    }

    /**
     * Gets all supported properties.
     *
     * @return A set of all supported properties.
     */
    @Override
    public final Set<String> getProperties() {
        return ImmutableSet.of(MAPPED_TYPE);
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
    public final boolean setProperty(final String propertyName, final Object value) {
        return false;
    }
}
