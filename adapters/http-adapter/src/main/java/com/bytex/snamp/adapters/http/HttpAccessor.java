package com.bytex.snamp.adapters.http;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface HttpAccessor {
    String RESOURCE_URL_PARAM = "resourceName";
    String getPath(final String servletContext,
                   final String resourceName);
}
