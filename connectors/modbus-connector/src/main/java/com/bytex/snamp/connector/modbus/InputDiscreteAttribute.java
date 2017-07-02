package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.modbus.transport.ModbusMaster;
import com.ghgande.j2mod.modbus.ModbusException;

import javax.management.openmbean.SimpleType;

/**
 * Provides access to input discrete.
 */
class InputDiscreteAttribute extends ModbusAttributeInfo<Boolean> {
    static final String NAME = "inputDiscrete";
    private static final String DESCRIPTION = "Represents input discrete";
    private static final long serialVersionUID = -856369386115138586L;

    InputDiscreteAttribute(final String attributeID, final AttributeDescriptor descriptor) {
        super(attributeID, DESCRIPTION, SimpleType.BOOLEAN, AttributeSpecifier.READ_ONLY, descriptor);
    }

    @Override
    Boolean getValue(final ModbusMaster deviceAccess) throws ModbusException, ModbusAbsentConfigurationParameterException {
        return deviceAccess.readInputDiscrete(getUnitID(), getOffset());
    }
}
