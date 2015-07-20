package com.itworks.snamp.adapters.groovy.impl.binding;

import com.itworks.snamp.adapters.groovy.impl.ScriptAttributeAccessor;
import com.itworks.snamp.adapters.binding.AttributeBindingInfo;
import com.itworks.snamp.jmx.WellKnownType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ScriptAttributeBindingInfo extends AttributeBindingInfo {
    private final WellKnownType attributeType;

    ScriptAttributeBindingInfo(final String declaredResource,
                               final ScriptAttributeAccessor accessor) {
        super(declaredResource, accessor);
        attributeType = accessor.getType();
    }

    /**
     * Gets information about attribute type inside of the adapter.
     *
     * @return The information about attribute type inside of the adapter.
     */
    @Override
    public WellKnownType getMappedType() {
        return attributeType;
    }
}
