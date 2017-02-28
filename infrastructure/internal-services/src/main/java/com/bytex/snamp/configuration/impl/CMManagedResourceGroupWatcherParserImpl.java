package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.internal.CMManagedResourceGroupWatcherParser;

/**
 * Represents parser of {@link SerializableManagedResourceGroupWatcherConfiguration}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CMManagedResourceGroupWatcherParserImpl extends SerializableConfigurationParser<SerializableManagedResourceGroupWatcherConfiguration> implements CMManagedResourceGroupWatcherParser {
    private static final String PID = "com.bytex.snamp.watchers";

    CMManagedResourceGroupWatcherParserImpl() {
        super(PID, SerializableManagedResourceGroupWatcherConfiguration.class);
    }

    @Override
    public String getPersistentID() {
        return persistentID;
    }
}
