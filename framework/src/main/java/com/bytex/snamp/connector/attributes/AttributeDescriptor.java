package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.Convert;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.FeatureDescriptor;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.ImmutableMap;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenType;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.bytex.snamp.connector.attributes.AttributeSupport.*;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents attribute descriptor.
 * with attribute metadata.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class AttributeDescriptor extends ImmutableDescriptor implements FeatureDescriptor<AttributeConfiguration> {
    private static final long serialVersionUID = -516459089021572254L;
    public static final AttributeDescriptor EMPTY_DESCRIPTOR = new AttributeDescriptor(ImmutableMap.<String, String>of());

    /**
     * Initializes a new attribute descriptor using attribute configuration.
     * @param attributeConfig The attribute configuration used to create descriptor. Cannot be {@literal null}.
     */
    public AttributeDescriptor(final AttributeConfiguration attributeConfig){
        this(attributeConfig.getReadWriteTimeout(), attributeConfig);
    }

    /**
     * Initializes a new attribute descriptor.
     * @param readWriteTimeout Attribute read/write timeout.
     * @param options Attribute connection options.
     */
    public AttributeDescriptor(final Duration readWriteTimeout,
                               final Map<String, String> options){
        this(getFields(readWriteTimeout, options));
    }

    private AttributeDescriptor(final Map<String, ?> fields){
        super(fields);
    }

    private static Map<String, ?> getFields(final Duration readWriteTimeout,
                                            final Map<String, String> options) {
        return readWriteTimeout == null ?
                options :
                ImmutableMap.<String, Object>builder()
                        .putAll(options)
                        .put(READ_WRITE_TIMEOUT_FIELD, readWriteTimeout)
                        .build();
    }

    /**
     * Infers type of the attribute.
     * @param attribute The attribute metadata. Cannot be {@literal null}.
     * @return The well-known SNAMP type that should be recognized by gateway.
     */
    public static WellKnownType getType(final MBeanAttributeInfo attribute) {
        final OpenType<?> ot = getOpenType(attribute);
        return ot != null ? WellKnownType.getType(ot) : WellKnownType.getType(attribute.getType());
    }

    public final Duration getReadWriteTimeout(){
        return getReadWriteTimeout(this);
    }

    public final String getDescription(){
        return getDescription(this, "");
    }

    public final String getDescription(final String defval){
        final String result = getDescription();
        return isNullOrEmpty(result) ? defval : result;
    }

    /**
     * Returns a type of the configuration entity.
     *
     * @return A type of the configuration entity.
     * @see GatewayConfiguration
     * @see ManagedResourceConfiguration
     * @see EventConfiguration
     * @see AttributeConfiguration
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
                default:
                    final Object fieldValue = getFieldValue(fieldName);
                    if (fieldValue == null)
                        continue;
                    else
                        entity.put(fieldName, fieldValue.toString());
                case ATTRIBUTE_NAME_FIELD:
                case READ_WRITE_TIMEOUT_FIELD:
            }
    }

    @Override
    public final AttributeDescriptor setFields(final Map<String, ?> values){
        if(values == null || values.isEmpty()) return this;
        final Map<String, Object> newFields = DescriptorUtils.toMap(this, false);
        newFields.putAll(values);
        return new AttributeDescriptor(newFields);
    }

    @Override
    public final AttributeDescriptor setFields(final Descriptor values){
        return setFields(DescriptorUtils.toMap(values));
    }

    public static Duration getReadWriteTimeout(final Descriptor metadata) {
        return Optional.ofNullable(metadata.getFieldValue(READ_WRITE_TIMEOUT_FIELD))
                .flatMap(Convert::toDuration)
                .orElse(null);
    }

    public static Duration getReadWriteTimeout(final MBeanAttributeInfo metadata){
        return getReadWriteTimeout(metadata.getDescriptor());
    }

    public static String getDescription(final Descriptor metadata, final String defVal){
        return DescriptorUtils.getField(metadata, DESCRIPTION_FIELD, Objects::toString).orElse(defVal);
    }

    public static String getDescription(final MBeanAttributeInfo metadata) {
        return getDescription(metadata.getDescriptor(), metadata.getDescription());
    }

    public static OpenType<?> getOpenType(final Descriptor metadata){
        return DescriptorUtils.getField(metadata, OPEN_TYPE, value -> (OpenType<?>)value).orElse(null);
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

    /**
     * Sets unit of measurement for this attribute.
     * @param value UOM
     * @return A new instance of modified descriptor.
     */
    public AttributeDescriptor setUnit(final String value) {
        return setFields(ImmutableMap.of(DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD, value));
    }

    /**
     * Gets unit of measurement for this attribute.
     * @return UOM.
     */
    public final String getUnit(){
        return DescriptorUtils.getField(this, DescriptorUtils.UNIT_OF_MEASUREMENT_FIELD, Objects::toString).orElse(null);
    }

    public static String getName(final MBeanAttributeInfo metadata) {
        return FeatureDescriptor.getName(metadata.getDescriptor()).orElseGet(metadata::getName);
    }
}
