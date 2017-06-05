package com.bytex.snamp.configuration.scriptlet;

import com.bytex.snamp.configuration.ScriptletConfiguration;

/**
 * Represents entity that can be exported as scriptlet configuration.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ScriptletConfigurationSupport {
    void configureScriptlet(final ScriptletConfiguration scriptlet);
}
