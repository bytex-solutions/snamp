package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("chrono")
public final class ChronoAxis extends Axis {
    public static final String UNIT_OF_MEASUREMENT = "seconds";

    @Override
    @JsonIgnore
    public String getUOM() {
        return UNIT_OF_MEASUREMENT;
    }
}
