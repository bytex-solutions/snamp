package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import org.codehaus.jackson.JsonNode;

import javax.management.openmbean.OpenType;

/**
 * Represents single metric exposed by Spring Actuator.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
abstract class SpringMetric<T> extends AbstractOpenAttributeInfo {
    private static final long serialVersionUID = 5536078710775514254L;

    SpringMetric(final String name,
                 final OpenType<T> type,
                 final AttributeDescriptor descriptor) {
        super(name, type, "Spring Actuator metric", AttributeSpecifier.READ_ONLY, descriptor);
    }

    abstract T getValue(final JsonNode valueNode) throws Exception;
}
