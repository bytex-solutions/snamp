package com.bytex.snamp.connector.mda;

import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.osgi.service.http.HttpService;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

import static com.bytex.snamp.connector.mda.MDAResourceConfigurationDescriptorProvider.parseExpireTime;

/**
 * Represents basic activator for MDA connector.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class MDAResourceActivator extends ManagedResourceActivator<DataAcceptor> {

    /**
     * Represents factory of MDA connector.
     */
    protected static abstract class MonitoringDataAcceptorFactory implements ManagedResourceConnectorLifecycleController<DataAcceptor>, Iterable<DataAcceptorFactory>{
        @Override
        public final DataAcceptor createConnector(final String resourceName, final String connectionString, final Map<String, String> connectionParameters, final RequiredService<?>... dependencies) throws Exception {
            final Duration expirationTime = Duration.ofMillis(parseExpireTime(connectionParameters));
            for(final DataAcceptorFactory factory: this)
                if(factory.canCreateFrom(connectionString)){
                    final DataAcceptor acceptor = factory.create(resourceName,
                            connectionString,
                            connectionParameters);
                    @SuppressWarnings("unchecked")
                    final HttpService publisher = getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies);
                    acceptor.beginListening(expirationTime, publisher);
                    return acceptor;
                }
            throw new InstantiationException("Unsupported connection string: ".concat(connectionString));
        }

        /**
         * Returns an iterator over a set of factories of MDA connector.
         *
         * @return an Iterator.
         */
        @Override
        public abstract Iterator<DataAcceptorFactory> iterator();
    }

    protected MDAResourceActivator(final MonitoringDataAcceptorFactory factory,
                                   final SupportConnectorServiceManager<ConfigurationEntityDescriptionProvider, ? extends MDAResourceConfigurationDescriptorProvider> descriptionManager,
                                   final RequiredService<?>... dependencies){
        super(factory, dependencies, new SupportConnectorServiceManager[]{descriptionManager});
    }
}
