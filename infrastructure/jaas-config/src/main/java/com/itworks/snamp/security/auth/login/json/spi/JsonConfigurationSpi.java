package com.itworks.snamp.security.auth.login.json.spi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.security.auth.login.json.JsonConfiguration;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.ConfigurationSpi;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.URIParameter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.security.auth.login.Configuration.Parameters;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JsonConfigurationSpi extends ConfigurationSpi {
    private static final String LOGGER_NAME = "com.itworks.snamp.login.config.spi";

    public static GsonBuilder init(final GsonBuilder builder) {
        return builder.registerTypeAdapter(JsonConfiguration.class, new JsonConfigurationSerializerDeserializer())
                .registerTypeAdapter(AppConfigurationEntry.class, new AppConfigurationEntrySerializerDeserializer())
                .registerTypeAdapter(AppConfigurationEntry.LoginModuleControlFlag.class, new LoginModuleControlFlagSerializerDeserializer());
    }

    private static JsonConfiguration deserializePrivileged(final Gson formatter, final URL configFile) throws Exception {
        return AccessController.doPrivileged(new PrivilegedExceptionAction<JsonConfiguration>() {
            @Override
            public JsonConfiguration run() throws IOException {
                return JsonConfiguration.deserialize(formatter, configFile);
            }
        });
    }

    private final URL configFile;
    private JsonConfiguration configuration;
    private final Gson formatter;

    public JsonConfigurationSpi(final URL configFile) throws Exception{
        this.configFile = Objects.requireNonNull(configFile, "configFile is null.");
        formatter = init(new GsonBuilder()).create();
        configuration = deserializePrivileged(formatter, configFile);
    }

    private static URL toURL(final Parameters params) throws Exception{
        if (params instanceof URIParameter)
            return  ((URIParameter) params).getURI().toURL();
        else throw new IllegalArgumentException("Unrecognized param " + params);
    }

    public JsonConfigurationSpi(final Parameters params) throws Exception {
        this(toURL(params));
    }

    /**
     * Retrieve the AppConfigurationEntries for the specified <i>name</i>.
     * <p/>
     * <p/>
     *
     * @param name the name used to index the Configuration.
     * @return an array of AppConfigurationEntries for the specified
     * <i>name</i>, or null if there are no entries.
     */
    @Override
    protected AppConfigurationEntry[] engineGetAppConfigurationEntry(final String name) {
        return configuration.getAppConfigurationEntry(name);
    }

    /**
     * Refresh and reload the Configuration.
     * <p/>
     * <p> This method causes this Configuration object to refresh/reload its
     * contents in an implementation-dependent manner.
     * For example, if this Configuration object stores its entries in a file,
     * calling <code>refresh</code> may cause the file to be re-read.
     * <p/>
     * <p> The default implementation of this method does nothing.
     * This method should be overridden if a refresh operation is supported
     * by the implementation.
     *
     * @throws SecurityException if the caller does not have permission
     *                           to refresh its Configuration.
     */
    @Override
    protected void engineRefresh() {
        try {
            configuration = deserializePrivileged(formatter, configFile);
        } catch (final Exception e) {
            OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    logger.log(Level.SEVERE, String.format("Unable to reload JAAS configuration from file %s", configFile), e);
                }
            });
        }
    }
}