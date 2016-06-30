package com.bytex.snamp.connectors.attributes;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigParameters;
import com.bytex.snamp.connectors.ConfigurationEntityRuntimeMetadata;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenType;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.connectors.attributes.AttributeSupport.*;
import static com.bytex.snamp.jmx.CompositeDataUtils.fillMap;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents attribute descriptor.
 * with attribute metadata.
 * @author Roman Sakno
 * @version 1.2
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
        this(attributeConfig.getReadWriteTimeout(),
                new ConfigParameters(attributeConfig));
    }

    /**
     * Initializes a new attribute descriptor.
     * @param readWriteTimeout Attribute read/write timeout.
     * @param options Attribute connection options.
     */
    public AttributeDescriptor(final Duration readWriteTimeout,
                               final CompositeData options){
        this(getFields(readWriteTimeout, options));
    }

    private AttributeDescriptor(final Map<String, ?> fields){
        super(fields);
    }

    private static Map<String, ?> getFields(final Duration readWriteTimeout,
                                            final CompositeData options){
        final Map<String, Object> fields = Maps.newHashMapWithExpectedSize(options.values().size() + 1);
        fields.put(READ_WRITE_TIMEOUT_FIELD, readWriteTimeout);
        fillMap(options, fields);
        return fields;
    }

    /**
     * Infers type of the attribute.
     * @param attribute The attribute metadata. Cannot be {@literal null}.
     * @return The well-known SNAMP type that should be recognized by resource adapter.
     */
    public static WellKnownType getType(final MBeanAttributeInfo attribute) {
        final OpenType<?> ot = getOpenType(attribute);
        return ot != null ? WellKnownType.getType(ot) : WellKnownType.getType(attribute.getType());
    }

    public final Duration getReadWriteTimeout(){
        return getReadWriteTimeout(this);
    }

    public final String getDescription(){
        return getDescription(this);
    }

    public final String getDescription(final String defval){
        final String result = getDescription();
        return isNullOrEmpty(result) ? defval : result;
    }

    /**
     * Returns a type of the configuration entity.
     *
     * @return A type of the configuration entity.
     * @see com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration
     * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration
     * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration
     * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration
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
        final Map<String, Object> newFields = DescriptorUtils.toMap(this, Object.class, false);
        newFields.putAll(values);
        return new AttributeDescriptor(newFields);
    }

    @Override
    public final AttributeDescriptor setFields(final Descriptor values){
        return setFields(DescriptorUtils.toMap(values));
    }

    public static Duration getReadWriteTimeout(final Descriptor metadata) {
        final Object timeout = metadata.getFieldValue(READ_WRITE_TIMEOUT_FIELD);
        if (timeout instanceof Duration)
            return (Duration) timeout;
        else if (timeout instanceof Number)
            return Duration.ofMillis(((Number) timeout).longValue());
        else if (timeout instanceof CharSequence)
            return Duration.parse((CharSequence) timeout);
        else return null;
    }

    public static Duration getReadWriteTimeout(final MBeanAttributeInfo metadata){
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

    public final AttributeDescriptor setOpenType(final OpenType<?> value){
        return value != null ? setFields(ImmutableMap.of(OPEN_TYPE, value)) : this;
    }

    public final OpenType<?> getOpenType(){
        return getOpenType(this);
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

    /**
     * Sets unit of measurement for this attribute.
     * @param value UOM
     * @return A new instance of modified descriptor.
     */
    public AttributeDescriptor setUnit(final String value) {
        return setFields(ImmutableMap.of(DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD, value));
    }

    /**
     * Indicating that the attribute with this descriptor will be added automatically by connector itself.
     * This can be happened because connector is in Smart mode.
     * @return {@literal true}, if the attribute with this descriptor will be added automatically by connector itself; otherwise, {@literal false}.
     */
    @Override
    public final boolean isAutomaticallyAdded(){
        return hasField(AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration.AUTOMATICALLY_ADDED_KEY);
    }

    public final String getName(final String defName){
        return hasField(AttributeConfiguration.NAME_KEY) ? getAlternativeName() : defName;
    }


    @Override
    public final String getAlternativeName(){
        return getField(AttributeConfiguration.NAME_KEY, String.class);
    }

    /**
     * Gets unit of measurement for this attribute.
     * @return UOM.
     */
    public final String getUnit(){
        return getField(DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD, String.class);
    }

    public static String getName(final MBeanAttributeInfo metadata) {
        return DescriptorUtils.getField(metadata.getDescriptor(),
                AttributeConfiguration.NAME_KEY,
                String.class,
                metadata.getName());
    }
}
