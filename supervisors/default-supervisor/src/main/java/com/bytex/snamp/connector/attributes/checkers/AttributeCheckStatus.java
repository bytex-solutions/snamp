package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.InvalidAttributeValue;
import com.bytex.snamp.connector.health.OkStatus;

import javax.management.Attribute;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum AttributeCheckStatus {
    OK {
        @Override
        public OkStatus createStatus(final String resourceName, final Attribute attribute) {
            return new OkStatus();
        }
    },
    SUSPICIOUS {
        @Override
        public InvalidAttributeValue createStatus(final String resourceName, final Attribute attribute) {
            return new InvalidAttributeValue(resourceName, attribute, false);
        }
    },
    MALFUNCTION {
        @Override
        public InvalidAttributeValue createStatus(final String resourceName, final Attribute attribute) {
            return new InvalidAttributeValue(resourceName, attribute, true);
        }
    };

    public abstract HealthStatus createStatus(final String resourceName, final Attribute attribute);
}
