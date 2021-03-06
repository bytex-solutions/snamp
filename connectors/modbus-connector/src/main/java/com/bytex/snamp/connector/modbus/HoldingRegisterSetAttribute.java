package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.modbus.transport.ModbusMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

/**
 * Provides access to a set of holding registers.
 */
final class HoldingRegisterSetAttribute extends ModbusArrayAttributeInfo<short[]> {
    private static final String DESCRIPTION = "A set of holding registers";
    private static final long serialVersionUID = -6206738073781348952L;

    HoldingRegisterSetAttribute(final String attributeID,
                                final AttributeDescriptor descriptor) throws OpenDataException {
        super(attributeID, DESCRIPTION, new ArrayType<>(SimpleType.SHORT, true), AttributeSpecifier.READ_WRITE, descriptor);
    }


    @Override
    short[] getValue(final ModbusMaster deviceAccess) throws Exception {
        final IntegerRange range = getRange();
        final Register[] registers = deviceAccess.readHoldingRegisters(getUnitID(), range.getLowerBound(), range.size());
        final short[] result = new short[registers.length];
        for(int i = 0; i < registers.length; i++)
            result[i] = registers[i].toShort();
        return result;
    }

    @Override
    void setValue(final ModbusMaster deviceAccess, final short[] value) throws Exception {
        final Register[] registers = new Register[value.length];
        for (int i = 0; i < value.length; i++)
            (registers[i] = new SimpleRegister()).setValue(value[i]);
        deviceAccess.writeHoldingRegisters(getUnitID(), getOffset(), registers);
    }
}
