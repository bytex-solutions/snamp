package com.itworks.snamp.connectors.attributes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.itworks.snamp.Attribute;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.ConfigParameters;
import com.itworks.snamp.connectors.ConfigurationEntityRuntimeMetadata;
import com.itworks.snamp.jmx.DescriptorUtils;
import com.itworks.snamp.jmx.WellKnownType;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenType;
import java.util.Map;
import java.util.Objects;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.connectors.attributes.AttributeSupport.*;
import static com.itworks.snamp.jmx.CompositeDataUtils.fillMap;

/**
 * Represents attribute descriptor.
 * with attribute metadata.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class AttributeDescriptor extends ImmutableDescriptor implements ConfigurationEntityRuntimeMetadata<AttributeConfiguration> {
    private static final long serialVersionUID = -516459089021572254L;
    public static final AttributeDescriptor EMPTY_DESCRIPTOR = new AttributeDescriptor(ImmutableMap.<String, String>of());

    /**
     * Initializes a new attribute descriptor using attribute configuration.
     * @param attributeConfig The attribute configuration used to create descriptor. Cannot be {@literal null}.
     */
    public AttributeDescriptor(final AttributeConfiguration attributeConfig){
        this(attributeConfig.getAttributeName(),
                attributeConfig.getReadWriteTimeout(),
                new ConfigParameters(attributeConfig));
    }

    /**
     * Initializes a new attribute descriptor.
     * @param attributeName The original name of the connected attribute.
     * @param readWriteTimeout Attribute read/write timeout.
     * @param options Attribute connection options.
     */
    public AttributeDescriptor(final String attributeName,
                               final TimeSpan readWriteTimeout,
                               final CompositeData options){
        this(getFields(attributeName, readWriteTimeout, options));
    }

    private AttributeDescriptor(final Map<String, ?> fields){
        super(fields);
    }

    private static Map<String, ?> getFields(final String attributeName,
                                            final TimeSpan readWriteTimeout,
                                            final CompositeData options){
        final Map<String, Object> fields = Maps.newHashMapWithExpectedSize(options.values().size() + 3);
        fields.put(ATTRIBUTE_NAME_FIELD, attributeName);
        fields.put(READ_WRITE_TIMEOUT_FIELD, readWriteTimeout);
        fillMap(options, fields);
        return fields;
    }

    public final String getAttributeName(){
        return getAttributeName(this);
    }

    public final TimeSpan getReadWriteTimeout(){
        return getReadWriteTimeout(this);
    }

    public final String getDescription(){
        return getDescription(this);
    }

    /**
     * Returns a type of the configuration entity.
     *
     * @return A type of the configuration entity.
     * @see com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration
     * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration
     * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration
     * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration
     */
    @Override
    public final Class<AttributeConfiguration> getEntityType() {
        return AttributeConfiguration.class;
    }

    /**
     * Fills the specified configuration entity.
     *
     * @param entity The configuration entity to fill.
     */
    @Override
    public final void fill(final AttributeConfiguration entity) {
        entity.setAttributeName(getAttributeName());
        entity.setReadWriteTimeout(getReadWriteTimeout());
        for (final String fieldName : getFieldNames())
            switch (fieldName) {
                default: entity.getParameters().put(fieldName, Objects.toString(getFieldValue(fieldName)));
                case ATTRIBUTE_NAME_FIELD:
                case READ_WRITE_TIMEOUT_FIELD:
            }
    }

    @Override
    public final AttributeDescriptor setFields(final Map<String, ?> values){
        if(values == null || values.isEmpty()) return this;
        final String[] fields = getFieldNames();
        final Map<String, Object> newFields = Maps.newHashMapWithExpectedSize(fields.length + values.size());
        for(final String name: fields)
            newFields.put(name, getFieldValue(name));
        newFields.putAll(values);
        return new AttributeDescriptor(newFields);
    }

    @Override
    public final AttributeDescriptor setFields(final Descriptor values){
        return setFields(DescriptorUtils.toMap(values));
    }

    public static String getAttributeName(final Descriptor metadata){
        return DescriptorUtils.getField(metadata, ATTRIBUTE_NAME_FIELD, String.class);
    }

    public static String getAttributeName(final MBeanAttributeInfo metadata){
        return getAttributeName(metadata.getDescriptor());
    }

    public static TimeSpan getReadWriteTimeout(final Descriptor metadata){
        final Object timeout = metadata.getFieldValue(READ_WRITE_TIMEOUT_FIELD);
        if(timeout instanceof TimeSpan)
            return (TimeSpan)timeout;
        else if(timeout instanceof Number)
            return new TimeSpan(((Number)timeout).longValue());
        else if(timeout instanceof String)
            return new TimeSpan(Long.parseLong(timeout.toString()));
        else return null;
    }

    public static TimeSpan getReadWriteTimeout(final MBeanAttributeInfo metadata){
        return getReadWriteTimeout(metadata.getDescriptor());
    }

    public String getDescription(final Descriptor metadata){
        return DescriptorUtils.getField(metadata, DESCRIPTION_FIELD, String.class);
    }

    public String getDescription(final MBeanAttributeInfo metadata) {
        return metadata.getDescription();
    }

    public static OpenType<?> getOpenType(final Descriptor metadata){
        return DescriptorUtils.getField(metadata, OPEN_TYPE, OpenType.class);
    }

    public static OpenType<?> getOpenType(final MBeanAttributeInfo metadata) {
        if(metadata instanceof OpenMBeanAttributeInfo)
            return ((OpenMBeanAttributeInfo) metadata).getOpenType();
        OpenType<?> result = getOpenType(metadata.getDescriptor());
        if(result == null){
            final WellKnownType knownType = WellKnownType.getType(metadata.getType());
            result = knownType != null ? knownType.getOpenType() : null;
        }
        return result;
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
