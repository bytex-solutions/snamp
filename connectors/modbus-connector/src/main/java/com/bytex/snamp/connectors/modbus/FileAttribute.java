package com.bytex.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.modbus.master.FileAccess;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import static com.bytex.snamp.connectors.modbus.ModbusResourceConnectorConfigurationDescriptor.parseRecordSize;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class FileAttribute extends ModbusArrayAttributeInfo<short[], FileAccess> {
    static final String NAME = "file";
    private static final String DESCRIPTION = "Read or write file records";
    private static final long serialVersionUID = 94068295080475933L;

    FileAttribute(final String attributeID,
                  final AttributeDescriptor descriptor,
                  final FileAccess deviceAccess) throws OpenDataException {
        super(attributeID,
                DESCRIPTION,
                new ArrayType<short[]>(SimpleType.SHORT, true),
                AttributeSpecifier.READ_WRITE,
                descriptor,
                deviceAccess);
    }

    private int getRecordSize() throws ModbusAbsentConfigurationParameterException {
        return parseRecordSize(getDescriptor());
    }

    @Override
    protected short[] getValue(final FileAccess deviceAccess) throws ModbusAbsentConfigurationParameterException, ModbusException {
        return deviceAccess.readFile(getUnitID(), getOffset(), getCount(), getRecordSize());
    }

    @Override
    protected void setValue(final FileAccess deviceAccess, final short[] value) throws ModbusAbsentConfigurationParameterException, ModbusException {
        deviceAccess.writeFile(getUnitID(), getOffset(), getRecordSize(), value);
    }
}
