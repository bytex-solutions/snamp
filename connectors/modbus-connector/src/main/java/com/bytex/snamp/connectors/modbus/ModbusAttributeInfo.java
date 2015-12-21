package com.bytex.snamp.connectors.modbus;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.attributes.OpenMBeanAttributeAccessor;
import com.bytex.snamp.connectors.modbus.master.SlaveDeviceAccess;

import javax.management.openmbean.OpenType;
import java.util.Objects;

import static com.bytex.snamp.connectors.modbus.ModbusResourceConnectorConfigurationDescriptor.*;

/**
 *
 */
abstract class ModbusAttributeInfo<T, A extends SlaveDeviceAccess> extends OpenMBeanAttributeAccessor<T> {
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

    final int getUnitID() {
        return parseUnitID(getDescriptor());
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
