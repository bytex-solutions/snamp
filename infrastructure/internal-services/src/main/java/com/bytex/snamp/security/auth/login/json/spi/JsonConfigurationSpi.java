package com.bytex.snamp.security.auth.login.json.spi;

import com.bytex.snamp.core.LogicalOperation;
import com.bytex.snamp.security.auth.login.json.JsonConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.ConfigurationSpi;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class JsonConfigurationSpi extends ConfigurationSpi {
    private static final String LOGGER_NAME = "com.bytex.snamp.login.config.spi";
    private static final class RefreshEngineLogicalOperation extends LogicalOperation{
        private static final CorrelationIdentifierGenerator CORREL_ID_GEN =
                new DefaultCorrelationIdentifierGenerator();

        private RefreshEngineLogicalOperation(){
            super(Logger.getLogger(LOGGER_NAME), "engineRefresh", CORREL_ID_GEN);
        }
    }

    public static GsonBuilder init(final GsonBuilder builder) {
        return builder.registerTypeAdapter(JsonConfiguration.class, new JsonConfigurationSerializerDeserializer())
                .registerTypeAdapter(AppConfigurationEntry.class, new AppConfigurationEntrySerializerDeserializer())
                .registerTypeAdapter(AppConfigurationEntry.LoginModuleControlFlag.class, new LoginModuleControlFlagSerializerDeserializer());
    }

    private static JsonConfiguration deserializePrivileged(final Gson formatter, final URL configFile) throws Exception {
        return AccessController.doPrivileged((PrivilegedExceptionAction<JsonConfiguration>) () -> JsonConfiguration.deserialize(formatter, configFile));
    }

    private final URL configFile;
    private JsonConfiguration configuration;
    private final Gson formatter;

    public JsonConfigurationSpi(final URL configFile) throws Exception{
        this.configFile = Objects.requireNonNull(configFile, "configFile is null.");
        formatter = init(new GsonBuilder()).create();
        configuration = deserializePrivileged(formatter, configFile);
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
        final LogicalOperation logger = new RefreshEngineLogicalOperation();
        try {
            configuration = deserializePrivileged(formatter, configFile);
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Unable to reload JAAS configuration from file %s", configFile, e);
        } finally {
            logger.close();
        }
    }
}
