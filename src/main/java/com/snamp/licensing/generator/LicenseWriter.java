package com.snamp.licensing.generator;

import net.xeoh.plugins.base.Plugin;

/**
 * @author roman
 */
interface LicenseWriter {
    public boolean addPlugin(final Class<? extends Plugin> pluginImpl);
}
