package com.bytex.snamp.connectors.mda.http;

import com.bytex.snamp.jmx.DefaultValues;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SimpleValueParser implements HttpValueParser {
    private final Class<?> attributeType;
    private final Object defaultValue;

    SimpleValueParser(final WellKnownType knownType) {
        this.attributeType = knownType.getJavaType();
        assert attributeType != null;
        this.defaultValue = DefaultValues.get(knownType.getOpenType());
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public Object deserialize(final JsonElement value, final Gson formatter) {
        return formatter.fromJson(value, attributeType);
    }

    @Override
    public JsonElement serialize(final Object value, final Gson formatter) {
        return formatter.toJsonTree(value, attributeType);
    }
}
