package com.itworks.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.Modbus;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import javax.management.Descriptor;
import java.util.Map;
import java.util.Objects;

import static com.itworks.snamp.jmx.DescriptorUtils.*;

/**
 * Provides configuration metadata of Modbus Connector.
 * This class cannot be inherited.
 */
final class ModbusResourceConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String OFFSET_PARAM = "offset";
    private static final String COUNT_PARAM = "count";
    private static final String RECORD_SIZE_PARAM = "recordSize";
    private static final String UNIT_ID_PARAM = "unitID";
    private static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";
    private static final String RETRY_COUNT_PARAM = "retryCount";

    private static final class AttributeConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeConfiguration";

        private AttributeConfigurationDescriptor(){
            super(RESOURCE_NAME, AttributeConfiguration.class, OFFSET_PARAM, COUNT_PARAM);
        }
    }

    private static final class ConnectorConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorConfiguration";

        private ConnectorConfigurationDescriptor(){
            super(RESOURCE_NAME, ManagedResourceConfiguration.class, RESOURCE_NAME, SOCKET_TIMEOUT_PARAM);
        }
    }

    ModbusResourceConnectorConfigurationDescriptor(){
        super(new AttributeConfigurationDescriptor(), new ConnectorConfigurationDescriptor());
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
        if(hasField(descriptor, UNIT_ID_PARAM))
            return Integer.parseInt(getField(descriptor, UNIT_ID_PARAM, String.class));
        else return Modbus.DEFAULT_UNIT_ID;
    }

    static int parseSocketTimeout(final Map<String, String> parameters){
        if(parameters.containsKey(SOCKET_TIMEOUT_PARAM))
            return Integer.parseInt(parameters.get(SOCKET_TIMEOUT_PARAM));
        else return 2000;
    }

    static int parseRetryCount(final Map<String, String> parameters){
        if(parameters.containsKey(RETRY_COUNT_PARAM))
            return Integer.parseInt(parameters.get(RETRY_COUNT_PARAM));
        else return 3;
    }

    static int parseRecordSize(final Descriptor descriptor) throws ModbusAbsentConfigurationParameterException {
        if(hasField(descriptor, RECORD_SIZE_PARAM))
            return Integer.parseInt(getField(descriptor, RECORD_SIZE_PARAM, String.class));
        else throw new ModbusAbsentConfigurationParameterException(RECORD_SIZE_PARAM);
    }
}
