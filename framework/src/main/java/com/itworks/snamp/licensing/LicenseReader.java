package com.itworks.snamp.licensing;

import com.google.common.base.Supplier;
import com.itworks.snamp.core.FrameworkService;
import org.osgi.service.cm.ManagedService;

/**
 * Represents OSGi service that allows to restrict functionality of another SNAMP services.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface LicenseReader extends FrameworkService, ManagedService {
    /**
     * Represents persistence identifier used to read and write license content.
     */
    String LICENSE_PID = "com.itworks.snamp.license";

    /**
     * Represents encoding of the license file.
     */
    String LICENSE_CONTENT_ENCODING = "UTF-8";

    /**
     * Represents name of the entry in the configuration dictionary which
     * contains raw license content in the form of byte array.
     */
    String LICENSE_CONTENT_ENTRY = "license";

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
     <T extends LicenseLimitations> T getLimitations(final Class<T> limitationsDescriptor, final Supplier<T> fallback);
}
