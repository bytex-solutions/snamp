package com.bytex.snamp.configuration;

/**
 * Represents read-only copy of the typed entity configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
abstract class ImmutableTypedEntityConfiguration extends ImmutableEntityConfiguration implements TypedEntityConfiguration {
    private static final long serialVersionUID = 3675950742369935416L;
    private final String type;

    ImmutableTypedEntityConfiguration(final TypedEntityConfiguration entity) {
        super(entity);
        type = entity.getType();
    }

    @Override
    public abstract ImmutableTypedEntityConfiguration asReadOnly();

    @Override
    public final String getType() {
        return type;
    }

    @Override
    public final void setType(final String value) {
        throw new UnsupportedOperationException();
    }
}
