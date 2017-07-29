package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import javax.management.AttributeList;
import java.util.function.Consumer;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("verticalBarChartOfAttributeValues")
public final class VerticalBarChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues<ResourceNameAxis, AttributeValueAxis> {
    public static final class ChartData extends AttributeChartData {
        private ChartData(final String instanceName, final Attribute attribute) {
            super(instanceName, attribute, VerticalBarChartOfAttributeValues.class);
        }
    }

    @Nonnull
    @Override
    protected ResourceNameAxis createDefaultAxisX() {
        return new ResourceNameAxis();
    }

    @Nonnull
    @Override
    protected AttributeValueAxis createDefaultAxisY() {
        return new AttributeValueAxis();
    }

    @Override
    void fillChartData(final String resourceName, final AttributeList attributes, final Consumer<? super AttributeChartData> acceptor) {
        for(final Attribute attribute: attributes.asList())
            if(attribute.getName().equals(getAxisY().getAttributeInfo().getName()))
                acceptor.accept(new ChartData(resourceName, attribute));
    }
}
