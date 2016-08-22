package com.bytex.snamp.connector.composite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ConnectionParametersMap implements Function<String, Map<String, String>>, ConnectorTypeSplit {
    private final Map<String, Map<String, String>> parameters;

    private ConnectionParametersMap(){
        parameters = new HashMap<>();
    }

    private void add(final String connectorType, final String paramName, final String paramValue){
        final Map<String, String> connectorParams;
        if(parameters.containsKey(connectorType))
            connectorParams = parameters.get(connectorType);
        else
            parameters.put(connectorType, connectorParams = new HashMap<>());
        connectorParams.put(paramName, paramValue);
    }

    private boolean add(final String paramName, final String paramValue) {
        return ConnectorTypeSplit.split(paramName, (connectorType, name) -> add(connectorType, name, paramValue));
    }

    static ConnectionParametersMap parse(final Map<String, String> parameters) {
        final ConnectionParametersMap result = new ConnectionParametersMap();
        parameters.forEach(result::add);
        return result;
    }

    @Override
    public Map<String, String> apply(final String connectorType) {
        final Map<String, String> result = parameters.get(connectorType);
        return firstNonNull(result, Collections.emptyMap());
    }
}
