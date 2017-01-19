package com.bytex.snamp.configuration;

/**
 * Represents read-only copy of the feature configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
abstract class ImmutableFeatureConfiguration extends ImmutableEntityConfiguration implements FeatureConfiguration {
    private static final long serialVersionUID = 2010338364077175239L;

    ImmutableFeatureConfiguration(final FeatureConfiguration feature){
        super(feature);
    }

    @Override
    public abstract ImmutableFeatureConfiguration asReadOnly();

    @Override
    public final void setAlternativeName(final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setAutomaticallyAdded(final boolean value) {
        throw new UnsupportedOperationException();
    }
}
