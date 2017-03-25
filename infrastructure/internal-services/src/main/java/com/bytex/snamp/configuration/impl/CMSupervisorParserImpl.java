package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.internal.CMSupervisorParser;

/**
 * Represents parser of {@link SerializableSupervisorConfiguration}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CMSupervisorParserImpl extends SerializableConfigurationParser<SerializableSupervisorConfiguration> implements CMSupervisorParser {
    private static final String PID = "com.bytex.snamp.watchers";

    CMSupervisorParserImpl() {
        super(PID, SerializableSupervisorConfiguration.class);
    }

    @Override
    public String getPersistentID() {
        return persistentID;
    }
}
