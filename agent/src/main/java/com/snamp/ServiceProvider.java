package com.snamp;

/**
 * Represents service provider.
 * @author roman
 */
public interface ServiceProvider {
    /**
     * Retrieves the service instance.
     * @param serviceType Type of the requested service.
     * @param <T> Type of the required service.
     * @return An instance of the requested service; or {@literal null} if service is not available.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public <T> T getService(final Class<T> serviceType);
}
