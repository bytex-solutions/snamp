package com.bytex.snamp.configuration.internal;

import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;

import java.io.IOException;
import java.util.Dictionary;

/**
 * Provides parser of {@link ManagedResourceGroupConfiguration}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface CMManagedResourceGroupParser extends CMConfigurationParser<ManagedResourceGroupConfiguration> {
    /**
     * Returns persistent identifier of the supervisor.
     * @param supervisorType Type of the group supervisor.
     * @return The persistent identifier.
     */
    String getFactoryPersistentID(final String supervisorType);

    String getGroupName(final Dictionary<String, ?> configuration);

    @Override
    SingletonMap<String, ? extends ManagedResourceGroupConfiguration> parse(final Dictionary<String, ?> config) throws IOException;
}
