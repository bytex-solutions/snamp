package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.modbus.transport.ModbusMaster;
import com.ghgande.j2mod.modbus.ModbusException;

import javax.management.openmbean.SimpleType;

/**
 * Provides access to a single coil.
 * This class cannot be inherited.
 */
final class CoilAttribute extends ModbusAttributeInfo<Boolean> {
    static final String NAME = "coil";
    private static final String DESCRIPTION = "Represents single coil";
    private static final long serialVersionUID = 8791041633626442808L;

    CoilAttribute(final String attributeID, final AttributeDescriptor descriptor) {
        super(attributeID, DESCRIPTION, SimpleType.BOOLEAN, AttributeSpecifier.READ_WRITE, descriptor);
    }

    @Override
    Boolean getValue(final ModbusMaster deviceAccess) throws ModbusException, ModbusAbsentConfigurationParameterException {
        return deviceAccess.readCoil(getUnitID(), getOffset());
    }

    @Override
    void setValue(final ModbusMaster deviceAccess, final Boolean value) throws ModbusException, ModbusAbsentConfigurationParameterException {
        deviceAccess.writeCoil(getUnitID(), getOffset(), value);
    }
}
