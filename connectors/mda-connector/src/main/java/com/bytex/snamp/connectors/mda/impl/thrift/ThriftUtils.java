package com.bytex.snamp.connectors.mda.impl.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class ThriftUtils {
    private ThriftUtils(){

    }

    static TField skipField(final TProtocol protocol) throws TException {
        final TField result = protocol.readFieldBegin();
        TProtocolUtil.skip(protocol, result.type);
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
