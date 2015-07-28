package com.bytex.snamp.adapters.jmx;

import com.google.common.collect.ImmutableSet;
import com.bytex.snamp.adapters.modeling.AttributeAccessor;

import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import java.util.Set;

import static com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class JmxAttributeAccessor extends AttributeAccessor implements FeatureBindingInfo<MBeanAttributeInfo> {

    JmxAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    abstract OpenMBeanAttributeInfo cloneMetadata();

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
     * This operation may change behavior of the resource adapter.
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
