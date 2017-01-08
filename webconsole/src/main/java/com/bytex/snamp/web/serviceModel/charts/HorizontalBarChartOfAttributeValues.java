package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;

/**
 * Represents bar chart with horizontal bars where X-axis contains attribute value and Y-axis contains instance names.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("horizontalBarChartOfAttributeValues")
public final class HorizontalBarChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues<AttributeValueAxis, InstanceNameAxis> {
    @Nonnull
    @Override
    protected InstanceNameAxis createDefaultAxisY() {
        return new InstanceNameAxis();
    }

    @Nonnull
    @Override
    protected AttributeValueAxis createDefaultAxisX() {
        return new AttributeValueAxis();
    }
}
