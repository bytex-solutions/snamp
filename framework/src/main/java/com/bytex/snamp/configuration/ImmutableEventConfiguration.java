package com.bytex.snamp.configuration;

/**
 * Represents read-only copy of the event configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class ImmutableEventConfiguration extends ImmutableFeatureConfiguration implements EventConfiguration {
    private static final long serialVersionUID = 4813934233660077451L;

    ImmutableEventConfiguration(final EventConfiguration event){
        super(event);
    }

    @Override
    public ImmutableEventConfiguration asReadOnly() {
        return this;
    }
}
