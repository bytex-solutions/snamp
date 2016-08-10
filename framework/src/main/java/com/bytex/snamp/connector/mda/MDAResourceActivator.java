package com.bytex.snamp.connector.mda;

import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.osgi.service.http.HttpService;

import javax.management.openmbean.CompositeData;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    protected static abstract class MonitoringDataAcceptorFactory extends ManagedResourceConnectorModeler<DataAcceptor> implements Iterable<DataAcceptorFactory>{
        @Override
        protected final boolean addAttribute(final DataAcceptor connector, final String attributeName, final Duration readWriteTimeout, final CompositeData options) {
            return connector.addAttribute(attributeName, readWriteTimeout, options);
        }

        @Override
        protected final void retainAttributes(final DataAcceptor connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected final boolean enableNotifications(final DataAcceptor connector, final String category, final CompositeData options) {
            return connector.enableNotifications(category, options);
        }

        @Override
        protected final void retainNotifications(final DataAcceptor connector, final Set<String> events) {
            connector.disableNotificationsExcept(events);
        }

        @Override
        protected final boolean enableOperation(final DataAcceptor connector, final String operationName, final Duration invocationTimeout, final CompositeData options) {
            return false;
        }

        @Override
        protected final void retainOperations(final DataAcceptor connector, final Set<String> operations) {

        }

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
