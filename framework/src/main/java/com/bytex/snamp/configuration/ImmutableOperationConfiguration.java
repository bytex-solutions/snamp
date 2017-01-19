package com.bytex.snamp.configuration;

import java.time.Duration;

/**
 * Represents read-only copy of the operation configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class ImmutableOperationConfiguration extends ImmutableFeatureConfiguration implements OperationConfiguration {
    private static final long serialVersionUID = -7534470270489626772L;
    private final Duration invocationTimeout;

    ImmutableOperationConfiguration(final OperationConfiguration configuration){
        super(configuration);
        invocationTimeout = configuration.getInvocationTimeout();
    }

    @Override
    public Duration getInvocationTimeout() {
        return invocationTimeout;
    }

    @Override
    public void setInvocationTimeout(final Duration value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableOperationConfiguration asReadOnly() {
        return this;
    }
}
