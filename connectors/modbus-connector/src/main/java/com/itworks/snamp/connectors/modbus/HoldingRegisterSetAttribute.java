package com.itworks.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.modbus.master.HoldingRegisterAccess;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

/**
 * Provides access to a set of holding registers.
 */
final class HoldingRegisterSetAttribute extends ModbusArrayAttributeInfo<short[], HoldingRegisterAccess> {
    private static final String DESCRIPTION = "A set of holding registers";

    protected HoldingRegisterSetAttribute(final String attributeID,
                                          final AttributeDescriptor descriptor,
                                          final HoldingRegisterAccess deviceAccess) throws OpenDataException {
        super(attributeID, DESCRIPTION, new ArrayType<short[]>(SimpleType.SHORT, true), AttributeSpecifier.READ_WRITE, descriptor, deviceAccess);
    }


    @Override
    protected short[] getValue(final HoldingRegisterAccess deviceAccess) throws Exception {
        final IntegerRange range = getRange();
        final Register[] registers = deviceAccess.readHoldingRegisters(getUnitID(), range.getLowerBound(), range.size());
        final short[] result = new short[registers.length];
        for(int i = 0; i < registers.length; i++)
            result[i] = registers[i].toShort();
        return result;
    }

    @Override
    protected void setValue(final HoldingRegisterAccess deviceAccess, final short[] value) throws Exception {
        final Register[] registers = new Register[value.length];
        for (int i = 0; i < value.length; i++)
            (registers[i] = new SimpleRegister()).setValue(value[i]);
        deviceAccess.writeHoldingRegisters(getUnitID(), getOffset(), registers);
    }
}
