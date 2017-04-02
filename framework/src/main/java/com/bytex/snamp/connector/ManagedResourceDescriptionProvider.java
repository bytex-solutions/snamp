package com.bytex.snamp.connector;

import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ThreadPoolResolver;

import java.util.Map;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.SMART_MODE_KEY;

/**
 * Provides parser of connector-related configuration parameters.
 * <p>
 *     Derived class should be placed in the same bundle where connector located.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public interface ManagedResourceDescriptionProvider extends ThreadPoolResolver {
    /**
     * Default value of {@link ManagedResourceConfiguration#SMART_MODE_KEY}
     * configuration property.
     */
    boolean DEFAULT_SMART_MODE_VALUE = false;

    /**
     * Determines whether SmartMode should be enabled for configured connector.
     * @param parameters Configuration parameters of the managed resource. Cannot be {@literal null}.
     * @return {@literal true} if SmartMode={@literal true} in the specified configuration parameters.
     * @see #DEFAULT_SMART_MODE_VALUE
     */
    default boolean isSmartModeEnabled(final Map<String, String> parameters) {
        return getValue(parameters, SMART_MODE_KEY, Boolean::parseBoolean).orElse(DEFAULT_SMART_MODE_VALUE);
    }
}
