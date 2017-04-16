package com.bytex.snamp.connector.modbus;

import com.bytex.snamp.Convert;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.ghgande.j2mod.modbus.Modbus;

import javax.management.Descriptor;
import java.util.Map;
import java.util.OptionalInt;

import static com.bytex.snamp.MapUtils.getValueAsInt;
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
        return getFieldIfPresent(descriptor,OFFSET_PARAM, Convert::toInt, ModbusAbsentConfigurationParameterException::new)
                .orElseThrow(NumberFormatException::new);
    }

    static boolean hasCount(final Descriptor descriptor){
        return hasField(descriptor, COUNT_PARAM);
    }

    static int parseCount(final Descriptor descriptor) throws ModbusAbsentConfigurationParameterException{
        return getFieldIfPresent(descriptor, COUNT_PARAM, Convert::toInt, ModbusAbsentConfigurationParameterException::new)
                .orElseThrow(NumberFormatException::new);
    }

    static int parseUnitID(final Descriptor descriptor) {
        return getField(descriptor, UNIT_ID_PARAM, Convert::toInt)
                .orElseGet(OptionalInt::empty)
                .orElse(Modbus.DEFAULT_UNIT_ID);
    }

    static int parseConnectionTimeout(final Map<String, String> parameters){
        return getValueAsInt(parameters, CONNECTION_TIMEOUT_PARAM, Integer::parseInt).orElse(2000);
    }

    static int parseRetryCount(final Map<String, String> parameters){
        return getValueAsInt(parameters, RETRY_COUNT_PARAM, Integer::parseInt).orElse(3);
    }

    static int parseRecordSize(final Descriptor descriptor) throws ModbusAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RECORD_SIZE_PARAM, Convert::toInt, ModbusAbsentConfigurationParameterException::new)
                .orElseThrow(NumberFormatException::new);
    }
}
