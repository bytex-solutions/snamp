package com.bytex.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.modbus.master.HoldingRegisterAccess;

import javax.management.openmbean.SimpleType;

/**
 * Provides access to holding register.
 */
final class HoldingRegisterAttribute extends ModbusAttributeInfo<Short, HoldingRegisterAccess> {
    static final String NAME = "holdingRegister";
    private static final String DESCRIPTION = "Represents holding register";

    protected HoldingRegisterAttribute(final String attributeID, final AttributeDescriptor descriptor, final HoldingRegisterAccess deviceAccess) {
        super(attributeID, DESCRIPTION, SimpleType.SHORT, AttributeSpecifier.READ_ONLY, descriptor, deviceAccess);
    }

    @Override
    protected Short getValue(final HoldingRegisterAccess deviceAccess) throws ModbusException, ModbusAbsentConfigurationParameterException {
        return deviceAccess.readHoldingRegister(getUnitID(), getOffset()).toShort();
    }

    @Override
    protected void setValue(final HoldingRegisterAccess deviceAccess, final Short value) throws ModbusException, ModbusAbsentConfigurationParameterException {
        final Register reg = new SimpleRegister(0);
        reg.setValue(value);
        deviceAccess.writeHoldingRegister(getUnitID(), getOffset(), reg);
    }
}
