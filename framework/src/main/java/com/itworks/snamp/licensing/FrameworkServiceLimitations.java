package com.itworks.snamp.licensing;

import com.itworks.snamp.core.FrameworkService;
import com.itworks.snamp.internal.Internal;
import com.itworks.snamp.internal.MethodThreadSafety;
import com.itworks.snamp.internal.ThreadSafety;

/**
 * Represents an interface that describes license limitations of the SNAMP service.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public interface FrameworkServiceLimitations<T extends FrameworkService> extends LicenseLimitations {
    /**
     *
     * @param pluginImpl
     * @throws LicensingException
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public void verifyPluginVersion(final Class<? extends T> pluginImpl) throws LicensingException;
}
