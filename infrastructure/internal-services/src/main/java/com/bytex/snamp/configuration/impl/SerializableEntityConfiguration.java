package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.EntityConfiguration;

import java.io.Externalizable;

/**
 * Represents serializable configuration entity.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.0
 */
interface SerializableEntityConfiguration extends EntityConfiguration, Modifiable, Externalizable {
    /**
     * Determines whether this configuration entity is modified after deserialization.
     * @return {@literal true}, if this configuration entity is modified; otherwise, {@literal false}.
     */
    @Override
    boolean isModified();
}
