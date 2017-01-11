package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents bar chart with horizontal bars where X-axis contains attribute value and Y-axis contains instance names.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("horizontalBarChartOfAttributeValues")
public final class HorizontalBarChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues<AttributeValueAxis, InstanceNameAxis> {
    public static final class ChartData extends AttributeChartData {
        private ChartData(final String instanceName, final Attribute attribute) {
            super(instanceName, attribute, PanelOfAttributeValues.class);
        }
    }

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

    @Override
    Optional<ChartData> createChartData(final String instanceName, final Attribute attribute) {
        return hasInstance(instanceName) && Objects.equals(attribute.getName(), getAxisX().getAttributeInfo().getName()) ?
                Optional.of(new ChartData(instanceName, attribute)) :
                Optional.empty();
    }
}
