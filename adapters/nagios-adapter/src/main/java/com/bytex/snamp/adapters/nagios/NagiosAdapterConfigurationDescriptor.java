package com.bytex.snamp.adapters.nagios;

import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import javax.management.Descriptor;
import java.util.Objects;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.jmx.DescriptorUtils.*;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class NagiosAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String SERVICE_NAME_PARAM = "serviceName";
    private static final String LABEL_PARAM = "label";
    private static final String CRIT_THRESHOLD_PARAM = "criticalThreshold";
    private static final String WARN_THRESHOLD_PARAM = "warningThreshold";
    private static final String UOM_PARAM = UNIT_OF_MEASUREMENT_FIELD;
    private static final String MAX_VALUE_PARAM = MAX_VALUE_FIELD;
    private static final String MIN_VALUE_PARAM = MIN_VALUE_FIELD;

    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeParameters";

        private AttributeConfigurationInfo(){
            super(RESOURCE_NAME,
                    AttributeConfiguration.class,
                    SERVICE_NAME_PARAM,
                    LABEL_PARAM,
                    CRIT_THRESHOLD_PARAM,
                    WARN_THRESHOLD_PARAM,
                    UOM_PARAM,
                    MAX_VALUE_PARAM,
                    MIN_VALUE_PARAM);
        }
    }

    NagiosAdapterConfigurationDescriptor(){
        super(new AttributeConfigurationInfo());
    }

    static String getServiceName(final Descriptor descriptor, final String defaultService) {
        return getField(descriptor, SERVICE_NAME_PARAM, String.class, defaultService);
    }

    static String getUnitOfMeasurement(final Descriptor descr){
        return getUOM(descr);
    }

    static String getLabel(final Descriptor descr, final String defaultLabel){
        return getField(descr, LABEL_PARAM, String.class, defaultLabel);
    }

    static String getMaxValue(final Descriptor descr){
        return Objects.toString(getRawMaxValue(descr), "");
    }

    static String getMinValue(final Descriptor descr){
        return Objects.toString(getRawMinValue(descr), "");
    }

    static String getCritThreshold(final Descriptor descr){
        return Objects.toString(descr.getFieldValue(CRIT_THRESHOLD_PARAM), "");
    }

    static String getWarnThreshold(final Descriptor descr){
        return Objects.toString(descr.getFieldValue(WARN_THRESHOLD_PARAM), "");
    }
}
