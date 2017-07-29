package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import javax.management.AttributeList;
import java.util.function.Consumer;

/**
 * Represents bar chart with horizontal bars where X-axis contains attribute value and Y-axis contains instance names.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("horizontalBarChartOfAttributeValues")
public final class HorizontalBarChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues<AttributeValueAxis, ResourceNameAxis> {
    public static final class ChartData extends AttributeChartData {
        private ChartData(final String instanceName, final Attribute attribute) {
            super(instanceName, attribute, HorizontalBarChartOfAttributeValues.class);
        }
    }

    @Nonnull
    @Override
    protected ResourceNameAxis createDefaultAxisY() {
        return new ResourceNameAxis();
    }

    @Nonnull
    @Override
    protected AttributeValueAxis createDefaultAxisX() {
        return new AttributeValueAxis();
    }

    @Override
    void fillChartData(final String resourceName, final AttributeList attributes, final Consumer<? super AttributeChartData> acceptor) {
        for (final Attribute attribute : attributes.asList())
            if (attribute.getName().equals(getAxisX().getAttributeInfo().getName()))
                acceptor.accept(new ChartData(resourceName, attribute));
    }
}
