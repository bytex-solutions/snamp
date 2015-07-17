package com.itworks.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.configuration.AbsentConfigurationParameterException;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.modbus.protocol.InputRegisterAccess;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

/**
 * Provides access to a set of input registers.
 */
final class InputRegisterSetAttribute extends ModbusArrayAttributeInfo<short[], InputRegisterAccess> {
    static final String NAME = "inputRegisterSet";
    private static final String DESCRIPTION = "A set of input registers";

    InputRegisterSetAttribute(final String attributeID, final AttributeDescriptor descriptor, final InputRegisterAccess deviceAccess) throws OpenDataException {
        super(attributeID, DESCRIPTION, new ArrayType<short[]>(SimpleType.SHORT, true), AttributeSpecifier.READ_ONLY, descriptor, deviceAccess);
    }

    @Override
    protected short[] getValue(final InputRegisterAccess deviceAccess) throws ModbusException, AbsentConfigurationParameterException {
        final IntegerRange range = getRange();
        final InputRegister[] registers = deviceAccess.readInputRegisters(range.getLowerBound(), range.size());
        final short[] result = new short[registers.length];
        for(int i = 0; i < registers.length; i++)
            result[i] = registers[i].toShort();
        return result;
    }
}
