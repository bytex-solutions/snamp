package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.HTTPDigestAuthFilter;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ActuatorConnectorDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    private static final String AUTHENTICATION_PARAM = "authentication";
    private static final String USER_NAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";
    private static final String METRICS_PATH_PARAM = "metricsPath";
    private static final String HEALTH_PATH_PARAM = "healthPath";
    private static final LazySoftReference<ActuatorConnectorDescriptionProvider> INSTANCE = new LazySoftReference<>();

    private ActuatorConnectorDescriptionProvider(){

    }

    static ActuatorConnectorDescriptionProvider getInstance(){
        return INSTANCE.lazyGet(ActuatorConnectorDescriptionProvider::new);
    }

    ClientFilter parseAuthentication(final Map<String, String> parameters) {
        if (parameters.containsKey(USER_NAME_PARAM) && parameters.containsKey(PASSWORD_PARAM)) {
            final String userName = parameters.get(USER_NAME_PARAM);
            final String password = parameters.get(PASSWORD_PARAM);
            switch (parameters.getOrDefault(AUTHENTICATION_PARAM, "basic")) {
                case "DIGEST":
                case "digest":
                    return new HTTPDigestAuthFilter(userName, password);
                case "basic":
                case "BASIC":
                    return new HTTPBasicAuthFilter(userName, password);
            }
        }
        return null;
    }

    String getMetricsPath(final Map<String, String> parameters){
        return parameters.getOrDefault(METRICS_PATH_PARAM, "metrics.json");
    }

    String getHealthPath(final Map<String, String> parameters){
        return parameters.getOrDefault(HEALTH_PATH_PARAM, "health.json");
    }
}
