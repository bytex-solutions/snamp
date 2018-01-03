package com.bytex.snamp.connector.composite;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class ComposedConfiguration {
    private static final Function<String, Pattern> CONNECTION_STRING_SPLITTER_CACHE = CacheBuilder.newBuilder()
            .softValues()
            .maximumSize(50)
            .build(new CacheLoader<String, Pattern>() {
                @Override
                public Pattern load(@Nonnull final String splitter) throws PatternSyntaxException {
                    return Pattern.compile(splitter);
                }
            })::getUnchecked;
    private static final Pattern PREFIXED_FRAGMENT_PATTERN = Pattern.compile("(?<connectorType>[a-z]+)\\s*:\\s*(?<fragment>.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONNECTION_STRING_PATTERN = Pattern.compile("(?<connectorType>[a-z]+)\\s*:=\\s*(?<connectionString>.+)", Pattern.CASE_INSENSITIVE);

    private final Pattern splitter;
    private final Map<String, String> connectionStrings;
    private final Map<String, Map<String, String>> parameters;

    ComposedConfiguration(final String connectionStringSplitter){
        splitter = CONNECTION_STRING_SPLITTER_CACHE.apply(connectionStringSplitter);
        connectionStrings = new HashMap<>();
        parameters = new HashMap<>();
    }

    Set<String> getConnectorTypes(){
        return connectionStrings.keySet();
    }

    String getConnectionString(final String connectorType){
        return connectionStrings.get(connectorType);
    }

    Map<String, String> getParameters(final String connectorType){
        return parameters.getOrDefault(connectorType, Collections.emptyMap());
    }

    private void add(final String connectorType, final String paramName, final String paramValue) {
        if (parameters.containsKey(connectorType))
            parameters.get(connectorType).put(paramName, paramValue);
    }

    private boolean add(final String paramName, final String paramValue) {
        return split(paramName, (connectorType, name) -> add(connectorType, name, paramValue));
    }

    private static boolean split(final CharSequence input, final BiConsumer<String, String> acceptor){
        final Matcher result = PREFIXED_FRAGMENT_PATTERN.matcher(input);
        final boolean matches;
        if(matches = result.matches())
            acceptor.accept(result.group("connectorType"), result.group("fragment"));
        return matches;
    }

    void parse(final String connectionString, final Map<String, String> parameters) {
        //parse connection strings
        for (String subString : splitter.split(connectionString)) {
            final Matcher match = CONNECTION_STRING_PATTERN.matcher(connectionString.trim());
            if (match.matches()) {
                final String connectorType = match.group("connectorType");
                subString = match.group("connectionString");
                connectionStrings.put(connectorType, subString);
                this.parameters.put(connectorType, new HashMap<>());
            }
        }
        parameters.forEach(this::add);
    }
}
