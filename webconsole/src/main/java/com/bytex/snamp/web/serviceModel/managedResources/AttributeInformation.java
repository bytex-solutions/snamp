package com.bytex.snamp.web.serviceModel.managedResources;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.WellKnownType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import static com.google.common.base.MoreObjects.firstNonNull;

import javax.management.MBeanAttributeInfo;
import java.util.Objects;

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
        description = firstNonNull(attributeInfo.getDescription(), "");
    }

    public AttributeInformation(final String name, final AttributeConfiguration attributeInfo) {
        this.name = name;
        type = WellKnownType.BIG_DECIMAL;
        unitOfMeasurement = firstNonNull(attributeInfo.getUnitOfMeasurement(), "");
        description = firstNonNull(attributeInfo.getDescription(), "");
    }

    public AttributeInformation(){
        name = unitOfMeasurement = description = "";
        type = WellKnownType.STRING;
    }

    public AttributeInformation(final String name, final WellKnownType type, final String uom){
        this.name = Objects.requireNonNull(name);
        this.unitOfMeasurement = Objects.requireNonNull(uom);
        this.type = Objects.requireNonNull(type);
    }

    @JsonProperty("name")
    public String getName(){
        return name;
    }

    public void setName(final String value){
        name = Objects.requireNonNull(value);
    }

    @JsonProperty("unitOfMeasurement")
    public String getUOM(){
        return unitOfMeasurement;
    }

    public void setUOM(final String value){
        unitOfMeasurement = Objects.requireNonNull(value);
    }

    @JsonProperty
    public String getDescription(){
        return description;
    }

    public void setDescription(final String value){
        description = Objects.requireNonNull(value);
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
}
