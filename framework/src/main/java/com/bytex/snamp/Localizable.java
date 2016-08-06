package com.bytex.snamp;

import java.util.Locale;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface Localizable {
    /**
     * Returns the localized description of this object.
     * @param locale The locale of the description. If it is {@literal null} then returns description
     *               in the default locale.
     * @return The localized description of this object.
     */
    String toString(final Locale locale);
}
