package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.modbus.transport.ModbusMaster;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

/**
 * Provides access to a set of input registers.
 */
final class InputRegisterSetAttribute extends ModbusArrayAttributeInfo<short[]> {
    private static final String DESCRIPTION = "A set of input registers";
    private static final long serialVersionUID = -4261475447443004126L;

    InputRegisterSetAttribute(final String attributeID, final AttributeDescriptor descriptor) throws OpenDataException {
        super(attributeID, DESCRIPTION, new ArrayType<>(SimpleType.SHORT, true), AttributeSpecifier.READ_ONLY, descriptor);
    }

    @Override
    short[] getValue(final ModbusMaster deviceAccess) throws ModbusException, AbsentConfigurationParameterException {
        final IntegerRange range = getRange();
        final InputRegister[] registers = deviceAccess.readInputRegisters(getUnitID(), range.getLowerBound(), range.size());
        final short[] result = new short[registers.length];
        for(int i = 0; i < registers.length; i++)
            result[i] = registers[i].toShort();
        return result;
    }
}
