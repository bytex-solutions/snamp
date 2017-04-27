package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import javax.management.AttributeList;
import java.util.function.Consumer;

/**
 * Represents a panel with scalar values of attributes.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("panelOfAttributeValues")
public final class PanelOfAttributeValues extends TwoDimensionalChartOfAttributeValues<InstanceNameAxis, AttributeValueAxis> {
    public static final class ChartData extends AttributeChartData {
        private ChartData(final String instanceName, final Attribute attribute) {
            super(instanceName, attribute, PanelOfAttributeValues.class);
        }
    }

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

    @Override
    void fillChartData(final String resourceName, final AttributeList attributes, final Consumer<? super AttributeChartData> acceptor) {
        for(final Attribute attribute: attributes.asList())
            if(attribute.getName().equals(getAxisY().getAttributeInfo().getName()))
                acceptor.accept(new ChartData(resourceName, attribute));
    }
}
