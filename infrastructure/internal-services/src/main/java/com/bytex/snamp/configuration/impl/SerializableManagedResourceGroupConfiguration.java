package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;

import java.util.Objects;

/**
 * Represents serializable version of managed resource group.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SerializableManagedResourceGroupConfiguration extends AbstractManagedResourceTemplate implements ManagedResourceGroupConfiguration {
    private static final long serialVersionUID = 9050126733283251808L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableManagedResourceGroupConfiguration(){
    }

    @Override
    public void fillResourceConfig(final ManagedResourceConfiguration resource) {
        //overwrite all properties in resource but hold user-defined properties
        this.forEach(resource::putIfAbsent);
        //overwrite all attributes
        resource.getAttributes().putAll(getAttributes());
        //overwrite all events
        resource.getEvents().putAll(getEvents());
        //overwrite all operations
        resource.getOperations().putAll(getOperations());
        //overwrite connector type
        resource.setType(getType());
    }

    private boolean equals(final ManagedResourceGroupConfiguration other) {
        return other.getAttributes().equals(getAttributes()) &&
                other.getEvents().equals(getEvents()) &&
                other.getOperations().equals(getOperations()) &&
                Objects.equals(other.getType(), getType()) &&
                super.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ManagedResourceGroupConfiguration && equals((ManagedResourceGroupConfiguration)other);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(getType(), getAttributes(), getEvents(), getOperations());
    }
}
