package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.modbus.transport.ModbusMaster;

import javax.management.openmbean.OpenType;

import static com.bytex.snamp.connector.modbus.ModbusResourceConnectorConfigurationDescriptor.parseOffset;
import static com.bytex.snamp.connector.modbus.ModbusResourceConnectorConfigurationDescriptor.parseUnitID;

/**
 *
 */
abstract class ModbusAttributeInfo<T> extends AbstractOpenAttributeInfo {
    private static final long serialVersionUID = -1718119127501806280L;

    ModbusAttributeInfo(final String attributeID,
                        final String description,
                        final OpenType<T> attributeType,
                        final AttributeSpecifier specifier,
                        final AttributeDescriptor descriptor) {
        super(attributeID, attributeType, description, specifier, descriptor);
    }

    final int getOffset() throws ModbusAbsentConfigurationParameterException {
        return parseOffset(getDescriptor());
    }

    final int getUnitID() {
        return parseUnitID(getDescriptor());
    }

    abstract T getValue(final ModbusMaster deviceAccess) throws Exception;

    void setValue(final ModbusMaster deviceAccess, final T value) throws Exception{
        throw new UnsupportedOperationException(String.format("Attribute '%s' is read-only", getName()));
    }
}
