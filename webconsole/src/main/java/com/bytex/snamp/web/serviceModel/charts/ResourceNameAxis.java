package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Represents axis with instance names.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("resource")
public final class ResourceNameAxis extends Axis {
    public ResourceNameAxis(){
        setName("resources");
    }

    @Override
    @JsonIgnore
    public String getUOM() {
        return "";
    }
}
