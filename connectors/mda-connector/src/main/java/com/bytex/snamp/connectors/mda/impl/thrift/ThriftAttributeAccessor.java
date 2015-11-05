package com.bytex.snamp.connectors.mda.impl.thrift;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.mda.MDAAttributeAccessor;
import com.google.common.cache.Cache;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TStruct;

import javax.management.openmbean.OpenType;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ThriftAttributeAccessor extends MDAAttributeAccessor {

    protected ThriftAttributeAccessor(final String name,
                                      final OpenType<?> type,
                                      final AttributeDescriptor descriptor) {
        super(name, type, AttributeSpecifier.READ_WRITE, descriptor);
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
        output.writeStructBegin(new TStruct(storageName));
        output.writeFieldBegin(new TField("value", parser.getType(), (short) 0));
        parser.serialize(storage.get(storageName), output);
        output.writeFieldStop();
        output.writeStructEnd();
    }

    static void setValue(final String storageName,
                                final ConcurrentMap<String, Object> storage,
                                final TProtocol input,
                                final TProtocol output,
                                final ThriftValueParser parser) throws TException {
        input.readStructBegin();
        input.readFieldBegin();
        final Object previous = storage.put(storageName, parser.deserialize(input));
        ThriftUtils.skipStopField(input);
        input.readFieldEnd();
        input.readStructEnd();

        output.writeStructBegin(new TStruct(storageName));
        output.writeFieldBegin(new TField("value", parser.getType(), (short) 0));
        parser.serialize(previous, output);
        output.writeFieldStop();
        output.writeStructEnd();
    }
}
