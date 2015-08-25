package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.ManagedResourceActivator;
import org.osgi.service.http.HttpService;

import javax.management.openmbean.CompositeData;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MdaResourceActivator extends ManagedResourceActivator<DataAcceptor> {


    private static final class MonitoringDataAcceptorFactory extends ManagedResourceConnectorModeler<DataAcceptor>{

        private final ServiceLoader<DataAcceptorFactory> dataAcceptors;

        private MonitoringDataAcceptorFactory(){
            dataAcceptors = ServiceLoader.load(DataAcceptorFactory.class, getClass().getClassLoader());
        }

        @Override
        protected boolean addAttribute(final DataAcceptor connector, final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
            return connector.addAttribute(attributeID, attributeName, readWriteTimeout, options);
        }

        @Override
        protected void removeAttributesExcept(final DataAcceptor connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected boolean enableNotifications(final DataAcceptor connector, final String listId, final String category, final CompositeData options) {
            return connector.enableNotifications(listId, category, options);
        }

        @Override
        protected void disableNotificationsExcept(final DataAcceptor connector, final Set<String> events) {
            connector.disableNotificationsExcept(events);
        }

        @Override
        protected boolean enableOperation(final DataAcceptor connector, final String operationID, final String operationName, final TimeSpan invocationTimeout, final CompositeData options) {
            return false;
        }

        @Override
        protected void disableOperationsExcept(final DataAcceptor connector, final Set<String> operations) {

        }

        @Override
        public DataAcceptor createConnector(final String resourceName, final String connectionString, final Map<String, String> connectionParameters, final RequiredService<?>... dependencies) throws Exception {
            for(final DataAcceptorFactory factory: dataAcceptors)
                if(factory.canCreateFrom(connectionString)){
                    final DataAcceptor acceptor = factory.create(resourceName, connectionString, connectionParameters);
                    final HttpService publisher = getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies);
                    acceptor.beginAccept(publisher);
                    return acceptor;
                }
            throw new InstantiationException("Unsupported connection string: ".concat(connectionString));
        }
    }

    @SpecialUse
    public MdaResourceActivator() {
        super(new MonitoringDataAcceptorFactory(),
                new RequiredService<?>[]{new SimpleDependency<>(HttpService.class)},
                ArrayUtils.emptyArray(SupportConnectorServiceManager[].class));
    }
}
