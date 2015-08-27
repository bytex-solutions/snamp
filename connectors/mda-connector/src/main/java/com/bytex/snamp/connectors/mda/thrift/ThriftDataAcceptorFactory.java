package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.connectors.mda.DataAcceptorFactory;
import com.bytex.snamp.connectors.mda.MdaThreadPoolConfig;
import org.apache.thrift.transport.TTransportException;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.bytex.snamp.connectors.mda.MdaResourceConfigurationDescriptorProvider.parseExpireTime;
import static com.bytex.snamp.connectors.mda.MdaResourceConfigurationDescriptorProvider.parseSocketTimeout;

/**
 * Represents factory of {@link ThriftDataAcceptor} class.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ThriftDataAcceptorFactory implements DataAcceptorFactory {
    private static final String THRIFT_SCHEME = "thrift";

    static ThriftDataAcceptor create(final String resourceName,
                        final URI connectionString,
                        final Map<String, String> parameters) throws TTransportException {
        return new ThriftDataAcceptor(resourceName,
                parseExpireTime(parameters),
                InetSocketAddress.createUnresolved(connectionString.getHost(), connectionString.getPort()),
                parseSocketTimeout(parameters),
                new MdaThreadPoolConfig(resourceName, parameters));
    }

    @Override
    public ThriftDataAcceptor create(final String resourceName,
                               final String connectionString,
                               final Map<String, String> parameters) throws URISyntaxException, TTransportException {
        return create(resourceName, new URI(connectionString), parameters);
    }

    /**
     * Determines whether this factory can create Data Acceptor using the specified connection string.
     *
     * @param connectionString Candidate connection string.
     * @return {@literal true}, if this factory can be used to produce a new instance of Data Acceptor
     * using specified connection string; otherwise, {@literal false}.
     */
    @Override
    public boolean canCreateFrom(final String connectionString) {
        try{
            final URI thriftUri = new URI(connectionString);
            return THRIFT_SCHEME.equals(thriftUri.getScheme());
        }
        catch (final URISyntaxException e){
            return false;
        }
    }
}
