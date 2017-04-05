package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.concurrent.LazySoftReference;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class DefaultManagedResourceGroupParser extends SerializableConfigurationParser<SerializableManagedResourceGroupConfiguration> {
    private static final String PID = "com.bytex.snamp.resourceGroups";

    private static final LazySoftReference<DefaultManagedResourceGroupParser> INSTANCE = new LazySoftReference<>();

    private DefaultManagedResourceGroupParser() {
        super(SerializableAgentConfiguration::getResourceGroups, PID, SerializableManagedResourceGroupConfiguration.class);
    }

    static DefaultManagedResourceGroupParser getInstance(){
        return INSTANCE.lazyGet(DefaultManagedResourceGroupParser::new);
    }
}
