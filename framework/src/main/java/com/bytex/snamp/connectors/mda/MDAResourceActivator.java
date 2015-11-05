package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.ManagedResourceActivator;
import org.osgi.service.http.HttpService;

import javax.management.openmbean.CompositeData;
import java.util.Map;
import java.util.Set;

import static com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider.parseExpireTime;

/**
 * Represents basic activator for MDA connectors.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class MDAResourceActivator extends ManagedResourceActivator<DataAcceptor> {


    protected static abstract class MonitoringDataAcceptorFactory extends ManagedResourceConnectorModeler<DataAcceptor> implements Iterable<DataAcceptorFactory>{
        @Override
        protected final boolean addAttribute(final DataAcceptor connector, final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
            return connector.addAttribute(attributeID, attributeName, readWriteTimeout, options);
        }

        @Override
        protected final void removeAttributesExcept(final DataAcceptor connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected final boolean enableNotifications(final DataAcceptor connector, final String listId, final String category, final CompositeData options) {
            return connector.enableNotifications(listId, category, options);
        }

        @Override
        protected final void disableNotificationsExcept(final DataAcceptor connector, final Set<String> events) {
            connector.disableNotificationsExcept(events);
        }

        @Override
        protected final boolean enableOperation(final DataAcceptor connector, final String operationID, final String operationName, final TimeSpan invocationTimeout, final CompositeData options) {
            return false;
        }

        @Override
        protected final void disableOperationsExcept(final DataAcceptor connector, final Set<String> operations) {

        }

        @Override
        public final DataAcceptor createConnector(final String resourceName, final String connectionString, final Map<String, String> connectionParameters, final RequiredService<?>... dependencies) throws Exception {
            final TimeSpan expirationTime = TimeSpan.ofMillis(parseExpireTime(connectionParameters));
            for(final DataAcceptorFactory factory: this)
                if(factory.canCreateFrom(connectionString)){
                    final DataAcceptor acceptor = factory.create(resourceName,
                            connectionString,
                            expirationTime,
                            connectionParameters);
                    final HttpService publisher = getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies);
                    acceptor.beginListening(publisher);
                    return acceptor;
                }
            throw new InstantiationException("Unsupported connection string: ".concat(connectionString));
        }
    }

    protected MDAResourceActivator(final MonitoringDataAcceptorFactory factory,
                                   final ConfigurationEntityDescriptionManager<? extends MDAResourceConfigurationDescriptorProvider> descriptionManager,
                                   final RequiredService<?>... dependencies){
        super(factory, dependencies, new SupportConnectorServiceManager[]{descriptionManager});
    }
}
