package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.WellKnownType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.management.MBeanAttributeInfo;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents information about attribute.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class AttributeInformation {
    private String name;
    private String unitOfMeasurement;
    private WellKnownType type;
    private String description;

    public AttributeInformation(final MBeanAttributeInfo attributeInfo){
        name = attributeInfo.getName();
        type = AttributeDescriptor.getType(attributeInfo);
        unitOfMeasurement = DescriptorUtils.getUOM(attributeInfo.getDescriptor());
        description = nullToEmpty(attributeInfo.getDescription());
    }

    public AttributeInformation(final String name, final AttributeConfiguration attributeInfo) {
        this.name = name;
        type = WellKnownType.BIG_DECIMAL;
        unitOfMeasurement = nullToEmpty(attributeInfo.getUnitOfMeasurement());
        description = nullToEmpty(attributeInfo.getDescription());
    }

    public AttributeInformation(){
        name = unitOfMeasurement = description = "";
        type = WellKnownType.STRING;
    }

    public AttributeInformation(final String name, final WellKnownType type, final String uom){
        this.name = Objects.requireNonNull(name);
        this.unitOfMeasurement = Objects.requireNonNull(uom);
        this.type = Objects.requireNonNull(type);
        this.description = "";
    }

    @JsonProperty("name")
    public String getName(){
        return name;
    }

    public void setName(final String value){
        name = nullToEmpty(value);
    }

    @JsonProperty("unitOfMeasurement")
    public String getUOM(){
        return unitOfMeasurement;
    }

    public void setUOM(final String value){
        unitOfMeasurement = nullToEmpty(value);
    }

    @JsonProperty
    public String getDescription(){
        return description;
    }

    public void setDescription(final String value){
        description = nullToEmpty(value);
    }

    @JsonProperty("type")
    @JsonSerialize(using = WellKnownTypeSerializer.class)
    @JsonDeserialize(using = WellKnownTypeDeserializer.class)
    public WellKnownType getType(){
        return type;
    }

    public void setType(final WellKnownType value){
        type = Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    private boolean equals(final AttributeInformation other){
        return Objects.equals(getName(), other.getName()) &&
                Objects.equals(getType(), other.getType());
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof AttributeInformation && equals((AttributeInformation) other);
    }
}
