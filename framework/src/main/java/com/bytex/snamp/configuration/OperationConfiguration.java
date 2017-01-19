package com.bytex.snamp.configuration;

import java.time.Duration;

/**
 * Represents configuration of the managed resource operation.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface OperationConfiguration extends FeatureConfiguration {
    /**
     * Recommended timeout for invocation of operation in smart mode.
     */
    Duration TIMEOUT_FOR_SMART_MODE = Duration.ofSeconds(10);

    /**
     * Gets timeout of operation invocation.
     * @return Timeout value.
     */
    Duration getInvocationTimeout();

    /**
     * Sets timeout of operation invocation.
     * @param value A new timeout value.
     */
    void setInvocationTimeout(final Duration value);

    default OperationConfiguration asReadOnly(){
        return new ImmutableOperationConfiguration(this);
    }

    static void copy(final OperationConfiguration source, final OperationConfiguration dest){
        dest.setInvocationTimeout(source.getInvocationTimeout());
        dest.load(source);
    }
}
