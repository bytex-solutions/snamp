package com.bytex.snamp.connectors.modbus;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.modbus.master.SlaveDeviceAccess;

import javax.management.openmbean.ArrayType;

import static com.bytex.snamp.connectors.modbus.ModbusResourceConnectorConfigurationDescriptor.parseCount;

/**
 * Provides access to a set of things such as coils and registers.
 */
abstract class ModbusArrayAttributeInfo<T, A extends SlaveDeviceAccess> extends ModbusAttributeInfo<T, A> {

    private static final long serialVersionUID = 8018051076191542627L;

    ModbusArrayAttributeInfo(final String attributeID,
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
