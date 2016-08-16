package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.configuration.*;
import com.ghgande.j2mod.modbus.Modbus;

import javax.management.Descriptor;
import java.util.Map;

import static com.bytex.snamp.jmx.DescriptorUtils.*;

/**
 * Provides configuration metadata of Modbus Connector.
 * This class cannot be inherited.
 */
final class ModbusResourceConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String OFFSET_PARAM = "offset";
    private static final String COUNT_PARAM = "count";
    private static final String RECORD_SIZE_PARAM = "recordSize";
    private static final String UNIT_ID_PARAM = "unitID";
    private static final String CONNECTION_TIMEOUT_PARAM = "connectionTimeout";
    private static final String RETRY_COUNT_PARAM = "retryCount";

    private static final class AttributeConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeConfiguration";

        private AttributeConfigurationDescriptor(){
            super(RESOURCE_NAME, AttributeConfiguration.class, OFFSET_PARAM, COUNT_PARAM, RECORD_SIZE_PARAM, UNIT_ID_PARAM);
        }
    }

    private static final class ConnectorConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorConfiguration";

        private ConnectorConfigurationDescriptor(){
            super(RESOURCE_NAME, ManagedResourceConfiguration.class, CONNECTION_TIMEOUT_PARAM, RETRY_COUNT_PARAM);
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

    static int parseConnectionTimeout(final Map<String, String> parameters){
        if(parameters.containsKey(CONNECTION_TIMEOUT_PARAM))
            return Integer.parseInt(parameters.get(CONNECTION_TIMEOUT_PARAM));
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
