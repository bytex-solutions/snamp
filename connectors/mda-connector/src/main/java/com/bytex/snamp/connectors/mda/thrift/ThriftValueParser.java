package com.bytex.snamp.connectors.mda.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface ThriftValueParser {

    Object getDefaultValue();

    void serialize(final Object input, final TProtocol output) throws TException;

    Object deserialize(final TProtocol input) throws TException;

}
