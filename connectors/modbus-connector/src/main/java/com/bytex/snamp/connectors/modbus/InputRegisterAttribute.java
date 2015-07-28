package com.bytex.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.modbus.master.InputRegisterAccess;

import javax.management.openmbean.SimpleType;

/**
 * Provides access to input register.
 */
final class InputRegisterAttribute extends ModbusAttributeInfo<Short, InputRegisterAccess> {
    static final String NAME = "inputRegister";
    private static final String DESCRIPTION = "Represents input register";

    protected InputRegisterAttribute(final String attributeID, final AttributeDescriptor descriptor, final InputRegisterAccess deviceAccess) {
        super(attributeID, DESCRIPTION, SimpleType.SHORT, AttributeSpecifier.READ_ONLY, descriptor, deviceAccess);
    }

    @Override
    protected Short getValue(final InputRegisterAccess deviceAccess) throws ModbusException, ModbusAbsentConfigurationParameterException {
        return deviceAccess.readInputRegister(getUnitID(), getOffset()).toShort();
    }
}