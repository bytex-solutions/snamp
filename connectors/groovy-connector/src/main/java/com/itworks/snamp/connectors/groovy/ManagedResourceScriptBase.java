package com.itworks.snamp.connectors.groovy;

import groovy.lang.GroovyObject;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagedResourceScriptBase extends GroovyObject {
    String RESOURCE_NAME_VAR = "resourceName";
}