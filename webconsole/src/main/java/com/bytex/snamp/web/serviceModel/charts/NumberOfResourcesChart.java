package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.json.InstantSerializer;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * Represents number of resources in the specified group.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("numberOfResourcesInGroup")
public final class NumberOfResourcesChart extends AbstractChart implements TwoDimensionalChart<ChronoAxis, NumericAxis> {
    public static final class ChartData implements com.bytex.snamp.web.serviceModel.charts.ChartData{
        private final Instant timeStamp;
        private final int count;

        private ChartData(final int count){
            this.count = count;
            timeStamp = Instant.now();
        }

        @JsonProperty("count")
        public int getCount(){
            return count;
        }

        @JsonProperty("timeStamp")
        @JsonSerialize(using = InstantSerializer.class)
        public Instant getTimeStamp(){
            return timeStamp;
        }

        @Override
        public Object getData(final int dimension) {
            switch (dimension){
                case 0:
                    return getTimeStamp();
                case 1:
                    return getCount();
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }

    private ChronoAxis x;
    private NumericAxis y;
    private String groupName;

    @JsonProperty("group")
    public void setGroupName(final String value){
        groupName = value;
    }

    public String getGroupName(){
        return groupName;
    }

    public void setAxisX(final ChronoAxis value){
        x = value;
    }

    public void setAxisY(final NumericAxis value){
        y = value;
    }

    /**
     * Gets information about X-axis.
     *
     * @return Information about X-axis.
     */
    @Nonnull
    @Override
    @JsonProperty("X")
    public ChronoAxis getAxisX() {
        if(x == null)
            x = new ChronoAxis();
        return x;
    }

    /**
     * Gets information about Y-axis.
     *
     * @return Information about Y-axis.
     */
    @Nonnull
    @Override
    @JsonProperty("Y")
    public NumericAxis getAxisY() {
        if(y == null) {
            y = new NumericAxis();
            y.setName("resources");
            y.setUOM("resources");
        }
        return y;
    }

    /**
     * Gets number of dimensions.
     *
     * @return Number of dimensions.
     */
    @Override
    @JsonIgnore
    public int getDimensions() {
        return 2;
    }

    /**
     * Gets configuration of the axis associated with the dimension.
     *
     * @param dimensionIndex Zero-based index of the dimension.
     * @return Axis configuration.
     * @throws IndexOutOfBoundsException Invalid dimension index.
     */
    @Override
    public Axis getAxis(final int dimensionIndex) {
        switch (dimensionIndex){
            case 0:
                return getAxisX();
            case 1:
                return getAxisY();
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Collects chart data.
     *
     * @param context Bundle context. Cannot be {@literal null}.
     * @return Chart data series.
     * @throws Exception The data cannot be collected.
     */
    @Override
    public Iterable<? extends ChartData> collectChartData(final BundleContext context) throws Exception {
        final ChartData data = new ChartData(ManagedResourceConnectorClient.selector().setGroupName(groupName).getResources(context).size());
        return ImmutableList.of(data);
    }
}
