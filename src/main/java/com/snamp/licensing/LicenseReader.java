package com.snamp.licensing;

import net.xeoh.plugins.base.Plugin;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Represents license reader.
 * @author roman
 */
public interface LicenseReader {


    /**
     * Determines whether the specified plugin is allowed by this license.
     * @param pluginImpl The class that represents a plugin.
     * @return {@literal true}, if the specified plug-in is allowed; otherwise, {@literal false}.
     */
    public boolean isPluginAllowed(final Class<? extends Plugin> pluginImpl);
}
