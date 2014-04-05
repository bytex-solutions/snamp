package com.itworks.snamp.licensing;

import com.itworks.snamp.core.PlatformPlugin;
import com.itworks.snamp.internal.Internal;
import com.itworks.snamp.internal.MethodThreadSafety;
import com.itworks.snamp.internal.ThreadSafety;

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
