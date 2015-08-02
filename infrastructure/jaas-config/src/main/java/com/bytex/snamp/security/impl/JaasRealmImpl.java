package com.bytex.snamp.security.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import org.apache.karaf.jaas.config.JaasRealm;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JaasRealmImpl implements JaasRealm {
    static final int DEFAULT_RANK = 10;
    static final String FACTORY_PID = "com.bytex.snamp.login.config";
    private static final String RANK_PROPERTY = "rank";
    private static final String REALM_PROPERTY = "realm";
    private static final String ENTRIES_PROPERTY = "entries";

    private final ImmutableList<AppConfigurationEntry> entries;
    private final String realmName;
    private final int rank;

    JaasRealmImpl(final String realmName,
                  final ImmutableList<AppConfigurationEntry> entries,
                  final int rank){
        this.entries = entries;
        this.realmName = realmName;
        this.rank = rank;
    }

    JaasRealmImpl(final String realmName,
                  final AppConfigurationEntry[] entries,
                  final int rank){
        this(realmName, ImmutableList.copyOf(entries), rank);
    }

    JaasRealmImpl(final Gson formatter,
                  final Dictionary<String, ?> persistentConfig) throws IllegalArgumentException {
        this(Utils.getProperty(persistentConfig, REALM_PROPERTY, String.class, ""),
                formatter.fromJson(Utils.getProperty(persistentConfig, ENTRIES_PROPERTY, String.class, "[]"), AppConfigurationEntry[].class),
                Utils.getProperty(persistentConfig, RANK_PROPERTY, Integer.class, 0));
    }

    @Override
    public String getName() {
        return realmName;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public AppConfigurationEntry[] getEntries() {
        return ArrayUtils.toArray(entries, AppConfigurationEntry.class);
    }

    private static String getConfigFilter(){
        return String.format("(%s=%s.*)", Constants.SERVICE_PID, FACTORY_PID);
    }

    static Collection<JaasRealm> getRealms(final Gson formatter, final BundleContext context) throws InvalidSyntaxException, IOException {
        final ServiceHolder<ConfigurationAdmin> adminRef = new ServiceHolder<>(context, ConfigurationAdmin.class);
        try {
            final Configuration[] configs = adminRef.getService().listConfigurations(getConfigFilter());
            if (configs == null || configs.length == 0) return Collections.emptyList();
            final List<JaasRealm> result = Lists.newArrayListWithExpectedSize(configs.length);
            for (final Configuration cfg : configs) {
                final Dictionary<String, ?> properties = cfg.getProperties();
                if (properties != null)
                    result.add(new JaasRealmImpl(formatter, properties));
            }
            return result;
        } finally {
            adminRef.release(context);
        }
    }

    static void deleteAll(final ConfigurationAdmin adminRef) throws InvalidSyntaxException, IOException {
        final Configuration[] configs = adminRef.listConfigurations(getConfigFilter());
        if (configs != null)
            for (final Configuration cfg : configs)
                cfg.delete();
    }

    Dictionary<String, ?> toDictionary(final Gson formatter){
        final Hashtable<String, Object> result = new Hashtable<>(5);
        result.put(RANK_PROPERTY, rank);
        result.put(REALM_PROPERTY, realmName);
        result.put(ENTRIES_PROPERTY, formatter.toJson(getEntries(), AppConfigurationEntry[].class));
        return result;
    }

    private void persist(final Gson formatter, final Configuration output) throws IOException{
        output.update(toDictionary(formatter));
    }

    void persist(final Gson formatter, final ConfigurationAdmin admin) throws IOException{
        persist(formatter, admin.createFactoryConfiguration(FACTORY_PID, null));
    }
}
