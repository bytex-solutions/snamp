package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Represents interpretation of correlationID.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@JsonSerialize(using = CorrelationSerializer.class)
@JsonDeserialize(using = CorrelationDeserializer.class)
public enum CorrelationPolicy {
    /**
     * Indicates that the correlationID is unique for communication between modules inside of single component.
     */
    LOCAL{
        @Override
        public String toString() {
            return "local";
        }
    },

    /**
     * Indicates that the correlationID is unique for communication between distributed components in the network.
     */
    GLOBAL{
        @Override
        public String toString() {
            return "global";
        }
    };

    public abstract String toString();
}
