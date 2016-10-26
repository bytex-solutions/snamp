package com.bytex.snamp.connector.mda.impl.thrift;

import com.bytex.snamp.connector.mda.DataAcceptorFactory;
import com.bytex.snamp.connector.mda.impl.MDAConnectorDescriptionProvider;
import org.apache.thrift.transport.TTransportException;

import java.net.*;
import java.util.Map;

/**
 * Represents factory of {@link ThriftDataAcceptor} class.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ThriftDataAcceptorFactory implements DataAcceptorFactory {
    private static final String THRIFT_SCHEME = "thrift";

    private static ThriftDataAcceptor create(final String resourceName,
                                             final URI connectionString,
                                             final Map<String, String> parameters) throws UnknownHostException, TTransportException {
        final MDAConnectorDescriptionProvider configurationParser = MDAConnectorDescriptionProvider.getInstance();
        return new ThriftDataAcceptor(resourceName,
                new InetSocketAddress(InetAddress.getByName(connectionString.getHost()), connectionString.getPort()),
                configurationParser.parseSocketTimeout(parameters),
                () -> configurationParser.parseThreadPool(parameters));
    }

    @Override
    public ThriftDataAcceptor create(final String resourceName,
                                             final String connectionString,
                                             final Map<String, String> parameters) throws UnknownHostException, TTransportException, URISyntaxException {
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
