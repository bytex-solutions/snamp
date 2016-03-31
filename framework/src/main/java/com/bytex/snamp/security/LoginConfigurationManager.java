package com.bytex.snamp.security;

import com.bytex.snamp.core.SupportService;
import com.google.common.collect.Multimap;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Represents supporting service provided by SNAMP to configure JAAS login modules.
 * <p>
 *     This manager can be used as an alternative to standard {@link javax.security.auth.login.Configuration#getConfiguration()}
 *     accessor.
 * </p>
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface LoginConfigurationManager extends SupportService {
    /**
     * Represents name of the service property that contains MIME type of the underlying configuration.
     */
    String CONFIGURATION_MIME_TYPE = "configurationMimeType";

    /**
     * Dumps the current configuration into the specified output stream.
     * @param out An output stream that accepts configuration content.
     * @throws IOException Some I/O problem occurred.
     */
    void dumpConfiguration(final Writer out) throws IOException;

    void dumpConfiguration(final Multimap<String, AppConfigurationEntry> out);

    /**
     * Reloads JAAS configuration from the specified stream.
     * @param in An input stream containing the configuration. Cannot be {@literal null}.
     * @throws IOException Some I/O problem occurred.
     */
    void loadConfiguration(final Reader in) throws IOException;

    /**
     * Setup empty configuration.
     * @throws IOException Unable to setup configuration.
     */
    void resetConfiguration() throws IOException;
}
