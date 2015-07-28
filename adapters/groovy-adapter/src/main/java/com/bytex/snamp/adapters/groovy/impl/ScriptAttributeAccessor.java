package com.bytex.snamp.adapters.groovy.impl;

import com.google.common.collect.ImmutableSet;
import com.bytex.snamp.adapters.modeling.AttributeAccessor;

import javax.management.MBeanAttributeInfo;

import static com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 *
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ScriptAttributeAccessor extends AttributeAccessor implements FeatureBindingInfo<MBeanAttributeInfo> {
    /**
     * Initializes a new attribute accessor.
     *
     * @param metadata The metadata of the attribute. Cannot be {@literal null}.
     */
    ScriptAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
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
        switch (propertyName){
            case MAPPED_TYPE: return getType();
            default: return null;
        }
    }

    /**
     * Gets all supported properties.
     *
     * @return A set of all supported properties.
     */
    @Override
    public ImmutableSet<String> getProperties() {
        return ImmutableSet.of(MAPPED_TYPE);
    }

    /**
     * Overwrite property value.
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
