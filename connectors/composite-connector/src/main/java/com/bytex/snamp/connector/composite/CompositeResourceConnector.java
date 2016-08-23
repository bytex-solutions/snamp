package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.metrics.MetricsReader;
import com.bytex.snamp.internal.Utils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents resource connector that can combine many resource connectors.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeResourceConnector extends AbstractManagedResourceConnector {
    private static final Pattern CONNECTION_STRING_PATTERN = Pattern.compile("(?<connectorType>[a-z]+)\\s*:=\\s*(?<connectionString>.+)", Pattern.CASE_INSENSITIVE);
    private static final Function<String, Pattern> CONNECTION_STRING_SPLITTER_CACHE = CacheBuilder.newBuilder()
            .softValues()
            .maximumSize(50)
            .build(new CacheLoader<String, Pattern>() {
                @Override
                public Pattern load(final String splitter) throws PatternSyntaxException {
                    return Pattern.compile(splitter);
                }
            })::getUnchecked;

    private final Composition connectors;
    @Aggregation(cached = true)
    private final AttributeComposition attributes;
    @Aggregation(cached = true)
    private final NotificationComposition notifications;
    @Aggregation(cached = true)
    private final OperationComposition operations;

    CompositeResourceConnector(final String resourceName, final ExecutorService threadPool) {
        connectors = new Composition(resourceName);
        attributes = new AttributeComposition(resourceName, connectors, getLogger());
        notifications = new NotificationComposition(resourceName, connectors, threadPool, getLogger(), Utils.getBundleContextOfObject(this));
        operations = new OperationComposition(resourceName, connectors, getLogger());
    }

    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes, notifications, operations);
    }

    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, notifications, operations);
    }

    private void update(final Map<String, String> connectionStrings, final Function<String, ? extends Map<String, String>> connectionParameters) throws Exception{
        //update supplied connectors
        Acceptor.forEachAccept(connectionStrings.entrySet(), entry -> connectors.updateConnector(entry.getKey(), entry.getValue(), connectionParameters.apply(entry.getKey())));
        //dispose connectors that are not specified in the connection string
        connectors.retainConnectors(connectionStrings.keySet());
    }

    @Override
    public void update(final String connectionString, final Map<String, String> connectionParameters) throws Exception {
        final CompositeResourceConfigurationDescriptor parser = CompositeResourceConfigurationDescriptor.getInstance();
        //extract connection strings of each connector
        final Map<String, String> connectionStrings = parseConnectionString(connectionString, parser.parseSplitter(connectionParameters));
        //extract connection parameters for each connector
        final ConnectionParametersMap parsedParams = ConnectionParametersMap.parse(connectionParameters);
        //do update
        update(connectionStrings, parsedParams);
    }

    @Override
    protected MetricsReader createMetricsReader() {
        return null;
    }

    private static Map<String, String> parseConnectionString(final String connectionString, final Pattern splitter) {
        return splitter.splitAsStream(connectionString)
                .map(String::trim)
                .map(CONNECTION_STRING_PATTERN::matcher)
                .filter(Matcher::matches)
                .collect(HashMap::new, (result, match) -> result.put(match.group("connectorType"), match.group("connectionString")), Map::putAll);
    }

    /**
     * Decompose connection string to the connection strings for each connector.
     * Example: jmx:=service:jmx:rmi:///jndi/rmi://localhost:5657/karaf-root; snmp:=192.168.0.1
     * @param connectionString Connection string to parse.
     * @return A map with connection types and connection strings.
     */
    static Map<String, String> parseConnectionString(final String connectionString, final String splitter){
        return parseConnectionString(connectionString, CONNECTION_STRING_SPLITTER_CACHE.apply(splitter));
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        connectors.close();
        attributes.close();
        notifications.close();
        operations.close();
    }
}
