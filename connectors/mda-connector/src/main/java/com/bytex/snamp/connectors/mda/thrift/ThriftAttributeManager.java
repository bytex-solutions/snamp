package com.bytex.snamp.connectors.mda.thrift;

import com.google.common.cache.Cache;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class ThriftAttributeManager {
    private final OpenType<?> attributeType;
    private final String storageSlotName;

    protected ThriftAttributeManager(final OpenType<?> type,
                                   final String slotName){
        this.attributeType = Objects.requireNonNull(type);
        this.storageSlotName = slotName;
    }

    abstract Object getDefaultValue();

    protected abstract void serialize(final Object input, final TProtocol output) throws TException;

    protected abstract Object deserialize(final TProtocol input) throws TException;

    final Object setValue(final Object value,
                          final ConcurrentMap<String, Object> storage) throws InvalidAttributeValueException {
        if(attributeType.isValue(value))
            return storage.put(storageSlotName, value);
        else throw new InvalidAttributeValueException(String.format("Value '%s' doesn't match to type '%s'", value, attributeType));
    }

    final void setValue(final TProtocol input,
                        final TProtocol output,
                        final ConcurrentMap<String, Object> storage) throws TException, InvalidAttributeValueException {
        final Object previous = setValue(deserialize(input), storage);
        serialize(previous, output);
    }

    final Object getValue(final ConcurrentMap<String, ?> storage){
        return storage.get(storageSlotName);
    }

    final void getValue(final TProtocol output, final ConcurrentMap<String, ?> storage) throws TException {
        serialize(getValue(storage), output);
    }

    final void saveTo(final Cache<String, ThriftAttributeManager> parsers) {
        parsers.put(storageSlotName, this);
    }
}
