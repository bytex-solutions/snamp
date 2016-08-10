package com.bytex.snamp.connector.mda.impl.thrift;

import com.bytex.snamp.connector.mda.impl.MDAAttributeRepository;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TStruct;

import javax.management.openmbean.OpenType;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ThriftAttributeRepository extends MDAAttributeRepository {
    ThriftAttributeRepository(final String resourceName,
                              final Logger logger) {
        super(resourceName, logger);
    }

    boolean getAttribute(final String storageKey, final TProtocol output) throws TException {
        final OpenType<?> attributeType = getAttributeType(storageKey);
        if (attributeType == null) return false;
        output.writeStructBegin(new TStruct(storageKey));
        output.writeFieldBegin(new TField("value", ThriftDataConverter.getType(attributeType), (short) 0));
        ThriftDataConverter.serialize(getStorage().get(storageKey), output);
        output.writeFieldStop();
        output.writeStructEnd();
        return true;
    }

    boolean setAttribute(final String storageKey, final TProtocol input, final TProtocol output) throws TException {
        final OpenType<?> attributeType = getAttributeType(storageKey);
        if (attributeType == null || !getStorage().containsKey(storageKey)) return false;

        input.readStructBegin();
        input.readFieldBegin();
        final Object previous = getStorage().put(storageKey, ThriftDataConverter.deserialize(attributeType, input));
        ThriftUtils.skipStopField(input);
        input.readFieldEnd();
        input.readStructEnd();

        output.writeStructBegin(new TStruct(storageKey));
        output.writeFieldBegin(new TField("value", ThriftDataConverter.getType(attributeType), (short) 0));
        ThriftDataConverter.serialize(previous, output);
        output.writeFieldStop();
        output.writeStructEnd();

        resetAccessTime();
        return true;
    }
}
