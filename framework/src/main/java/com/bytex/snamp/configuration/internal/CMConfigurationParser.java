package com.bytex.snamp.configuration.internal;

import com.bytex.snamp.configuration.EntityConfiguration;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;

/**
 * Provides parsing of SNAMP configuration from data provided by {@link org.osgi.service.cm.Configuration}.
 * <p>
 *     This interface is intended to use from your code directly. Any future release of SNAMP may change
 *     configuration storage provided and this interface will be deprecated.
 * @param <E> Type of configuration section.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public interface CMConfigurationParser<E extends EntityConfiguration> {
    /**
     * Converts {@link Dictionary} into SNAMP-specific configuration section.
     * @param config Configuration to convert.
     * @return Converted SNAMP configuration section.
     * @throws IOException Unable to parse persistent configuration.
     */
    Map<String, ? extends E> parse(final Dictionary<String, ?> config) throws IOException;
}
