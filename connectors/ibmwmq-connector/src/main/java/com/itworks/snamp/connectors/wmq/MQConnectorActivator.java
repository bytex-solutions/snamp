package com.itworks.snamp.connectors.wmq;

import com.ibm.mq.MQException;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.internal.annotations.SpecialUse;

import javax.management.openmbean.CompositeData;
import java.beans.IntrospectionException;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MQConnectorActivator extends ManagedResourceActivator<MQConnector> {
    private static final class WMQJavaClassesNotInstalled extends PrerequisiteException{
        private static final long serialVersionUID = -3317875854766072865L;

        private WMQJavaClassesNotInstalled() {
            super("WebSphere MQ classes for Java are not installed into OSGi environment");
        }

        @Override
        protected boolean abortStarting() {
            return false;
        }
    }

    private static final class MQConnectorFactory extends ManagedResourceConnectorModeler<MQConnector> {

        @Override
        protected boolean addAttribute(final MQConnector connector, final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
            return connector.addAttribute(attributeID, attributeName, readWriteTimeout, options) != null;
        }

        @Override
        protected boolean enableNotifications(final MQConnector connector, final String listId, final String category, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected boolean enableOperation(final MQConnector connector, final String operationID, final String operationName, final TimeSpan timeout, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected void removeAttributesExcept(final MQConnector connector, final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected void disableNotificationsExcept(final MQConnector connector, final Set<String> events) {
            //not supported
        }

        @Override
        protected void disableOperationsExcept(final MQConnector connector, final Set<String> operations) {
            //not supported
        }

        @Override
        public MQConnector createConnector(final String resourceName,
                                           final String connectionString,
                                           final Map<String, String> connectionParameters,
                                           final RequiredService<?>... dependencies) throws IntrospectionException, MQException {
            return new MQConnector(resourceName, connectionString, connectionParameters);
        }
    }

    private static final class MQConnectorConfigurationProvider extends ConfigurationEntityDescriptionManager<MQConnectorConfigurationDescriptor> {
        @Override
        protected MQConnectorConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new MQConnectorConfigurationDescriptor();
        }
    }

    private static final class MQDiscoveryServiceProvider extends DiscoveryServiceManager<MQConnector.MQDiscoveryService> {
        @Override
        protected MQConnector.MQDiscoveryService createDiscoveryService(final RequiredService<?>... dependencies) throws IntrospectionException, WMQJavaClassesNotInstalled {
            return new MQConnector.MQDiscoveryService();
        }

        @Override
        protected boolean isActivationAllowed() {
            return isPrerequisitesOK();
        }
    }

    private static final class MQMaintenanceManager extends MaintenanceServiceManager<MQConnectorMaintainer>{

        @Override
        protected MQConnectorMaintainer createMaintenanceService(final RequiredService<?>... dependencies) {
            return new MQConnectorMaintainer();
        }
    }

    @SpecialUse
    public MQConnectorActivator() {
        super(new MQConnectorFactory(),
                new MQConnectorConfigurationProvider(),
                new MQDiscoveryServiceProvider(),
                new MQMaintenanceManager());
    }

    private static boolean isWmqInstalled(){
        return MQConnectorMaintainer.isWmqInstalled();
    }

    @Override
    protected void checkPrerequisites() throws WMQJavaClassesNotInstalled {
        if (!isWmqInstalled())
            throw new WMQJavaClassesNotInstalled();
    }
}