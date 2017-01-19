package com.bytex.snamp.configuration;

/**
 * Represents read-only copy of the resource group configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class ImmutableManagedResourceGroupConfiguration extends ImmutableManagedResourceTemplate implements ManagedResourceGroupConfiguration {
    private static final long serialVersionUID = 5850846232096530165L;

    ImmutableManagedResourceGroupConfiguration(final ManagedResourceGroupConfiguration entity) {
        super(entity);
    }

    @Override
    public ImmutableManagedResourceGroupConfiguration asReadOnly() {
        return this;
    }
}
