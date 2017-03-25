package com.bytex.snamp.configuration.internal;

import com.bytex.snamp.configuration.SupervisorConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface CMSupervisorParser extends CMConfigurationParser<SupervisorConfiguration> {
    String getPersistentID();
}
