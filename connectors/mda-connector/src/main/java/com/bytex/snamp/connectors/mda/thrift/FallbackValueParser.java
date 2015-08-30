package com.bytex.snamp.connectors.mda.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

/**
 * Represents default attribute manager.
 */
final class FallbackValueParser implements ThriftValueParser {
    static final ThriftValueParser INSTANCE = new FallbackValueParser();

    private FallbackValueParser(){
    }

    @Override
    public Object getDefaultValue() {
        return "";
    }

    @Override
    public void serialize(final Object input, final TProtocol output) throws TException {
        output.writeString(String.valueOf(input));
    }

    @Override
    public String deserialize(final TProtocol input) throws TException {
        return input.readString();
    }

    @Override
    public byte getType() {
        return TType.STRING;
    }
}
