package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;

/**
 * Represents line chart where X is a timestamp of attribute values; Y is an attribute value.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("lineChartOfAttributeValues")
public final class LineChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues<ChronoAxis, AttributeValueAxis> {
    @Override
    @Nonnull
    protected ChronoAxis createDefaultAxisX() {
        return new ChronoAxis();
    }

    @Override
    @Nonnull
    protected AttributeValueAxis createDefaultAxisY() {
        return new AttributeValueAxis();
    }
}
