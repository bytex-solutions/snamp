package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("verticalBarChartOfAttributeValues")
public final class VerticalBarChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues<InstanceNameAxis, AttributeValueAxis> {
    public static final class ChartData extends AttributeChartData {
        private ChartData(final String instanceName, final Attribute attribute) {
            super(instanceName, attribute, VerticalBarChartOfAttributeValues.class);
        }
    }

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

    @Override
    Optional<ChartData> createChartData(final String instanceName, final Attribute attribute) {
        return hasInstance(instanceName) && Objects.equals(attribute.getName(), getAxisY().getAttributeInfo().getName()) ?
                Optional.of(new ChartData(instanceName, attribute)) :
                Optional.empty();
    }
}
