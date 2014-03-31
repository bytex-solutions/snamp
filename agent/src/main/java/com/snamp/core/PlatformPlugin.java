package com.snamp.core;

import net.xeoh.plugins.base.Plugin;

/**
 * Represents SNAMP platform plug-in.<br/>
 * <p>
 *     The implementer class should be annotated with {@link net.xeoh.plugins.base.annotations.PluginImplementation}.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface PlatformPlugin extends PlatformService, Plugin {
}
