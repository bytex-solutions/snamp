package com.itworks.snamp.connectors.attributes;

import com.google.common.collect.Maps;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.ConfigParameters;
import com.itworks.snamp.connectors.ConfigurationEntityRuntimeMetadata;

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
import static com.itworks.snamp.jmx.DescriptorUtils.getField;

/**
 * Represents attribute descriptor.
 * with attribute metadata.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class AttributeDescriptor extends ImmutableDescriptor implements ConfigurationEntityRuntimeMetadata<AttributeConfiguration> {


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
        super(getFields(attributeName, readWriteTimeout, options));
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
            entity.getParameters().put(fieldName, Objects.toString(getFieldValue(fieldName)));
    }

    public static String getAttributeName(final Descriptor metadata){
        return getField(metadata, ATTRIBUTE_NAME_FIELD, String.class);
    }

    public static String getAttributeName(final MBeanAttributeInfo metadata){
        return getAttributeName(metadata.getDescriptor());
    }

    public static TimeSpan getReadWriteTimeout(final Descriptor metadata){
        return getField(metadata, READ_WRITE_TIMEOUT_FIELD, TimeSpan.class, TimeSpan.INFINITE);
    }

    public static TimeSpan getReadWriteTimeout(final MBeanAttributeInfo metadata){
        return getReadWriteTimeout(metadata.getDescriptor());
    }

    public String getDescription(final Descriptor metadata){
        return getField(metadata, DESCRIPTION_FIELD, String.class);
    }

    public String getDescription(final MBeanAttributeInfo metadata) {
        return metadata.getDescription();
    }

    public static OpenType<?> getOpenType(final Descriptor metadata){
        return getField(metadata, OPEN_TYPE, OpenType.class);
    }

    public static OpenType<?> getOpenType(final MBeanAttributeInfo metadata) {
        return metadata instanceof OpenMBeanAttributeInfo ?
                ((OpenMBeanAttributeInfo) metadata).getOpenType() :
                getOpenType(metadata.getDescriptor());
    }
}
