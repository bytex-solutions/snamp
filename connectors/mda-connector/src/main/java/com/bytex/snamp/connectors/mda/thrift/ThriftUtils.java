package com.bytex.snamp.connectors.mda.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ThriftUtils {
    private ThriftUtils(){

    }

    static TField skipField(final TProtocol protocol) throws TException {
        final TField result = protocol.readFieldBegin();
        protocol.readFieldEnd();
        return result;
    }

    static void skipField(final TProtocol protocol, final byte expectedType) throws TException {
        final TField field = skipField(protocol);
        if(field.type != expectedType)
            throw new TProtocolException(expectedType);
    }

    static void skipStopField(final TProtocol protocol) throws TException {
        skipField(protocol, TType.STOP);
    }
}
