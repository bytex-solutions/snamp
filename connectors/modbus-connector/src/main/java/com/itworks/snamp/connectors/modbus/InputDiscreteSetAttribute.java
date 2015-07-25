package com.itworks.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.cmd.ReadFileRecordTest;
import com.ghgande.j2mod.modbus.msg.ReadFileRecordRequest;
import com.ghgande.j2mod.modbus.msg.ReadFileRecordResponse;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordRequest;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordResponse;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.modbus.master.InputDiscreteAccess;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

/**
 * Provides access to a range of input discretes.
 */
final class InputDiscreteSetAttribute extends ModbusArrayAttributeInfo<boolean[], InputDiscreteAccess> {
    private static final String DESCRIPTION = "A set of input discretes";

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
