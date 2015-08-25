package com.bytex.snamp.connectors.mda.http;

import com.google.gson.Gson;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class FallbackAttributeManager extends HttpAttributeManager {
    FallbackAttributeManager(final String slotName) {
        super(SimpleType.STRING, slotName);
    }

    @Override
    String getDefaultValue() {
        return "";
    }

    @Override
    protected String deserialize(final String value, final Gson formatter) {
        return value;
    }

    @Override
    protected String serialize(final Object value, final Gson formatter) {
        return String.valueOf(value);
    }
}
