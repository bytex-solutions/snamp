package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.EntityConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface EntityConfigurationPatch<E extends EntityConfiguration> extends ConfigurationPatch {
    String getEntityID();
    E getEntity();
}
