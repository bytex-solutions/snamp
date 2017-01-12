package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.node.ObjectNode;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents line chart where X is a timestamp of attribute values; Y is an attribute value.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("lineChartOfAttributeValues")
public final class LineChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues<ChronoAxis, AttributeValueAxis> {
    public static final class ChartData extends AttributeChartData {
        private final Date timeStamp;

        private ChartData(final String instanceName, final Attribute attribute) {
            super(instanceName, attribute, LineChartOfAttributeValues.class);
            timeStamp = new Date();
        }

        @JsonIgnore
        public final Date getTimeStamp(){
            return timeStamp;
        }

        @Nonnull
        @Override
        protected ObjectNode toJsonNode() throws JsonProcessingException {
            final ObjectNode node = super.toJsonNode();
            node.put("timeStamp", timeStamp.getTime());
            return node;
        }

        @Override
        public Object getData(final int dimension) {
            switch (dimension) {
                case 0:
                    return getTimeStamp();
                case 1:
                    return getAttribute().getValue();
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }

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

    @Override
    Optional<ChartData> createChartData(final String instanceName, final Attribute attribute) {
        return hasInstance(instanceName) && Objects.equals(attribute.getName(), getAxisY().getAttributeInfo().getName()) ?
                Optional.of(new ChartData(instanceName, attribute)) :
                Optional.empty();
    }
}
