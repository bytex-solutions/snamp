package com.itworks.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.modbus.protocol.CoilAccess;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

/**
 * Provides access to set of coils.
 *
 */
final class CoilSetAttribute extends ModbusArrayAttributeInfo<boolean[], CoilAccess> {
    static final String NAME = "coils";
    private static final String DESCRIPTION = "A set of coils";

    CoilSetAttribute(final String attributeID, final AttributeDescriptor descriptor, final CoilAccess deviceAccess) throws OpenDataException {
        super(attributeID, DESCRIPTION, new ArrayType<boolean[]>(SimpleType.BOOLEAN, true), AttributeSpecifier.READ_WRITE, descriptor, deviceAccess);
    }

    @Override
    protected boolean[] getValue(final CoilAccess deviceAccess) throws ModbusAbsentConfigurationParameterException, ModbusException {
        final IntegerRange range = getRange();
        final BitVector coils = deviceAccess.readCoils(range.getLowerBound(), range.size());
        final boolean[] result = new boolean[coils.size()];
        for(int i = 0; i < coils.size(); i++)
            result[i] = coils.getBit(i);
        return result;
    }

    @Override
    protected void setValue(final CoilAccess deviceAccess, final boolean[] value) throws ModbusAbsentConfigurationParameterException, ModbusException {
        final BitVector coils = new BitVector(value.length);
        for(int i = 0; i < value.length; i++)
            coils.setBit(i, value[i]);
        deviceAccess.writeCoils(getOffset(), coils);
    }
}
