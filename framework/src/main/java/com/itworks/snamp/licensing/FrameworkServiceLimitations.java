package com.itworks.snamp.licensing;

import com.itworks.snamp.core.FrameworkService;
import com.itworks.snamp.internal.annotations.*;

/**
 * Represents an interface that describes license limitations of the SNAMP service.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public interface FrameworkServiceLimitations<T extends FrameworkService> extends LicenseLimitations {
    /**
     * Verifies version of the SNAMP-specific version.
     * @param serviceContract Type of the service contract to verify.
     * @throws LicensingException Actual version of the service doesn't met to license requirements.
     */
    @ThreadSafe
    void verifyServiceVersion(final Class<? extends T> serviceContract) throws LicensingException;
}
