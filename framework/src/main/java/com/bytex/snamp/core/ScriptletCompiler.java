package com.bytex.snamp.core;

import com.bytex.snamp.configuration.ScriptletConfiguration;

/**
 * Provides compilation of {@link com.bytex.snamp.configuration.ScriptletConfiguration} into executable entity.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ScriptletCompiler<O> {
    O compile(final ScriptletConfiguration scriptlet) throws ScriptletCompilationException;
}
