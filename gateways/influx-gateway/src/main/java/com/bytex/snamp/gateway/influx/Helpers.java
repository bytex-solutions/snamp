package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;

import javax.management.InstanceNotFoundException;
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

    static Map<String, String> extractTags(final BundleContext context, final String resourceName) throws InstanceNotFoundException {
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, resourceName);
        try{
            return client.getProperties((k, v) -> v instanceof String, Objects::toString);
        } finally {
            client.release(context);
        }
    }

    static Map<String, Object> toScalar(final Object value){
        final String VALUE_FIELD = "value";
        return ImmutableMap.of(VALUE_FIELD, value);
    }
}
