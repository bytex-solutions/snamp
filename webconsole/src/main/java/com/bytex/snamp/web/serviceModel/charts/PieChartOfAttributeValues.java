package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("pieChartOfAttributeValues")
public final class PieChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues<InstanceNameAxis, AttributeValueAxis> {
    @Nonnull
    @Override
    protected InstanceNameAxis createDefaultAxisX() {
        return new InstanceNameAxis();
    }

    @Nonnull
    @Override
    protected AttributeValueAxis createDefaultAxisY() {
        return new AttributeValueAxis();
    }
}
