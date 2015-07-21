package com.itworks.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.modbus.protocol.InputDiscreteAccess;

import javax.management.openmbean.SimpleType;

/**
 * Provides access to input discrete.
 */
public class InputDiscreteAttribute extends ModbusAttributeInfo<Boolean, InputDiscreteAccess> {
    static final String NAME = "inputDiscrete";
    private static final String DESCRIPTION = "Represents input discrete";

    protected InputDiscreteAttribute(final String attributeID, final AttributeDescriptor descriptor, final InputDiscreteAccess deviceAccess) {
        super(attributeID, DESCRIPTION, SimpleType.BOOLEAN, AttributeSpecifier.READ_ONLY, descriptor, deviceAccess);
    }

    @Override
    protected Boolean getValue(final InputDiscreteAccess deviceAccess) throws ModbusException, ModbusAbsentConfigurationParameterException {
        return deviceAccess.readInputDiscrete(getUnitID(), getOffset());
    }
}
