package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.InvalidAttributeValue;
import com.bytex.snamp.connector.health.OkStatus;

import javax.management.Attribute;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public enum AttributeCheckStatus {
    OK {
        @Override
        public OkStatus createStatus(final Attribute attribute) {
            return new OkStatus();
        }
    },
    SUSPICIOUS {
        @Override
        public InvalidAttributeValue createStatus(final Attribute attribute) {
            return new InvalidAttributeValue(attribute, false);
        }
    },
    MALFUNCTION {
        @Override
        public InvalidAttributeValue createStatus(final Attribute attribute) {
            return new InvalidAttributeValue(attribute, true);
        }
    };

    public abstract HealthStatus createStatus(final Attribute attribute);
}
