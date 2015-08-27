package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.mda.MdaAttributeAccessor;
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
final class ThriftAttributeAccessor extends MdaAttributeAccessor {
    private final ThriftValueParser parser;

    protected ThriftAttributeAccessor(final String name,
                                      final OpenType<?> type,
                                      final AttributeDescriptor descriptor,
                                      final ThriftValueParser manager) {
        super(name, type, AttributeSpecifier.READ_WRITE, descriptor);
        this.parser = Objects.requireNonNull(manager);
    }

    final void setValue(final TProtocol input,
                        final TProtocol output,
                        final ConcurrentMap<String, Object> storage) throws TException, InvalidAttributeValueException {
        final Object previous = setValue(parser.deserialize(input), storage);
        parser.serialize(previous, output);
    }

    final void getValue(final TProtocol output, final ConcurrentMap<String, ?> storage) throws TException {
        parser.serialize(getValue(storage), output);
    }

    static void saveParser(final ThriftValueParser parser,
                           final AttributeDescriptor descriptor,
                           final Cache<String, ThriftValueParser> parsers) {
        parsers.put(getStorageName(descriptor), parser);
    }

    static void getValue(final String storageName,
                         final ConcurrentMap<String, Object> storage,
                         final TProtocol output,
                         final ThriftValueParser parser) throws TException {
        parser.serialize(storage.get(storageName), output);
    }

    public static void setValue(final String storageName,
                                final ConcurrentMap<String, Object> storage,
                                final TProtocol input,
                                final TProtocol output,
                                final ThriftValueParser parser) throws TException {
        final Object previous = storage.put(storageName, parser.deserialize(input));
        parser.serialize(previous, output);
    }
}
