package com.itworks.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.facade.ModbusUDPMaster;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;
import com.google.common.base.Function;
import com.google.common.collect.Range;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ResourceEventListener;
import com.itworks.snamp.connectors.attributes.AbstractAttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.OpenAttributeSupport;
import com.itworks.snamp.connectors.modbus.transport.ModbusClient;
import com.itworks.snamp.connectors.modbus.transport.ModbusTransportType;
import com.itworks.snamp.jmx.JMExceptionUtils;

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
final class ModbusResourceConnector extends AbstractManagedResourceConnector implements Modbus {
    private static final class ModbusAttributeSupport extends OpenAttributeSupport<ModbusAttributeInfo>{
        private final ModbusClient client;
        private final Logger logger;

        private ModbusAttributeSupport(final String resourceName, final ModbusClient client, final Logger logger) {
            super(resourceName, ModbusAttributeInfo.class);
            this.client = Objects.requireNonNull(client);
            this.logger = logger;
        }

        @Override
        protected ModbusAttributeInfo<?, ?> connectAttribute(final String attributeID, final AttributeDescriptor descriptor) throws AttributeNotFoundException, OpenDataException {
            switch (descriptor.getAttributeName()){
                case CoilAttribute.NAME: return new CoilAttribute(attributeID, descriptor, client);
                case InputRegisterAttribute.NAME: return new InputRegisterAttribute(attributeID, descriptor, client);
                case InputDiscreteAttribute.NAME: return new InputDiscreteAttribute(attributeID, descriptor, client);
                case HoldingRegisterAttribute.NAME: return new HoldingRegisterAttribute(attributeID, descriptor, client);
                case CoilSetAttribute.NAME: return new CoilSetAttribute(attributeID, descriptor, client);
                case InputDiscreteSetAttribute.NAME: return new InputDiscreteSetAttribute(attributeID, descriptor, client);
                case InputRegisterSetAttribute.NAME: return new InputRegisterSetAttribute(attributeID, descriptor, client);

                default: throw JMExceptionUtils.attributeNotFound(descriptor.getAttributeName());
            }
        }

        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.WARNING, attributeID, attributeName, e);
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
    private final ModbusClient client;
    private final ModbusAttributeSupport attributes;

    ModbusResourceConnector(final String resourceName,
                            final ModbusTransportType transportType,
                            final String address,
                            final int port){
        client = transportType.createClient(address, port);
        attributes = new ModbusAttributeSupport(resourceName, client, getLogger());
    }

    private static ModbusTransportType getTransportType(final URI connectionString) throws MalformedURLException{
        switch (connectionString.getScheme()){
            case "tcp": return ModbusTransportType.TCP;
            case "udp": return ModbusTransportType.UDP;
            default: throw new MalformedURLException("Unsupported schema type: " + connectionString.getScheme());
        }
    }

    ModbusResourceConnector(final String resourceName,
                            final URI connectionString) throws MalformedURLException{
        this(resourceName, getTransportType(connectionString), "", 0);
    }

    boolean addAttribute(final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options){
        return attributes.addAttribute(attributeID, attributeName, readWriteTimeout, options) != null;
    }

    void removeAttributesExcept(final Set<String> attributes) {
        this.attributes.removeAllExcept(attributes);
    }

    void connect() throws IOException {
        client.openConnection();
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
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType, new Function<Class<T>, T>() {
            @Override
            public T apply(final Class<T> objectType) {
                return ModbusResourceConnector.super.queryObject(objectType);
            }
        }, attributes);
    }

    @Override
    public void close() throws Exception {
        super.close();
        client.close();
    }
}
