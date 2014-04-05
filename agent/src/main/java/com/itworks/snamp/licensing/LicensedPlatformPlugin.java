package com.itworks.snamp.licensing;

import com.itworks.snamp.core.PlatformPlugin;

/**
 * Represents SNAMP platform plugin which functionality managed by commercial license.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface LicensedPlatformPlugin<L extends LicenseLimitations> extends PlatformPlugin {
    /**
     * Returns license limitations associated with this plugin.
     * @return The license limitations applied to this plugin.
     */
    public L getLimitations();
}
