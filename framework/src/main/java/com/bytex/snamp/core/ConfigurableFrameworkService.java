package com.bytex.snamp.core;

import com.bytex.snamp.Internal;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Provides runtime configuration support of SNAMP-related service.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
@Internal
public interface ConfigurableFrameworkService<C extends Map<String, ?>> extends FrameworkService {
    /**
     * Represents an exception indicating that the framework service cannot be updated
     * without it recreation. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.1
     */
    final class UnsupportedUpdateOperationException extends Exception {
        private static final long serialVersionUID = 8128304831615736668L;

        private UnsupportedUpdateOperationException() {
        }

        @Override
        public UnsupportedUpdateOperationException fillInStackTrace() {
            return this;
        }
    }

    /**
     * Gets runtime configuration of this service.
     * @return Runtime configuration of this service.
     * @implSpec Returning map is always immutable.
     */
    @Override
    @Nonnull
    C getConfiguration();

    /**
     * Updates this service with new configuration.
     * @param newConfiguration New configuration of the service.
     * @throws Exception Unable to update configuration.
     * @throws UnsupportedUpdateOperationException Update of configuration is not supported.
     */
    default void update(@Nonnull final C newConfiguration) throws Exception{
        throw new UnsupportedUpdateOperationException();
    }
}
