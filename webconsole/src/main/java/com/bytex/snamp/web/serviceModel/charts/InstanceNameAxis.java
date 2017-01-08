package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Represents axis with instance names.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class InstanceNameAxis extends Axis {
    public InstanceNameAxis(){
        setName("instances");
    }

    @Override
    @JsonIgnore
    public String getUOM() {
        return "";
    }
}
