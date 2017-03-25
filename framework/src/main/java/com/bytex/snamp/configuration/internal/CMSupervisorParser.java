package com.bytex.snamp.configuration.internal;

import com.bytex.snamp.configuration.SupervisorConfiguration;

import java.util.Dictionary;

/**
 * Represents parser of {@link SupervisorConfiguration}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface CMSupervisorParser extends CMRootEntityParser<SupervisorConfiguration> {
    String getGroupName(final Dictionary<String, ?> configuration);
}
