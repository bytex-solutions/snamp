package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;

/**
 * Represents a panel with scalar values of attributes.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("panelOfAttributeValues")
public final class PanelOfAttributeValues extends TwoDimensionalChartOfAttributeValues<InstanceNameAxis, AttributeValueAxis> {
    @Override
    @Nonnull
    protected InstanceNameAxis createDefaultAxisX() {
        return new InstanceNameAxis();
    }

    @Override
    @Nonnull
    protected AttributeValueAxis createDefaultAxisY() {
        return new AttributeValueAxis();
    }
}
