package com.itworks.snamp.licensing;

import com.itworks.snamp.core.SupportService;

import java.util.Collection;
import java.util.Locale;

/**
 * Represents a service which supplies information about license limitations of the bundle.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface LicensingDescriptionService extends SupportService {
    /**
     * Gets a read-only collection of license limitations.
     * @return A read-only collection of license limitations.
     */
    Collection<String> getLimitations();

    /**
     * Gets human-readable description of the specified limitation.
     * @param limitationName The system name of the limitation.
     * @param loc The locale of the description. May be {@literal null}.
     * @return The description of the limitation.
     */
    String getDescription(final String limitationName, final Locale loc);
}
