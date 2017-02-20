package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class Helpers {
    private Helpers(){
        throw new InstantiationError();
    }

    private static boolean propertyFilter(final String property, final Object value){
        switch (property){
            case Constants.SERVICE_PID:
            case ConfigurationAdmin.SERVICE_FACTORYPID:
            case ConfigurationAdmin.SERVICE_BUNDLELOCATION:
                return false;
            default:
                return value instanceof String;
        }
    }

    static Map<String, String> extractTags(final BundleContext context, final String resourceName) {
        final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(context, resourceName);
        if (client == null)
            return new HashMap<>();
        else
            try {
                return client.getProperties(Helpers::propertyFilter, Objects::toString);
            } finally {
                client.release(context);
            }
    }

    static Map<String, Object> toScalar(final Object value){
        final String VALUE_FIELD = "value";
        return ImmutableMap.of(VALUE_FIELD, value);
    }
}
