package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.web.serviceModel.managedResources.AttributeInformation;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@JsonTypeName("attributeValue")
public final class AttributeValueAxis extends Axis {
    private AttributeInformation attributeInfo;

    public AttributeValueAxis(){
        attributeInfo = null;
    }

    public AttributeValueAxis(final AttributeInformation source){
        this.attributeInfo = Objects.requireNonNull(source);
    }

    @JsonProperty("sourceAttribute")
    @Nonnull
    public AttributeInformation getAttributeInfo(){
        if(attributeInfo == null)
            attributeInfo = new AttributeInformation();
        return attributeInfo;
    }

    public void setAttributeInfo(@Nonnull final AttributeInformation value){
        attributeInfo = Objects.requireNonNull(value);
    }

    /**
     * Gets unit of measurement associated with this axis.
     *
     * @return Unit of measurement associated with this axis.
     */
    @Override
    @JsonIgnore
    @Nonnull
    public String getUOM() {
        return attributeInfo != null ? attributeInfo.getUOM() : "";
    }
}
