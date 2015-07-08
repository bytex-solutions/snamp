package com.itworks.snamp.connectors.operations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.itworks.snamp.configuration.ConfigParameters;
import com.itworks.snamp.connectors.ConfigurationEntityRuntimeMetadata;
import com.itworks.snamp.jmx.DescriptorUtils;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.CompositeData;
import java.util.Map;
import java.util.Objects;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.OperationConfiguration;
import static com.itworks.snamp.connectors.operations.OperationSupport.ASYNC_FIELD;
import static com.itworks.snamp.connectors.operations.OperationSupport.OPERATION_NAME_FIELD;
import static com.itworks.snamp.jmx.CompositeDataUtils.fillMap;

/**
 * Represents descriptor of the managed resource operation.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class OperationDescriptor extends ImmutableDescriptor implements ConfigurationEntityRuntimeMetadata<OperationConfiguration> {
    private static final long serialVersionUID = -6350507145892936614L;
    public static final OperationDescriptor EMPTY_DESCRIPTOR = new OperationDescriptor(ImmutableMap.<String, String>of());

    private OperationDescriptor(final Map<String, ?> fields){
        super(fields);
    }

    public OperationDescriptor(final OperationConfiguration config){
        this(config.getOperationName(), new ConfigParameters(config));
    }

    public OperationDescriptor(final String operationName,
                               final CompositeData options){
        this(getFields(operationName, options));
    }

    private static Map<String, ?> getFields(final String operationName,
                                            final CompositeData options){
        final Map<String, Object> fields = Maps.newHashMapWithExpectedSize(options.values().size() + 1);
        fields.put(OPERATION_NAME_FIELD, operationName);
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
        entity.setOperationName(getOperationName());
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
        final String[] fields = getFieldNames();
        final Map<String, Object> newFields = Maps.newHashMapWithExpectedSize(fields.length + values.size());
        for(final String name: fields)
            newFields.put(name, getFieldValue(name));
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

    public static String getOperationName(final Descriptor descriptor){
        return DescriptorUtils.getField(descriptor, OPERATION_NAME_FIELD, String.class);
    }

    public static String getOperationName(final MBeanOperationInfo metadata){
        return getOperationName(metadata.getDescriptor());
    }

    public final String getOperationName(){
        return getOperationName(this);
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
}
