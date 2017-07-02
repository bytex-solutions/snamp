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
 * Provides access to set of coils.
 *
 */
final class CoilSetAttribute extends ModbusArrayAttributeInfo<boolean[]> {
    private static final String DESCRIPTION = "A set of coils";
    private static final long serialVersionUID = 2220377415321325345L;

    CoilSetAttribute(final String attributeID, final AttributeDescriptor descriptor) throws OpenDataException {
        super(attributeID, DESCRIPTION, new ArrayType<>(SimpleType.BOOLEAN, true), AttributeSpecifier.READ_WRITE, descriptor);
    }

    @Override
    boolean[] getValue(final ModbusMaster deviceAccess) throws ModbusAbsentConfigurationParameterException, ModbusException {
        final IntegerRange range = getRange();
        final BitVector coils = deviceAccess.readCoils(getUnitID(), range.getLowerBound(), range.size());
        final boolean[] result = new boolean[coils.size()];
        for(int i = 0; i < coils.size(); i++)
            result[i] = coils.getBit(i);
        return result;
    }

    @Override
    void setValue(final ModbusMaster deviceAccess, final boolean[] value) throws ModbusAbsentConfigurationParameterException, ModbusException {
        final BitVector coils = new BitVector(value.length);
        for(int i = 0; i < value.length; i++)
            coils.setBit(i, value[i]);
        deviceAccess.writeCoils(getUnitID(), getOffset(), coils);
    }
}
