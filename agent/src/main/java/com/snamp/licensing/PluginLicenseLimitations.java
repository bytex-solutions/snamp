package com.snamp.licensing;

import net.xeoh.plugins.base.Plugin;

/**
 * Represents an interface for describing plugin license limitations.
 * @author roman
 */
public interface PluginLicenseLimitations<T extends Plugin> {
    /**
     *
     * @param pluginImpl
     * @throws LicensingException
     */
    public void verifyPluginVersion(final Class<? extends T> pluginImpl) throws LicensingException;
}
