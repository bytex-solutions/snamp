package com.bytex.snamp.core;

import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents repository of shared objects operating in thread-safe manner.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
@ThreadSafe
public abstract class AbstractSharedObjectRepository<S extends SharedObject> extends ConcurrentResourceAccessor<Map<String, S>> implements SharedObjectRepository<S> {
    private static final long serialVersionUID = 4354344539777078421L;

    protected AbstractSharedObjectRepository() {
        super(new HashMap<>());
    }

    final Logger getLogger() {
        return LoggerProvider.getLoggerForObject(this);
    }

    @Nonnull
    protected abstract S createSharedObject(final String name);

    /**
     * Gets or creates shared object.
     *
     * @param name Name of the shared object.
     * @return Shared object.
     */
    @Nonnull
    @Override
    public final S getSharedObject(@Nonnull final String name) {
        S service = read(services -> services.get(name));
        if (service == null)
            service = write(services -> {
                S result = services.get(name);
                if (result == null)
                    services.put(name, createSharedObject(name));
                return result;
            });
        return service;
    }

    protected void releaseSharedObject(final S service){

    }

    public final void releaseAll() {
        write(services -> {
            services.values().forEach(this::releaseSharedObject);
            services.clear();
            return null;
        });
    }

    public final void clear() {
        write(services -> {
            services.clear();
            return null;
        });
    }

    @Override
    public final void releaseSharedObject(@Nonnull final String name) {
        final S service = write(services -> services.remove(name));
        if (service != null)
            releaseSharedObject(service);
    }
}
