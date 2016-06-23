package com.bytex.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.modbus.master.CoilAccess;

import javax.management.openmbean.SimpleType;

/**
 * Provides access to a single coil.
 * This class cannot be inherited.
 */
final class CoilAttribute extends ModbusAttributeInfo<Boolean, CoilAccess> {
    static final String NAME = "coil";
    private static final String DESCRIPTION = "Represents single coil";
    private static final long serialVersionUID = 8791041633626442808L;

    CoilAttribute(final String attributeID, final AttributeDescriptor descriptor, final CoilAccess deviceAccess) {
        super(attributeID, DESCRIPTION, SimpleType.BOOLEAN, AttributeSpecifier.READ_WRITE, descriptor, deviceAccess);
    }

    @Override
    protected Boolean getValue(final CoilAccess deviceAccess) throws ModbusException, ModbusAbsentConfigurationParameterException {
        return deviceAccess.readCoil(getUnitID(), getOffset());
    }

    @Override
    protected void setValue(final CoilAccess deviceAccess, final Boolean value) throws ModbusException, ModbusAbsentConfigurationParameterException {
        deviceAccess.writeCoil(getUnitID(), getOffset(), value);
    }
}
