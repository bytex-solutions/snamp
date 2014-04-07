package com.itworks.snamp.connectors;

import java.util.*;

/**
 * Represents a root interface for all management entities, such as attributes, notifications and etc.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagementEntityMetadata extends Map<String, String> {

    /**
     * Returns the localized description of this management entity.
     * @param locale The locale of the description. If it is {@literal null} then returns description
     *               in the default locale.
     * @return The localized description of this management entity.
     */
    public String getDescription(final Locale locale);
}
