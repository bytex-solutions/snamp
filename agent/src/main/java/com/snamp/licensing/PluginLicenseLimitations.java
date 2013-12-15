package com.snamp.licensing;

import com.snamp.*;

/**
 * Represents an interface for describing plugin license limitations.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public interface PluginLicenseLimitations<T extends PlatformPlugin> {
    /**
     *
     * @param pluginImpl
     * @throws LicensingException
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public void verifyPluginVersion(final Class<? extends T> pluginImpl) throws LicensingException;
}
