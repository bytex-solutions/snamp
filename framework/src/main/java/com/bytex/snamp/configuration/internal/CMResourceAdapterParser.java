package com.bytex.snamp.configuration.internal;

import org.osgi.framework.BundleContext;

import java.util.Dictionary;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * Provides parsing of resource adapter configuration from data provided by {@link org.osgi.service.cm.Configuration}.
 * <p>
 *     This interface is intended to use from your code directly. Any future release of SNAMP may change
 *     configuration storage provided and this interface will be deprecated.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public interface CMResourceAdapterParser extends CMConfigurationParser<ResourceAdapterConfiguration> {
    /**
     * Returns persistent identifier of the specified resource adapter.
     * @param adapterType The name of the adapter instance.
     * @return Persistent identifier.
     */
    String getAdapterFactoryPersistentID(final String adapterType);

    static String getAdapterFactoryPersistentID(final BundleContext context, final String adapterType) {
        return CMConfigurationParser.withParser(context,
                CMResourceAdapterParser.class,
                parser -> parser.getAdapterFactoryPersistentID(adapterType));
    }

    /**
     * Extracts the name of the adapter instance from its configuration.
     * @param adapterConfig The adapter instance configuration supplied by {@link org.osgi.service.cm.Configuration} object.
     * @return Adapter instance name.
     */
    String getAdapterInstanceName(final Dictionary<String, ?> adapterConfig);

    static String getAdapterInstanceName(final BundleContext context, final Dictionary<String, ?> adapterConfig) {
        return CMConfigurationParser.withParser(context,
                CMResourceAdapterParser.class,
                parser -> parser.getAdapterInstanceName(adapterConfig));
    }

    /**
     * Extracts configuration parameters of resource adapter.
     * @param adapterConfig A collection of configuration parameters supplied by {@link org.osgi.service.cm.Configuration} object.
     * @return Configuration parameters of resource adapter.
     */
    Map<String, String> getAdapterParameters(final Dictionary<String, ?> adapterConfig);

    static Map<String, String> getAdapterParameters(final BundleContext context, final Dictionary<String, ?> adapterConfig) {
        return CMConfigurationParser.withParser(context,
                CMResourceAdapterParser.class,
                parser -> parser.getAdapterParameters(adapterConfig));
    }
}
