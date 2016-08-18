package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents parser of SNAMP global configuration parameters.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CMAgentParserImpl {
    private static final String PID = "com.bytex.snamp.configuration";

    private static Configuration getConfig(final ConfigurationAdmin admin) throws IOException {
        return admin.getConfiguration(PID);
    }

    static void saveParameters(final ConfigurationAdmin admin, final EntityConfiguration agentConfig) throws IOException {
        final Configuration conf = getConfig(admin);
        conf.update(new Hashtable<>(agentConfig.getParameters()));
    }

    static void loadParameters(final ConfigurationAdmin admin, final EntityConfiguration agentConfig) throws IOException{
        final Configuration conf = getConfig(admin);
        if(conf != null) {
            final Iterator<String> keys = Iterators.forEnumeration(conf.getProperties().keys());
            final Map<String, String> params = Maps.toMap(keys, key -> Utils.getProperty(conf.getProperties(), key, String.class, ""));
            agentConfig.setParameters(params);
        }
    }
}
