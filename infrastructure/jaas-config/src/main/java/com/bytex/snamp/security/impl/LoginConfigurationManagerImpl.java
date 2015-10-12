package com.bytex.snamp.security.impl;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.security.LoginConfigurationManager;
import com.bytex.snamp.security.auth.login.json.JsonConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import org.apache.karaf.jaas.config.JaasRealm;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class LoginConfigurationManagerImpl extends AbstractAggregator implements LoginConfigurationManager {
    private static final String LOGGER_NAME = "com.bytex.snamp.login.config";
    private final Gson formatter;

    LoginConfigurationManagerImpl(final Gson formatter){
        this.formatter = Objects.requireNonNull(formatter);
    }

    private BundleContext getContext(){
        return Utils.getBundleContextByObject(this);
    }

    /**
     * Dumps the current configuration into the specified output stream.
     *
     * @param out An output stream that accepts configuration content.
     * @throws java.io.IOException Some I/O problem occurred.
     */
    @Override
    public void dumpConfiguration(final Writer out) throws IOException {
        final JsonConfiguration config = new JsonConfiguration();
        try {
            dumpConfiguration(Utils.getBundleContextByObject(this), formatter, config);
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
        config.serialize(formatter, out);
    }

    private static void dumpConfiguration(final BundleContext context,
                                          final Gson formatter,
                                          final Multimap<String, AppConfigurationEntry> out) throws InvalidSyntaxException, IOException {
        for (final JaasRealm realm : JaasRealmImpl.getRealms(formatter, context)) {
            out.putAll(realm.getName(),
                    ImmutableList.copyOf(realm.getEntries()));
        }
    }

    @Override
    public void dumpConfiguration(final Multimap<String, AppConfigurationEntry> out) {
        try {
            dumpConfiguration(getContext(), formatter, out);
        } catch (final InvalidSyntaxException | IOException e) {
            getLogger().log(Level.SEVERE, "Unable to dump JAAS configuration", e);
        }
    }

    /**
     * Setup empty configuration.
     *
     * @throws IOException Unable to setup configuration.
     */
    @Override
    public void resetConfiguration() throws IOException {
        final BundleContext context = getContext();
        final ServiceHolder<ConfigurationAdmin> adminRef = new ServiceHolder<>(context,
                ConfigurationAdmin.class);
        try{
            JaasRealmImpl.deleteAll(adminRef.getService());
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        } finally {
            adminRef.release(context);
        }
    }

    /**
     * Reloads JAAS configuration from the specified stream.
     *
     * @param in An input stream containing the configuration. Cannot be {@literal null}.
     * @throws java.io.IOException Some I/O problem occurred.
     */
    @Override
    public void loadConfiguration(final Reader in) throws IOException {
        final JsonConfiguration config = JsonConfiguration.deserialize(formatter, in);
        final BundleContext context = getContext();
        final ServiceHolder<ConfigurationAdmin> adminRef = new ServiceHolder<>(context,
                ConfigurationAdmin.class);
        try {
            //delete existing realms
            JaasRealmImpl.deleteAll(adminRef.getService());
            //register a new realms
            for (final String realmName : config.keySet()) {
                final AppConfigurationEntry[] entries = config.getAppConfigurationEntry(realmName);
                final JaasRealmImpl realm = new JaasRealmImpl(realmName, entries, JaasRealmImpl.DEFAULT_RANK);
                realm.persist(formatter, adminRef.getService());
            }
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        } finally {
            adminRef.release(context);
        }
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    @Aggregation
    public Logger getLogger() {
        return Logger.getLogger(LOGGER_NAME);
    }
}
