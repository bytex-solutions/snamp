package com.bytex.snamp.gateway.http;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
interface HttpAccessor {
    String RESOURCE_URL_PARAM = "resourceName";
    String getPath(final String servletContext,
                   final String resourceName);
}
