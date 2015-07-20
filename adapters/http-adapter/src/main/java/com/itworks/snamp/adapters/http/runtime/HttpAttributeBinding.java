package com.itworks.snamp.adapters.http.runtime;

import com.itworks.snamp.adapters.http.HttpAttributeAccessor;
import com.itworks.snamp.adapters.runtime.AttributeBinding;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpAttributeBinding extends AttributeBinding {
    private final String mappedType;

    HttpAttributeBinding(final String servletContext,
                         final String resourceName,
                         final HttpAttributeAccessor accessor){
        super(resourceName, accessor);
        mappedType = accessor.getJsonType();
        put("path", accessor.getPath(servletContext, resourceName));
    }

    /**
     * Gets information about attribute type inside of the adapter.
     *
     * @return The information about attribute type inside of the adapter.
     */
    @Override
    public String getMappedType() {
        return mappedType;
    }
}
