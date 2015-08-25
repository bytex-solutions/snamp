package com.bytex.snamp.connectors.mda.http;

import com.bytex.snamp.jmx.DefaultValues;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.gson.Gson;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SimpleAttributeManager extends HttpAttributeManager {
    private final Class<?> attributeType;
    private final Object defaultValue;

    SimpleAttributeManager(final WellKnownType knownType, final String slotName) {
        super(knownType.getOpenType(), slotName);
        this.attributeType = knownType.getJavaType();
        assert attributeType != null;
        this.defaultValue = DefaultValues.get(knownType.getOpenType());
    }

    @Override
    Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    protected Object deserialize(final String value, final Gson formatter) {
        return formatter.fromJson(value, attributeType);
    }

    @Override
    protected String serialize(final Object value, final Gson formatter) {
        return formatter.toJson(value, attributeType);
    }
}
