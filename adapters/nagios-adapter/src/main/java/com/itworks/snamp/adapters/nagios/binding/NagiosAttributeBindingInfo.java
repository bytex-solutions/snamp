package com.itworks.snamp.adapters.nagios.binding;

import com.itworks.snamp.adapters.binding.AttributeBindingInfo;
import com.itworks.snamp.adapters.nagios.NagiosAttributeAccessor;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NagiosAttributeBindingInfo extends AttributeBindingInfo {
    NagiosAttributeBindingInfo(final String servletContext,
                               final String resourceName,
                               final NagiosAttributeAccessor accessor){
        super(resourceName, accessor);
        put("path", accessor.getPath(servletContext, resourceName));
    }

    /**
     * Gets information about attribute type inside of the adapter.
     *
     * @return The information about attribute type inside of the adapter.
     */
    @Override
    public Object getMappedType() {
        return "Text";
    }
}
