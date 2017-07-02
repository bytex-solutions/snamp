package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.modbus.transport.ModbusMaster;
import com.ghgande.j2mod.modbus.ModbusException;

import javax.management.openmbean.SimpleType;

/**
 * Provides access to input register.
 */
final class InputRegisterAttribute extends ModbusAttributeInfo<Short> {
    static final String NAME = "inputRegister";
    private static final String DESCRIPTION = "Represents input register";
    private static final long serialVersionUID = -892885813426264736L;

    InputRegisterAttribute(final String attributeID, final AttributeDescriptor descriptor) {
        super(attributeID, DESCRIPTION, SimpleType.SHORT, AttributeSpecifier.READ_ONLY, descriptor);
    }

    @Override
    Short getValue(final ModbusMaster deviceAccess) throws ModbusException, ModbusAbsentConfigurationParameterException {
        return deviceAccess.readInputRegister(getUnitID(), getOffset()).toShort();
    }
}
