package com.bytex.snamp.connector.operations;

import com.bytex.snamp.Convert;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.connector.FeatureDescriptor;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.ImmutableMap;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfo;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.connector.attributes.AttributeSupport.DESCRIPTION_FIELD;
import static com.bytex.snamp.connector.operations.OperationSupport.*;

/**
 * Represents descriptor of the managed resource operation.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class OperationDescriptor extends ImmutableDescriptor implements FeatureDescriptor<OperationConfiguration> {
    private static final long serialVersionUID = -6350507145892936614L;
    public static final OperationDescriptor EMPTY_DESCRIPTOR = new OperationDescriptor(ImmutableMap.<String, String>of());

    private OperationDescriptor(final Map<String, ?> fields){
        super(fields);
    }

    public OperationDescriptor(final OperationConfiguration config){
        this(config.getInvocationTimeout(), config);
    }

    public OperationDescriptor(final Duration invocationTimeout,
                               final Map<String, String> options){
        this(getFields(invocationTimeout, options));
    }

    private static Map<String, ?> getFields(final Duration invocationTimeout,
                                            final Map<String, String> options) {
        return invocationTimeout == null ?
                options :
                ImmutableMap.<String, Object>builder()
                        .putAll(options)
                        .put(INVOCATION_TIMEOUT_FIELD, invocationTimeout)
                        .build();
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
        entity.setInvocationTimeout(getInvocationTimeout());
        for (final String fieldName : getFieldNames())
            switch (fieldName) {
                default:
                    entity.put(fieldName, Objects.toString(getFieldValue(fieldName)));
                case OPERATION_NAME_FIELD:
            }
    }

    @Override
    public final OperationDescriptor setFields(final Map<String, ?> values){
        if(values == null || values.isEmpty()) return this;
        final Map<String, Object> newFields = DescriptorUtils.toMap(this, false);
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

    public static Duration getInvocationTimeout(final Descriptor descriptor){
        return Convert.toDuration(descriptor.getFieldValue(INVOCATION_TIMEOUT_FIELD)).orElse(null);
    }

    public final Duration getInvocationTimeout(){
        return getInvocationTimeout(this);
    }


    public static String getName(final MBeanOperationInfo metadata){
        return FeatureDescriptor.getName(metadata.getDescriptor()).orElseGet(metadata::getName);
    }

    public static WellKnownType getReturnType(final MBeanOperationInfo operationInfo) {
        if(operationInfo instanceof OpenMBeanOperationInfo)
            return WellKnownType.getType(((OpenMBeanOperationInfo)operationInfo).getReturnOpenType());
        else return WellKnownType.getType(operationInfo.getReturnType());
    }

    public static String getDescription(final Descriptor metadata, final String defval){
        return DescriptorUtils.getField(metadata, DESCRIPTION_FIELD, Objects::toString).orElse(defval);
    }

    public String getDescription(final String defval){
        return getDescription(this, defval);
    }

    public String getDescription() {
        return getDescription(this, "");
    }
}
