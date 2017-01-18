package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import com.bytex.snamp.configuration.OperationConfiguration;

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

    @SpecialUse
    public SerializableManagedResourceGroupConfiguration(){
    }

    private boolean equals(final ManagedResourceGroupConfiguration other){
        return getAttributes().equals(other.getFeatures(AttributeConfiguration.class)) &&
                getEvents().equals(other.getFeatures(EventConfiguration.class)) &&
                getOperations().equals(other.getFeatures(OperationConfiguration.class)) &&
                getType().equals(other.getType()) &&
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
