package com.itworks.snamp.connectors.modbus;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;

import javax.management.Descriptor;
import static com.itworks.snamp.jmx.DescriptorUtils.*;

/**
 * Created by RSakno on 17.07.2015.
 */
final class ModbusResourceConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String OFFSET_PARAM = "offset";
    private static final String COUNT_PARAM = "count";

    static int parseOffset(final Descriptor descriptor) throws ModbusAbsentConfigurationParameterException{
        if(hasField(descriptor, OFFSET_PARAM))
            return Integer.parseInt(getField(descriptor, OFFSET_PARAM, String.class));
        else throw new ModbusAbsentConfigurationParameterException(OFFSET_PARAM);
    }

    static int parseCount(final Descriptor descriptor) throws ModbusAbsentConfigurationParameterException{
        if(hasField(descriptor, COUNT_PARAM))
            return Integer.parseInt(getField(descriptor, COUNT_PARAM, String.class));
        else throw new ModbusAbsentConfigurationParameterException(COUNT_PARAM);
    }
}
