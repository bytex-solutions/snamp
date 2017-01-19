package com.bytex.snamp.configuration;

/**
 * Represents gateway configuration.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface GatewayConfiguration extends TypedEntityConfiguration {
    /**
     * Represents name of configuration parameter that points to thread pool in {@link com.bytex.snamp.concurrent.ThreadPoolRepository}
     * service used by gateway.
     * @since 1.2
     */
    String THREAD_POOL_KEY = "threadPool";

    @Override
    default GatewayConfiguration asReadOnly(){
        return new ImmutableGatewayConfiguration(this);
    }

    static void copy(final GatewayConfiguration input, final GatewayConfiguration output){
        output.setType(input.getType());
        output.load(input);
    }
}
