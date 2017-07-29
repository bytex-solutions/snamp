package com.bytex.snamp.gateway.http;

import javax.ws.rs.WebApplicationException;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
interface AttributeSupport {
    String getAttribute(final String resourceName,
                        final String attributeName) throws WebApplicationException;

    void setAttribute(final String resourceName,
                      final String attributeName,
                      final String value) throws WebApplicationException;

    Set<String> getResourceAttributes(final String resourceName);

    Set<String> getHostedResources();
}
