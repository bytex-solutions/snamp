package com.bytex.snamp.connectors.modbus;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.OpenAttributeRepository;
import com.bytex.snamp.connectors.metrics.MetricsReader;
import com.bytex.snamp.connectors.modbus.transport.ModbusMaster;
import com.bytex.snamp.connectors.modbus.transport.ModbusTransportType;
import com.bytex.snamp.jmx.JMExceptionUtils;

import javax.management.AttributeNotFoundException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents Modbus connector.
 */
final class ModbusResourceConnector extends AbstractManagedResourceConnector {
    private static final class ModbusAttributeRepository extends OpenAttributeRepository<ModbusAttributeInfo> {
        private final ModbusMaster client;
        private final Logger logger;

        private ModbusAttributeRepository(final String resourceName, final ModbusMaster client, final Logger logger) {
            super(resourceName, ModbusAttributeInfo.class);
            this.client = Objects.requireNonNull(client);
            this.logger = logger;
        }

        @Override
        protected ModbusAttributeInfo<?, ?> connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws AttributeNotFoundException, OpenDataException {
            switch (descriptor.getName(attributeName)) {
                case CoilAttribute.NAME:
                    if (ModbusResourceConnectorConfigurationDescriptor.hasCount(descriptor))
                        return new CoilSetAttribute(attributeName, descriptor, client);
                    else
                        return new CoilAttribute(attributeName, descriptor, client);
                case InputRegisterAttribute.NAME:
                    if (ModbusResourceConnectorConfigurationDescriptor.hasCount(descriptor))
                        return new InputRegisterSetAttribute(attributeName, descriptor, client);
                    else
                        return new InputRegisterAttribute(attributeName, descriptor, client);
                case InputDiscreteAttribute.NAME:
                    if (ModbusResourceConnectorConfigurationDescriptor.hasCount(descriptor))
                        return new InputDiscreteSetAttribute(attributeName, descriptor, client);
                    else
                        return new InputDiscreteAttribute(attributeName, descriptor, client);
                case HoldingRegisterAttribute.NAME:
                    if (ModbusResourceConnectorConfigurationDescriptor.hasCount(descriptor))
                        return new HoldingRegisterSetAttribute(attributeName, descriptor, client);
                    else
                        return new HoldingRegisterAttribute(attributeName, descriptor, client);
                case FileAttribute.NAME:
                    return new FileAttribute(attributeName, descriptor, client);
                default:
                    throw JMExceptionUtils.attributeNotFound(descriptor.getName(attributeName));
            }
        }

        @Override
        protected void failedToConnectAttribute(final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.WARNING, attributeName, e);
        }

        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(logger, Level.SEVERE, attributeID, e);
        }

        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(logger, Level.SEVERE, attributeID, value, e);
        }
    }
    private final ModbusMaster client;
    @Aggregation
    private final ModbusAttributeRepository attributes;
    @Aggregation
    private final MetricsReader metrics;

    ModbusResourceConnector(final String resourceName,
                            final ModbusTransportType transportType,
                            final String address,
                            final int port) throws IOException {
        client = transportType.createMaster(address, port);
        attributes = new ModbusAttributeRepository(resourceName, client, getLogger());
        metrics = assembleMetricsReader(attributes);
    }

    static ModbusTransportType getTransportType(final URI connectionString) throws MalformedURLException{
        switch (connectionString.getScheme()){
            case "tcp": return ModbusTransportType.TCP;
            case "udp": return ModbusTransportType.UDP;
            case "rtu-ip": return ModbusTransportType.RTU_IP;
            default: throw new MalformedURLException("Unsupported schema type: ".concat(connectionString.getScheme()));
        }
    }

    ModbusResourceConnector(final String resourceName,
                            final URI connectionString) throws IOException {
        this(resourceName, getTransportType(connectionString), connectionString.getHost(), connectionString.getPort());
    }

    boolean addAttribute(final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options){
        return attributes.addAttribute(attributeName, readWriteTimeout, options) != null;
    }

    void removeAttributesExcept(final Set<String> attributes) {
        this.attributes.removeAllExcept(attributes);
    }

    void connect(final int socketTimeout, final int retryCount) throws IOException {
        client.setRetryCount(retryCount);
        client.connect(socketTimeout);
    }

    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes);
    }

    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes);
    }

    @Override
    public void close() throws Exception {
        super.close();
        client.close();
    }
}
