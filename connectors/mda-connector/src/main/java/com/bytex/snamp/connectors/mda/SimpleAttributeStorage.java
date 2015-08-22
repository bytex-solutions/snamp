package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.jmx.DefaultValues;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.gson.Gson;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SimpleAttributeStorage extends AttributeStorage {
    private final Class<?> attributeType;
    private final Object defaultValue;

    SimpleAttributeStorage(final SimpleType<?> type, final String slotName) {
        super(type, slotName);
        this.attributeType = WellKnownType.getType(type).getJavaType();
        assert attributeType != null;
        this.defaultValue = DefaultValues.get(type);
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
