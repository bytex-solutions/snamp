package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.connector.supervision.HealthStatus;
import com.bytex.snamp.connector.supervision.InvalidAttributeValue;
import com.bytex.snamp.connector.supervision.OkStatus;

import javax.management.Attribute;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum AttributeCheckStatus {
    OK {
        @Override
        public OkStatus createStatus(final String resourceName, final Attribute attribute) {
            return new OkStatus(resourceName);
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

    public AttributeCheckStatus max(final AttributeCheckStatus other) {
        return compareTo(other) >= 0 ? this : other;
    }
}
