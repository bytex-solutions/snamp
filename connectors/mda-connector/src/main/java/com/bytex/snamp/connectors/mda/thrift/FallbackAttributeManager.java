package com.bytex.snamp.connectors.mda.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * Represents default attribute manager.
 */
final class FallbackAttributeManager extends ThriftAttributeManager {
    FallbackAttributeManager(final String slotName) {
        super(SimpleType.STRING, slotName);
    }

    @Override
    Object getDefaultValue() {
        return "";
    }

    @Override
    protected void serialize(final Object input, final TProtocol output) throws TException {
        output.writeString(String.valueOf(input));
    }

    @Override
    protected String deserialize(final TProtocol input) throws TException {
        return input.readString();
    }
}
