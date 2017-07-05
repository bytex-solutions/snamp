package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class DefaultManagedResourceGroupParser extends SerializableConfigurationParser<SerializableManagedResourceGroupConfiguration> {
    private static final String PID = "com.bytex.snamp.resourceGroups";

    private static final LazyReference<DefaultManagedResourceGroupParser> INSTANCE = LazyReference.soft();

    private DefaultManagedResourceGroupParser() {
        super(SerializableAgentConfiguration::getResourceGroups, PID, SerializableManagedResourceGroupConfiguration.class, ManagedResourceConfiguration.GROUP_NAME_PROPERTY);
    }

    static DefaultManagedResourceGroupParser getInstance(){
        return INSTANCE.lazyGet(DefaultManagedResourceGroupParser::new);
    }
}
