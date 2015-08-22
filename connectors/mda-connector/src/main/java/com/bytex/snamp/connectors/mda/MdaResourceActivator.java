package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.ManagedResourceActivator;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

import javax.management.openmbean.CompositeData;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MdaResourceActivator extends ManagedResourceActivator<MonitoringDataAcceptor> {


    private static final class MonitoringDataAcceptorFactory extends ManagedResourceConnectorModeler<MonitoringDataAcceptor>{
        private static final String SERVLET_CONTEXT = "/snamp/connectors/mda/%s";

        @Override
        protected boolean addAttribute(final MonitoringDataAcceptor connector, final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
            return connector.addAttribute(attributeID, attributeName, readWriteTimeout, options);
        }

        @Override
        protected void removeAttributesExcept(final MonitoringDataAcceptor connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected boolean enableNotifications(final MonitoringDataAcceptor connector, final String listId, final String category, final CompositeData options) {
            return false;
        }

        @Override
        protected void disableNotificationsExcept(final MonitoringDataAcceptor connector, final Set<String> events) {

        }

        @Override
        protected boolean enableOperation(final MonitoringDataAcceptor connector, final String operationID, final String operationName, final TimeSpan invocationTimeout, final CompositeData options) {
            return false;
        }

        @Override
        protected void disableOperationsExcept(final MonitoringDataAcceptor connector, final Set<String> operations) {

        }

        private static String getServletContext(final String resourceName){
            return String.format(SERVLET_CONTEXT, resourceName);
        }

        @Override
        public MonitoringDataAcceptor createConnector(final String resourceName, final String connectionString, final Map<String, String> connectionParameters, final RequiredService<?>... dependencies) throws Exception {
            final HttpService publisher = getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies);
            if (publisher == null)
                throw new InstantiationException("OSGi HttpService is not available");
            final MonitoringDataAcceptor result = new MonitoringDataAcceptor(resourceName);
            publisher.registerServlet(getServletContext(resourceName), new MdaServlet(result), null, null);
            return result;
        }

        @Override
        public void releaseConnector(final MonitoringDataAcceptor connector) throws Exception {
            final BundleContext context = Utils.getBundleContextByObject(this);
            try {
                final ServiceHolder<HttpService> publisher = new ServiceHolder<>(context, HttpService.class);
                try {
                    publisher.get().unregister(getServletContext(connector.resourceName));
                } finally {
                    publisher.release(context);
                }
            } finally {
                super.releaseConnector(connector);
            }
        }
    }

    @SpecialUse
    public MdaResourceActivator() {
        super(new MonitoringDataAcceptorFactory(),
                new RequiredService<?>[]{new SimpleDependency<>(HttpService.class)},
                ArrayUtils.emptyArray(SupportConnectorServiceManager[].class));
    }
}
