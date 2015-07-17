package com.itworks.snamp.connectors.modbus;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.modbus.protocol.SlaveDeviceAccess;

import javax.management.openmbean.ArrayType;

import static com.itworks.snamp.connectors.modbus.ModbusResourceConnectorConfigurationDescriptor.parseCount;

/**
 * Provides access to a set of things such as coils and registers.
 */
abstract class ModbusArrayAttributeInfo<T, A extends SlaveDeviceAccess> extends ModbusAttributeInfo<T, A> {

    protected ModbusArrayAttributeInfo(final String attributeID,
                                       final String description,
                                       final ArrayType<T> attributeType,
                                       final AttributeSpecifier specifier,
                                       final AttributeDescriptor descriptor,
                                       final A deviceAccess) {
        super(attributeID, description, attributeType, specifier, descriptor, deviceAccess);
    }

    final int getCount() throws ModbusAbsentConfigurationParameterException {
        return parseCount(getDescriptor());
    }

    final IntegerRange getRange() throws ModbusAbsentConfigurationParameterException {
        return new IntegerRange(getOffset(), getOffset() + getCount() - 1);
    }
}
