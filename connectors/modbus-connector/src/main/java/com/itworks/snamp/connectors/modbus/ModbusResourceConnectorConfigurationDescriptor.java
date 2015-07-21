package com.itworks.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.Modbus;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import javax.management.Descriptor;
import java.util.Map;

import static com.itworks.snamp.jmx.DescriptorUtils.*;

/**
 * Provides configuration metadata of Modbus Connector.
 * This class cannot be inherited.
 */
final class ModbusResourceConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String OFFSET_PARAM = "offset";
    private static final String COUNT_PARAM = "count";
    private static final String UNITID_PARAM = "unitID";
    private static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";

    private static final class AttributeConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeConfiguration";

        private AttributeConfigurationDescriptor(){
            super(RESOURCE_NAME, AttributeConfiguration.class, OFFSET_PARAM, COUNT_PARAM);
        }
    }

    ModbusResourceConnectorConfigurationDescriptor(){
        super(new AttributeConfigurationDescriptor());
    }

    static int parseOffset(final Descriptor descriptor) throws ModbusAbsentConfigurationParameterException{
        if(hasField(descriptor, OFFSET_PARAM))
            return Integer.parseInt(getField(descriptor, OFFSET_PARAM, String.class));
        else throw new ModbusAbsentConfigurationParameterException(OFFSET_PARAM);
    }

    static boolean hasCount(final Descriptor descriptor){
        return hasField(descriptor, COUNT_PARAM);
    }

    static int parseCount(final Descriptor descriptor) throws ModbusAbsentConfigurationParameterException{
        if(hasCount(descriptor))
            return Integer.parseInt(getField(descriptor, COUNT_PARAM, String.class));
        else throw new ModbusAbsentConfigurationParameterException(COUNT_PARAM);
    }

    static int parseUnitID(final Descriptor descriptor){
        if(hasField(descriptor, UNITID_PARAM))
            return Integer.parseInt(getField(descriptor, UNITID_PARAM, String.class));
        else return Modbus.DEFAULT_UNIT_ID;
    }

    static int parseSocketTimeout(final Map<String, String> parameters){
        if(parameters.containsKey(SOCKET_TIMEOUT_PARAM))
            return Integer.parseInt(parameters.get(SOCKET_TIMEOUT_PARAM));
        else return 2000;
    }
}
