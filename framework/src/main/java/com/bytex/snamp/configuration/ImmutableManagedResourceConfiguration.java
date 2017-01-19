package com.bytex.snamp.configuration;

/**
 * Represents read-only copy of the resource configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class ImmutableManagedResourceConfiguration extends ImmutableManagedResourceTemplate implements ManagedResourceConfiguration {
    private static final long serialVersionUID = 6387103488335571447L;
    private final String connectionString;

    ImmutableManagedResourceConfiguration(final ManagedResourceConfiguration entity) {
        super(entity);
        connectionString = entity.getConnectionString();
    }

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    @Override
    public void setConnectionString(final String connectionString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGroupName(final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableManagedResourceConfiguration asReadOnly() {
        return this;
    }
}
