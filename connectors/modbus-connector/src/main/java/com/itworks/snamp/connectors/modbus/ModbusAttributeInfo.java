package com.itworks.snamp.connectors.modbus;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.attributes.OpenAttributeAccessor;
import com.itworks.snamp.connectors.modbus.protocol.SlaveDeviceAccess;

import javax.management.openmbean.OpenType;
import java.io.Serializable;
import java.util.Objects;

import static com.itworks.snamp.connectors.modbus.ModbusResourceConnectorConfigurationDescriptor.parseOffset;

/**
 *
 */
abstract class ModbusAttributeInfo<T, A extends SlaveDeviceAccess> extends OpenAttributeAccessor<T> {
    private final A slaveDeviceAccess;

    protected ModbusAttributeInfo(final String attributeID,
                                  final String description,
                                  final OpenType<T> attributeType,
                                  final AttributeSpecifier specifier,
                                  final AttributeDescriptor descriptor,
                                  final A deviceAccess) {
        super(attributeID, description, attributeType, specifier, descriptor);
        this.slaveDeviceAccess = Objects.requireNonNull(deviceAccess);
    }

    final int getOffset() throws ModbusAbsentConfigurationParameterException {
        return parseOffset(getDescriptor());
    }

    protected abstract T getValue(final A deviceAccess) throws Exception;

    @Override
    protected final T getValue() throws Exception {
        return getValue(slaveDeviceAccess);
    }

    protected void setValue(final A deviceAccess, final T value) throws Exception{
        throw new UnsupportedOperationException(String.format("Attribute '%s' is read-only", getName()));
    }

    @Override
    protected final void setValue(final T value) throws Exception {
        setValue(slaveDeviceAccess, value);
    }
}
