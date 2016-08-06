package com.bytex.snamp.adapters.groovy.dsl;

import java.util.Map;
import java.util.Set;

/**
 * Provides access to connected resources.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
interface ResourcesView {
    Map<String, ?> getResourceParameters(final String resourceName);
    Set<String> getList();
}
