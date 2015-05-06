package com.itworks.snamp.connectors.wmq;

import com.ibm.mq.MQException;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.internal.annotations.SpecialUse;

import javax.management.openmbean.CompositeData;
import java.beans.IntrospectionException;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MQConnectorActivator extends ManagedResourceActivator<MQConnector> {
    private static final String NAME = MQConnector.NAME;

    private static final class WMQJavaClassesNotInstalled extends PrerequisiteException{
        private static final long serialVersionUID = -3317875854766072865L;

        private WMQJavaClassesNotInstalled() {
            super("WebSphere MQ classes for Java are not installed into OSGi environment");
        }
    }

    private static final class MQConnectorFactory extends ManagedResourceConnectorModeler<MQConnector> {

        @Override
        protected void addAttribute(final MQConnector connector, final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
            connector.addAttribute(attributeID, attributeName, readWriteTimeout, options);
        }

        @Override
        protected void enableNotifications(final MQConnector connector, final String listId, final String category, final CompositeData options) {
            //not supported
        }

        @Override
        protected void addOperation(final MQConnector connector, final String operationID, final String operationName, final CompositeData options) {
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

    private static final class MQDiscoveryServiceProvider extends DiscoveryServiceManager<MQConnector.MQDiscoveryService>{
        @Override
        protected MQConnector.MQDiscoveryService createDiscoveryService(final RequiredService<?>... dependencies) throws IntrospectionException {
            return new MQConnector.MQDiscoveryService();
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
        super(NAME,
                new MQConnectorFactory(),
                new MQConnectorConfigurationProvider(),
                new MQDiscoveryServiceProvider(),
                new MQMaintenanceManager());
    }

    @Override
    protected void checkPrerequisites() throws WMQJavaClassesNotInstalled {
        if (!MQConnectorMaintainer.isWmqInstalledImpl())
            throw new WMQJavaClassesNotInstalled();
    }
}