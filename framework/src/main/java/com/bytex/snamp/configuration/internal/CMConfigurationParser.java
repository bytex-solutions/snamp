package com.bytex.snamp.configuration.internal;
import org.osgi.service.cm.Configuration;

import java.io.IOException;

import com.bytex.snamp.configuration.EntityConfiguration;

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
     * Converts {@link Configuration} into SNAMP-specific configuration section.
     * @param config Configuration to convert.
     * @return Converted SNAMP configuration section.
     * @throws IOException Unable to parse persistent configuration.
     */
    E parse(final Configuration config) throws IOException;

    void serialize(final E input, final Configuration output) throws IOException;
}
