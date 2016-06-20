package com.bytex.snamp.configuration.internal;


import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Dictionary;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;

/**
 * Provides parsing of managed resource configuration from data provided by {@link org.osgi.service.cm.Configuration}.
 * <p>
 *     This interface is intended to use from your code directly. Any future release of SNAMP may change
 *     configuration storage provided and this interface will be deprecated.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public interface CMManagedResourceParser extends CMConfigurationParser<ManagedResourceConfiguration> {
    /**
     * Returns managed connector persistent identifier.
     * @param connectorType The type of the managed resource connector.
     * @return The persistent identifier.
     */
    String getConnectorFactoryPersistentID(final String connectorType);

    static String getConnectorFactoryPersistentID(final BundleContext context, final String connectorType) {
        return CMConfigurationParser.withParser(context,
                CMManagedResourceParser.class,
                parser -> parser.getConnectorFactoryPersistentID(connectorType));
    }

    /**
     * Extracts resource connection string from the managed resource configuration.
     * @param resourceConfig A dictionary that represents managed resource configuration.
     * @return Resource connection string.
     */
    String getConnectionString(final Dictionary<String, ?> resourceConfig);

    static String getConnectionString(final BundleContext context, final Dictionary<String, ?> resourceConfig){
        return CMConfigurationParser.withParser(context,
                CMManagedResourceParser.class,
                parser -> parser.getConnectionString(resourceConfig));
    }

    /**
     * Extracts resource name from the managed resource configuration.
     * @param resourceConfig A dictionary that represents managed resource configuration.
     * @return The resource name.
     */
    String getResourceName(final Dictionary<String, ?> resourceConfig);

    static String getResourceName(final BundleContext context, final Dictionary<String, ?> resourceConfig) {
        return CMConfigurationParser.withParser(context,
                CMManagedResourceParser.class,
                parser -> parser.getResourceName(resourceConfig));
    }

    Map<String, String> getResourceConnectorParameters(final Dictionary<String, ?> resourceConfig);

    static Map<String, String> getResourceConnectorParameters(final BundleContext context, final Dictionary<String, ?> resourceConfig) {
        return CMConfigurationParser.withParser(context,
                CMManagedResourceParser.class,
                parser -> parser.getResourceConnectorParameters(resourceConfig));
    }

    Map<String, ? extends AttributeConfiguration> getAttributes(final Dictionary<String, ?> resourceConfig) throws IOException;

    static Map<String, ? extends AttributeConfiguration> getAttributes(final BundleContext context, final Dictionary<String, ?> resourceConfig) {
        return CMConfigurationParser.withParser(context,
                CMManagedResourceParser.class,
                parser -> {
                    try {
                        return parser.getAttributes(resourceConfig);
                    } catch (final IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    Map<String, ? extends OperationConfiguration> getOperations(final Dictionary<String, ?> resourceConfig) throws IOException;

    static Map<String, ? extends OperationConfiguration> getOperations(final BundleContext context, final Dictionary<String, ?> resourceConfig) {
        return CMConfigurationParser.withParser(context,
                CMManagedResourceParser.class,
                parser -> {
                    try {
                        return parser.getOperations(resourceConfig);
                    } catch (final IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    Map<String, ? extends EventConfiguration> getEvents(final Dictionary<String, ?> resourceConfig) throws IOException;

    static Map<String, ? extends EventConfiguration> getEvents(final BundleContext context, final Dictionary<String, ?> resourceConfig) {
        return CMConfigurationParser.withParser(context,
                CMManagedResourceParser.class,
                parser -> {
                    try {
                        return parser.getEvents(resourceConfig);
                    } catch (final IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }
}
