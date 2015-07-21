package com.itworks.snamp.adapters.jmx.binding;

import com.itworks.snamp.adapters.jmx.JmxAttributeAccessor;

import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxAttributeBindingInfo extends AttributeBindingInfo {
    private final OpenType<?> attributeType;

    JmxAttributeBindingInfo(final String declaredResource,
                            final JmxAttributeAccessor accessor){
        super(declaredResource, accessor);
        attributeType = accessor.cloneMetadata().getOpenType();
    }

    /**
     * Gets information about attribute type inside of the adapter.
     *
     * @return The information about attribute type inside of the adapter.
     */
    @Override
    public OpenType<?> getMappedType() {
        return attributeType;
    }
}
