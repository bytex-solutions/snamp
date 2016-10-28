package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.connector.modbus.transport.ModbusMaster;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.modbus.master.HoldingRegisterAccess;

import javax.management.openmbean.SimpleType;

/**
 * Provides access to holding register.
 */
final class HoldingRegisterAttribute extends ModbusAttributeInfo<Short> {
    static final String NAME = "holdingRegister";
    private static final String DESCRIPTION = "Represents holding register";
    private static final long serialVersionUID = 4476436987210604936L;

    HoldingRegisterAttribute(final String attributeID, final AttributeDescriptor descriptor) {
        super(attributeID, DESCRIPTION, SimpleType.SHORT, AttributeSpecifier.READ_ONLY, descriptor);
    }

    @Override
    protected Short getValue(final ModbusMaster deviceAccess) throws ModbusException, ModbusAbsentConfigurationParameterException {
        return deviceAccess.readHoldingRegister(getUnitID(), getOffset()).toShort();
    }

    @Override
    protected void setValue(final ModbusMaster deviceAccess, final Short value) throws ModbusException, ModbusAbsentConfigurationParameterException {
        final Register reg = new SimpleRegister(0);
        reg.setValue(value);
        deviceAccess.writeHoldingRegister(getUnitID(), getOffset(), reg);
    }
}
