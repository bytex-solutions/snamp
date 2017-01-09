package com.bytex.snamp.json;

import org.codehaus.jackson.JsonProcessingException;

import javax.management.openmbean.OpenDataException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenTypeProcessingException extends JsonProcessingException {
    private static final long serialVersionUID = -6023205807641406363L;

    OpenTypeProcessingException() {
        super("Malformed JSON. Unable to recognize JMX open type.");
    }

    OpenTypeProcessingException(final OpenDataException e) {
        super(e);
    }
}
