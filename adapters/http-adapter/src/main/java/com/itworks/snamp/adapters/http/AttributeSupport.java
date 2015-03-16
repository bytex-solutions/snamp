package com.itworks.snamp.adapters.http;

import javax.ws.rs.WebApplicationException;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeSupport {
    String getAttribute(final String resourceName,
                        final String attributeName) throws WebApplicationException;

    void setAttribute(final String resourceName,
                      final String attributeName,
                      final String value) throws WebApplicationException;

    Set<String> getResourceAttributes(final String resourceName);

    Set<String> getHostedResources();
}
