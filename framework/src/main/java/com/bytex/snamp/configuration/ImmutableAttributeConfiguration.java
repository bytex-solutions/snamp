package com.bytex.snamp.configuration;

import java.time.Duration;

/**
 * Represents read-only copy of the attribute configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class ImmutableAttributeConfiguration extends ImmutableFeatureConfiguration implements AttributeConfiguration {
    private static final long serialVersionUID = -9124191483467624246L;
    private final Duration readWriteTimeout;

    ImmutableAttributeConfiguration(final AttributeConfiguration configuration){
        super(configuration);
        readWriteTimeout = configuration.getReadWriteTimeout();
    }

    @Override
    public Duration getReadWriteTimeout() {
        return readWriteTimeout;
    }

    @Override
    public void setReadWriteTimeout(final Duration value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableAttributeConfiguration asReadOnly() {
        return this;
    }
}
