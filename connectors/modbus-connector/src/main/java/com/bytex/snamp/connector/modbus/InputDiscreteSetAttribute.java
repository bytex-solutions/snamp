package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.modbus.transport.ModbusMaster;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.util.BitVector;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

/**
 * Provides access to a range of input discretes.
 */
final class InputDiscreteSetAttribute extends ModbusArrayAttributeInfo<boolean[]> {
    private static final String DESCRIPTION = "A set of input discretes";
    private static final long serialVersionUID = 5270513533820737433L;

    InputDiscreteSetAttribute(final String attributeID,
                              final AttributeDescriptor descriptor) throws OpenDataException {
        super(attributeID, DESCRIPTION, new ArrayType<>(SimpleType.BOOLEAN, true), AttributeSpecifier.READ_ONLY, descriptor);
    }

    @Override
    boolean[] getValue(final ModbusMaster deviceAccess) throws ModbusException, ModbusAbsentConfigurationParameterException {
        final IntegerRange range = getRange();
        final BitVector coils = deviceAccess.readInputDiscretes(getUnitID(), range.getLowerBound(), range.size());
        final boolean[] result = new boolean[coils.size()];
        for(int i = 0; i < coils.size(); i++)
            result[i] = coils.getBit(i);
        return result;
    }
}
