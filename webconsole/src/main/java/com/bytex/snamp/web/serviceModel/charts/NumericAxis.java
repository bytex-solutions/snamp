package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("value")
public final class NumericAxis extends Axis {
    private String uom;

    public NumericAxis(){
        setName("value");
    }

    public void setUOM(final String value){
        uom = nullToEmpty(value);
    }

    /**
     * Gets unit of measurement associated with this axis.
     *
     * @return Unit of measurement associated with this axis.
     */
    @Override
    @JsonProperty("uom")
    public String getUOM() {
        return uom;
    }
}
