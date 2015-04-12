package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides configuration schema of this resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AggregatorConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String SOURCE_PARAM = "source";
    private static final String FOREIGN_ATTRIBUTE_PARAM = "foreignAttribute";
    private static final String PATTERN_PARAM = "pattern";
    private static final String FIRST_FOREIGN_ATTRIBUTE_PARAM = "firstForeignAttribute";
    private static final String SECOND_FOREIGN_ATTRIBUTE_PARAM = "secondForeignAttribute";
    private static final String COMPARER_PARAM = "comparer";
    private static final String VALUE_PARAM = "value";
    private static final String TIME_INTERVAL_PARAM = "timeInterval";
    private static final String FIELD_PATH_PARAM = "fieldPath";

    private static final class AttributeConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeParameters";

        private AttributeConfigurationDescriptor(){
            super(AttributeConfiguration.class,
                    SOURCE_PARAM,
                    FOREIGN_ATTRIBUTE_PARAM,
                    PATTERN_PARAM,
                    FIRST_FOREIGN_ATTRIBUTE_PARAM,
                    SECOND_FOREIGN_ATTRIBUTE_PARAM,
                    COMPARER_PARAM,
                    VALUE_PARAM,
                    TIME_INTERVAL_PARAM);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc != null ? loc : Locale.getDefault());
        }
    }

    AggregatorConnectorConfigurationDescriptor(){
        super(new AttributeConfigurationDescriptor());
    }

    private static String getAttributeParameter(final AttributeDescriptor descriptor,
                                                final String parameterName) throws AbsentAggregatorAttributeParameter{
        if(descriptor.hasField(parameterName))
            return descriptor.getField(parameterName, String.class);
        else throw new AbsentAggregatorAttributeParameter(parameterName);
    }

    static String getSourceManagedResource(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter{
        return getAttributeParameter(descriptor, SOURCE_PARAM);
    }

    static String getForeignAttributeName(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter{
        return getAttributeParameter(descriptor, FOREIGN_ATTRIBUTE_PARAM);
    }

    static String getFirstForeignAttributeName(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter{
        return getAttributeParameter(descriptor, FIRST_FOREIGN_ATTRIBUTE_PARAM);
    }

    static String getSecondForeignAttributeName(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter{
        return getAttributeParameter(descriptor, SECOND_FOREIGN_ATTRIBUTE_PARAM);
    }

    static String getPattern(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter{
        return getAttributeParameter(descriptor, PATTERN_PARAM);
    }

    static Comparison getComparisonType(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter{
        return Comparison.parse(getAttributeParameter(descriptor, COMPARER_PARAM));
    }

    static String getUserDefinedValue(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter{
        return getAttributeParameter(descriptor, VALUE_PARAM);
    }

    static long getTimeIntervalInMillis(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        return Long.parseLong(getAttributeParameter(descriptor, TIME_INTERVAL_PARAM));
    }

    static CompositeDataPath getFieldPath(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter{
        if(descriptor.hasField(FIELD_PATH_PARAM))
            return new CompositeDataPath(descriptor.getField(FIELD_PATH_PARAM, String.class));
        else throw new AbsentAggregatorAttributeParameter(FIELD_PATH_PARAM);
    }
}
