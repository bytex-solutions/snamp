package com.bytex.snamp.connectors.mda.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

/**
 * Represents default attribute manager.
 */
final class FallbackValueParser implements ThriftValueParser {
    static final ThriftValueParser INSTANCE = new FallbackValueParser();

    private final TStruct struct;
    private FallbackValueParser(){
        struct = new TStruct("FALLBACK");
    }

    @Override
    public Object getDefaultValue() {
        return "";
    }

    @Override
    public void serialize(final Object input, final TProtocol output) throws TException {
        output.writeStructBegin(struct);
        output.writeFieldBegin(new TField("value", TType.STRING, (short) 0));
        output.writeString(String.valueOf(input));
        output.writeFieldEnd();
        output.writeFieldStop();
        output.writeStructEnd();
    }

    @Override
    public String deserialize(final TProtocol input) throws TException {
        input.readStructBegin();
        input.readFieldBegin();
        try {
            return input.readString();
        }
        finally {
            ThriftUtils.skipStopField(input);
            input.readFieldEnd();
            input.readStructEnd();
        }
    }
}
