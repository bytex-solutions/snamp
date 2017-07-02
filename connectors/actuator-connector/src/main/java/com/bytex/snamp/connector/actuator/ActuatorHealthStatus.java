package com.bytex.snamp.connector.actuator;

import org.codehaus.jackson.JsonNode;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
enum ActuatorHealthStatus {
    DOWN,
    UP,
    OUT_OF_SERVICE,
    UNKNOWN;

    static ActuatorHealthStatus valueOf(final JsonNode statusNode) {
        String value = statusNode.getTextValue();
        if (isNullOrEmpty(value))
            return UNKNOWN;
        value = value.toUpperCase();
        return valueOf(value);
    }
}
