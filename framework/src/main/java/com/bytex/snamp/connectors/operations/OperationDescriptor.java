package com.bytex.snamp.connectors.operations;

import com.bytex.snamp.KeyValueExtractor;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigParameters;
import com.bytex.snamp.connectors.ConfigurationEntityRuntimeMetadata;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenMBeanOperationInfo;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.OperationConfiguration;
import static com.bytex.snamp.connectors.operations.OperationSupport.*;
import static com.bytex.snamp.jmx.CompositeDataUtils.fillMap;

/**
 * Represents descriptor of the managed resource operation.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class OperationDescriptor extends ImmutableDescriptor implements ConfigurationEntityRuntimeMetadata<OperationConfiguration> {
    private static final long serialVersionUID = -6350507145892936614L;
    public static final OperationDescriptor EMPTY_DESCRIPTOR = new OperationDescriptor(ImmutableMap.<String, String>of());

    private OperationDescriptor(final Map<String, ?> fields){
        super(fields);
    }

    public OperationDescriptor(final OperationConfiguration config){
        this(config.getInvocationTimeout(), new ConfigParameters(config));
    }

    public OperationDescriptor(final Duration invocationTimeout,
                               final CompositeData options){
        this(getFields(invocationTimeout, options));
    }

    private static Map<String, ?> getFields(final Duration invocationTimeout,
                                            final CompositeData options){
        final Map<String, Object> fields = Maps.newHashMapWithExpectedSize(options.values().size() + 1);
        fields.put(INVOCATION_TIMEOUT_FIELD, invocationTimeout);
        fillMap(options, fields);
        return fields;
    }

    /**
     * The type of the configuration entity.
     *
     * @return The type of the configuration entity.
     */
    @Override
    public final Class<OperationConfiguration> getEntityType() {
        return OperationConfiguration.class;
    }

    /**
     * Fills the specified configuration entity.
     *
     * @param entity The configuration entity to fill.
     */
    @Override
    public final void fill(final OperationConfiguration entity) {
        for (final String fieldName : getFieldNames())
            switch (fieldName) {
                default:
                    entity.getParameters().put(fieldName, Objects.toString(getFieldValue(fieldName)));
                case OPERATION_NAME_FIELD:
            }
    }

    @Override
    public final OperationDescriptor setFields(final Map<String, ?> values){
        if(values == null || values.isEmpty()) return this;
        final Map<String, Object> newFields = Arrays.stream(getFieldNames())
                .map(KeyValueExtractor.of(this::getFieldValue))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        newFields.putAll(values);
        return new OperationDescriptor(newFields);
    }

    @Override
    public final OperationDescriptor setFields(final Descriptor values){
        return setFields(DescriptorUtils.toMap(values));
    }

    public static boolean isAsynchronous(final Descriptor descriptor){
        if(DescriptorUtils.hasField(descriptor, ASYNC_FIELD)){
            final Object result = descriptor.getFieldValue(ASYNC_FIELD);
            if(result instanceof Boolean)
                return (Boolean)result;
            else if(result instanceof String)
                return Boolean.valueOf((String)result);
        }
        return false;
    }

    public static boolean isAsynchronous(final MBeanOperationInfo metadata){
        return isAsynchronous(metadata.getDescriptor());
    }

    public final boolean isAsynchronous(){
        return isAsynchronous(this);
    }

    /**
     * Determines whether the field with the specified name is defined in this descriptor.
     * @param fieldName The name of the field to check.
     * @return {@literal true}, if the specified field exists in this descriptor; otherwise, {@literal false}.
     */
    public final boolean hasField(final String fieldName){
        return DescriptorUtils.hasField(this, fieldName);
    }

    public final  <T> T getField(final String fieldName, final Class<T> fieldType){
        return DescriptorUtils.getField(this, fieldName, fieldType);
    }

    public static Duration getInvocationTimeout(final Descriptor descriptor){
        final Object fieldValue = descriptor.getFieldValue(INVOCATION_TIMEOUT_FIELD);
        if(fieldValue instanceof Number)
            return Duration.ofMillis(((Number)fieldValue).longValue());
        else if(fieldValue instanceof Duration)
            return (Duration)fieldValue;
        else if(fieldValue instanceof CharSequence)
            return Duration.parse((CharSequence)fieldValue);
        else return null;
    }

    public final Duration getInvocationTimeout(){
        return getInvocationTimeout(this);
    }

    /**
     * Indicating that the operation with this descriptor will be added automatically by connector itself.
     * This can be happened because connector is in Smart mode.
     * @return {@literal true}, if the operation with this descriptor will be added automatically by connector itself; otherwise, {@literal false}.
     */
    @Override
    public final boolean isAutomaticallyAdded(){
        return hasField(AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration.AUTOMATICALLY_ADDED_KEY);
    }

    /**
     * Gets alternative name of the feature.
     *
     * @return Alternative name of the feature.
     * @see AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration#NAME_KEY
     */
    @Override
    public final String getAlternativeName() {
        return getField(OperationConfiguration.NAME_KEY, String.class);
    }

    public final String getName(final String defName){
        return hasField(OperationConfiguration.NAME_KEY) ? getAlternativeName() : defName;
    }

    public static String getName(final MBeanOperationInfo metadata){
        return DescriptorUtils.getField(metadata.getDescriptor(),
                OperationConfiguration.NAME_KEY,
                String.class,
                metadata.getName());
    }

    public static WellKnownType getReturnType(final MBeanOperationInfo operationInfo) {
        if(operationInfo instanceof OpenMBeanOperationInfo)
            return WellKnownType.getType(((OpenMBeanOperationInfo)operationInfo).getReturnOpenType());
        else return WellKnownType.getType(operationInfo.getReturnType());
    }
}
