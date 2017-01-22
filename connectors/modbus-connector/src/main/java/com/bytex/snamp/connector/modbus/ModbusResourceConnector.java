package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.modbus.transport.ModbusMaster;
import com.bytex.snamp.connector.modbus.transport.ModbusTransportType;
import com.bytex.snamp.jmx.JMExceptionUtils;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenDataException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;

import static com.bytex.snamp.connector.modbus.ModbusResourceConnectorConfigurationDescriptor.parseConnectionTimeout;
import static com.bytex.snamp.connector.modbus.ModbusResourceConnectorConfigurationDescriptor.parseRetryCount;

/**
 * Represents Modbus connector.
 */
final class ModbusResourceConnector extends AbstractManagedResourceConnector {
    private static final class ModbusAttributeRepository extends AbstractAttributeRepository<ModbusAttributeInfo> {
        private final ModbusMaster client;

        private ModbusAttributeRepository(final String resourceName, final ModbusMaster client) {
            super(resourceName, ModbusAttributeInfo.class, false);
            this.client = Objects.requireNonNull(client);
        }

        @Override
        protected ModbusAttributeInfo<?> connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws AttributeNotFoundException, OpenDataException {
            switch (descriptor.getName(attributeName)) {
                case CoilAttribute.NAME:
                    if (ModbusResourceConnectorConfigurationDescriptor.hasCount(descriptor))
                        return new CoilSetAttribute(attributeName, descriptor);
                    else
                        return new CoilAttribute(attributeName, descriptor);
                case InputRegisterAttribute.NAME:
                    if (ModbusResourceConnectorConfigurationDescriptor.hasCount(descriptor))
                        return new InputRegisterSetAttribute(attributeName, descriptor);
                    else
                        return new InputRegisterAttribute(attributeName, descriptor);
                case InputDiscreteAttribute.NAME:
                    if (ModbusResourceConnectorConfigurationDescriptor.hasCount(descriptor))
                        return new InputDiscreteSetAttribute(attributeName, descriptor);
                    else
                        return new InputDiscreteAttribute(attributeName, descriptor);
                case HoldingRegisterAttribute.NAME:
                    if (ModbusResourceConnectorConfigurationDescriptor.hasCount(descriptor))
                        return new HoldingRegisterSetAttribute(attributeName, descriptor);
                    else
                        return new HoldingRegisterAttribute(attributeName, descriptor);
                case FileAttribute.NAME:
                    return new FileAttribute(attributeName, descriptor);
                default:
                    throw JMExceptionUtils.attributeNotFound(descriptor.getName(attributeName));
            }
        }

        @Override
        protected Object getAttribute(final ModbusAttributeInfo metadata) throws Exception {
            return metadata.getValue(client);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void setAttribute(final ModbusAttributeInfo attribute, Object value) throws Exception {
            if(attribute.getOpenType().isValue(value))
                attribute.setValue(client, value);
            else
                throw new InvalidAttributeValueException("Invalid value: " + value);
        }
    }
    private final ModbusMaster client;
    @Aggregation
    private final ModbusAttributeRepository attributes;

    private ModbusResourceConnector(final String resourceName,
                                    final ModbusTransportType transportType,
                                    final String address,
                                    final int port) throws IOException {
        client = transportType.createMaster(address, port);
        attributes = new ModbusAttributeRepository(resourceName, client);
    }

    private static ModbusTransportType getTransportType(final URI connectionString) throws MalformedURLException{
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

    @Override
    protected MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes);
    }

    private void connect(final int socketTimeout, final int retryCount) throws IOException {
        client.setRetryCount(retryCount);
        client.connect(socketTimeout);
    }

    void connect(final ManagedResourceInfo configuration) throws IOException {
        setConfiguration(configuration);
        connect(parseConnectionTimeout(configuration), parseRetryCount(configuration));
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
