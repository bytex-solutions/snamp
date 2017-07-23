package com.bytex.snamp.connector.composite;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.configuration.ThreadPoolConfigurationSupport;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ComposedConfiguration extends HashMap<String, ManagedResourceInfo> {
    private static final class ComposedManagedResourceInfo extends HashMap<String, String> implements ManagedResourceInfo, ThreadPoolConfigurationSupport {
        private static final long serialVersionUID = -3185159768725211394L;
        private final String connectionString;
        private final String groupName;

        private ComposedManagedResourceInfo(final String connectionString,
                                            final String groupName) {
            this.connectionString = nullToEmpty(connectionString);
            this.groupName = nullToEmpty(groupName);
        }

        @Override
        public String getConnectionString() {
            return connectionString;
        }

        @Override
        public String getGroupName() {
            return groupName;
        }

        private boolean equals(final ManagedResourceInfo other){
            return Objects.equals(connectionString, other.getConnectionString()) && super.equals(other);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof ManagedResourceInfo && equals((ManagedResourceInfo) other);
        }

        @Override
        public int hashCode() {
            return super.hashCode() ^ connectionString.hashCode();
        }
    }

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
    private static final long serialVersionUID = 3080996600579886847L;

    private ComposedConfiguration(){
    }

    private void add(final String connectorType, final String paramName, final String paramValue) {
        if (containsKey(connectorType))
            get(connectorType).put(paramName, paramValue);
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

    private void fill(final ManagedResourceInfo parameters, final Pattern splitter) {
        //parse connection strings
        for (String connectionString : splitter.split(parameters.getConnectionString())) {
            final Matcher match = CONNECTION_STRING_PATTERN.matcher(connectionString.trim());
            if (match.matches()) {
                final String connectorType = match.group("connectorType");
                connectionString = match.group("connectionString");
                put(connectorType, new ComposedManagedResourceInfo(connectionString, parameters.getGroupName()));
            }
        }
        //parse parameters of each managed resource connector
        parameters.forEach(this::add);
    }

    static ComposedConfiguration parse(final ManagedResourceInfo parameters, final String splitter) {
        final ComposedConfiguration result = new ComposedConfiguration();
        result.fill(parameters, CONNECTION_STRING_SPLITTER_CACHE.apply(splitter));
        return result;
    }

    static ManagedResourceInfo createResourceInfo(final String connectionString,
                                                  final String groupName){
        return new ComposedManagedResourceInfo(connectionString, groupName);
    }
}
