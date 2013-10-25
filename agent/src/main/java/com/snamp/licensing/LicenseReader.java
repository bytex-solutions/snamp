package com.snamp.licensing;

import net.xeoh.plugins.base.Plugin;

/**
 * Represents license reader.
 * @author roman
 */
public interface LicenseReader {
    /**
     * Returns a set of restrictions associated with the specified plugin.
     *
     * @param pluginImpl The plugin implementation.
     * @return A set of restrictions.
     */
    public Restrictions getRestrictions(final Class<? extends Plugin> pluginImpl);

    /**
     * Determines whether the specified plugin is allowed by this license.
     *
     * @param pluginImpl The class that represents a plugin.
     * @return {@literal true}, if the specified plug-in is allowed; otherwise, {@literal false}.
     */
    public boolean isPluginAllowed(final Class<? extends Plugin> pluginImpl);
}
