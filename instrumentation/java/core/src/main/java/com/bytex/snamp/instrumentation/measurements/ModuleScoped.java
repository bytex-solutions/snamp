package com.bytex.snamp.instrumentation.measurements;

/**
 * Represents measurement scoped to the specified subsystem or module.
 */
interface ModuleScoped {
    String getModuleName();
}
