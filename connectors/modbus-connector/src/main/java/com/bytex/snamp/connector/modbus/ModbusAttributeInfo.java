package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.attributes.OpenMBeanAttributeAccessor;
import com.bytex.snamp.connector.modbus.master.SlaveDeviceAccess;

import javax.management.openmbean.OpenType;
import java.util.Objects;

import static com.bytex.snamp.connector.modbus.ModbusResourceConnectorConfigurationDescriptor.*;

/**
 *
 */
abstract class ModbusAttributeInfo<T, A extends SlaveDeviceAccess> extends OpenMBeanAttributeAccessor<T> {
    private static final long serialVersionUID = -1718119127501806280L;
    private final A slaveDeviceAccess;

    ModbusAttributeInfo(final String attributeID,
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
