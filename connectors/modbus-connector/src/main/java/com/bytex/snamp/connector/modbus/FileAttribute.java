package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.modbus.transport.ModbusMaster;
import com.ghgande.j2mod.modbus.ModbusException;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

import static com.bytex.snamp.connector.modbus.ModbusResourceConnectorConfigurationDescriptor.parseRecordSize;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class FileAttribute extends ModbusArrayAttributeInfo<short[]> {
    static final String NAME = "file";
    private static final String DESCRIPTION = "Read or write file records";
    private static final long serialVersionUID = 94068295080475933L;

    FileAttribute(final String attributeID,
                  final AttributeDescriptor descriptor) throws OpenDataException {
        super(attributeID,
                DESCRIPTION,
                new ArrayType<>(SimpleType.SHORT, true),
                AttributeSpecifier.READ_WRITE,
                descriptor);
    }

    private int getRecordSize() throws ModbusAbsentConfigurationParameterException {
        return parseRecordSize(getDescriptor());
    }

    @Override
    short[] getValue(final ModbusMaster deviceAccess) throws ModbusAbsentConfigurationParameterException, ModbusException {
        return deviceAccess.readFile(getUnitID(), getOffset(), getCount(), getRecordSize());
    }

    @Override
    void setValue(final ModbusMaster deviceAccess, final short[] value) throws ModbusAbsentConfigurationParameterException, ModbusException {
        deviceAccess.writeFile(getUnitID(), getOffset(), getRecordSize(), value);
    }
}
