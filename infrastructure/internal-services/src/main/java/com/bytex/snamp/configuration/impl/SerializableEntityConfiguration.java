package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.EntityConfiguration;

import java.io.Externalizable;

/**
 * Represents serializable configuration entity.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.1
 */
interface SerializableEntityConfiguration extends EntityConfiguration, Modifiable, Externalizable, Stateful {
}
