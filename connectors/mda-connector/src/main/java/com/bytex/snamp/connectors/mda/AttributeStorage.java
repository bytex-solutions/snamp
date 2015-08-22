package com.bytex.snamp.connectors.mda;

import com.google.gson.Gson;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AttributeStorage {
    private final OpenType<?> attributeType;
    private final String storageSlotName;

    protected AttributeStorage(final OpenType<?> type,
                               final String slotName){
        this.attributeType = Objects.requireNonNull(type);
        this.storageSlotName = slotName;
    }

    abstract Object getDefaultValue();

    protected abstract Object deserialize(final String value, final Gson formatter) throws OpenDataException;

    protected abstract String serialize(final Object value, final Gson formatter);

    final Object setValue(final Object value,
                            final ConcurrentMap<String, Object> storage) throws InvalidAttributeValueException {
        if(attributeType.isValue(value))
            return storage.put(storageSlotName, value);
        else throw new InvalidAttributeValueException(String.format("Value '%s' doesn't match to type '%s'", value, attributeType));
    }

    final String setValue(final String value,
                                 final Gson formatter,
                                 final ConcurrentMap<String, Object> storage) throws OpenDataException, InvalidAttributeValueException {
        return serialize(setValue(deserialize(value, formatter), storage), formatter);
    }

    final Object getValue(final ConcurrentMap<String, ?> storage){
        return storage.get(storageSlotName);
    }

    final String getValue(final Gson formatter,
                                 final ConcurrentMap<String, ?> storage) {
        return serialize(getValue(storage), formatter);
    }

    final void saveTo(final Map<String, AttributeStorage> parsers){
        parsers.put(storageSlotName, this);
    }
}
