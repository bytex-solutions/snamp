package com.bytex.snamp.connectors.mda.impl.thrift;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.mda.DataAcceptorFactory;
import com.bytex.snamp.connectors.mda.impl.MDAResourceConfigurationDescriptorProviderImpl;
import com.bytex.snamp.connectors.mda.impl.MDAThreadPoolConfig;
import com.bytex.snamp.internal.Utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.bytex.snamp.connectors.mda.impl.MDAResourceConfigurationDescriptorProviderImpl.parseSocketTimeout;

/**
 * Represents factory of {@link ThriftDataAcceptor} class.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ThriftDataAcceptorFactory implements DataAcceptorFactory {
    private static final String THRIFT_SCHEME = "thrift";

    ThriftDataAcceptor create(final String resourceName,
                        final URI connectionString,
                        final TimeSpan expirationTime,
                        final Map<String, String> parameters) throws Exception {
        MDAResourceConfigurationDescriptorProviderImpl.waitForHazelcast(parameters, Utils.getBundleContextByObject(this));
        return new ThriftDataAcceptor(resourceName,
                expirationTime,
                new InetSocketAddress(InetAddress.getByName(connectionString.getHost()), connectionString.getPort()),
                parseSocketTimeout(parameters),
                new MDAThreadPoolConfig(resourceName, parameters));
    }

    @Override
    public ThriftDataAcceptor create(final String resourceName,
                                     final String connectionString,
                                     final TimeSpan expirationTime,
                                     final Map<String, String> parameters) throws Exception {
        return create(resourceName, new URI(connectionString), expirationTime, parameters);
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
