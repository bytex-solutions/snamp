package com.itworks.snamp.licensing;

import com.itworks.snamp.core.PlatformService;
import org.apache.commons.collections4.Factory;

/**
 * Represents OSGi service that allows to restrict functionality of another SNAMP services.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface LicenseReader extends PlatformService {
    /**
     * Represents system property that contains path to the SNAMP commercial license descriptor.
     */
    String LICENSE_FILE_PROPERTY = "com.snamp.itworks.licensing.file";

    /**
     * Reload the license from the persistent storage.
     */
    void reload();

    /**
     * Returns the limitations from the currently loaded license.
     * @param limitationsDescriptor The limitations descriptor.
     * @param fallback The fallback factory that produces limitation holder if license is not available.
     * @param <T> Type of the license limitations descriptor.
     * @return A new instance of the license limitations.
     */
     <T extends LicenseLimitations> T getLimitations(final Class<T> limitationsDescriptor, final Factory<T> fallback);
}
