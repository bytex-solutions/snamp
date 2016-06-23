package com.bytex.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.modbus.master.InputDiscreteAccess;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

/**
 * Provides access to a range of input discretes.
 */
final class InputDiscreteSetAttribute extends ModbusArrayAttributeInfo<boolean[], InputDiscreteAccess> {
    private static final String DESCRIPTION = "A set of input discretes";
    private static final long serialVersionUID = 5270513533820737433L;

    InputDiscreteSetAttribute(final String attributeID,
                              final AttributeDescriptor descriptor,
                              final InputDiscreteAccess deviceAccess) throws OpenDataException {
        super(attributeID, DESCRIPTION, new ArrayType<boolean[]>(SimpleType.BOOLEAN, true), AttributeSpecifier.READ_ONLY, descriptor, deviceAccess);
    }


    @Override
    protected boolean[] getValue(final InputDiscreteAccess deviceAccess) throws ModbusException, ModbusAbsentConfigurationParameterException {
        final IntegerRange range = getRange();
        final BitVector coils = deviceAccess.readInputDiscretes(getUnitID(), range.getLowerBound(), range.size());
        final boolean[] result = new boolean[coils.size()];
        for(int i = 0; i < coils.size(); i++)
            result[i] = coils.getBit(i);
        return result;
    }
}
