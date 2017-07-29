package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityConfiguration;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.MapUtils.getValue;

/**
 * Represents parser of SNAMP global configuration parameters.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class DefaultAgentParser {
    private static final String PID = "com.bytex.snamp.configuration";

    private DefaultAgentParser(){
        throw new InstantiationError();
    }

    private static Configuration getConfig(final ConfigurationAdmin admin) throws IOException {
        return admin.getConfiguration(PID);
    }

    static void saveParameters(final ConfigurationAdmin admin, final EntityConfiguration agentConfig) throws IOException {
        final Configuration conf = getConfig(admin);
        conf.update(new Hashtable<>(agentConfig));
    }

    private static boolean isValidParameter(final String key) {
        switch (key) {
            case Constants.SERVICE_PID:
                return false;
            default:
                return true;
        }
    }

    static void loadParameters(final ConfigurationAdmin admin, final AgentConfiguration agentConfig) throws IOException {
        final Configuration conf = getConfig(admin);
        if (conf.getProperties() != null) {
            Iterator<String> keys = Iterators.filter(Iterators.forEnumeration(conf.getProperties().keys()), DefaultAgentParser::isValidParameter);
            final Map<String, String> params = Maps.toMap(keys, key -> getValue(conf.getProperties(), key, Objects::toString).orElse(""));
            agentConfig.load(params);
        }
    }
}
