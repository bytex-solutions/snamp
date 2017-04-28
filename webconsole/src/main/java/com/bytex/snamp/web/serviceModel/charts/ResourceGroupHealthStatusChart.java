package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Provides health status of managed resource group.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("groupHealthStatus")
public final class ResourceGroupHealthStatusChart extends AbstractChart implements TwoDimensionalChart<ResourceNameAxis, HealthStatusAxis> {
    public static final class ChartData implements com.bytex.snamp.web.serviceModel.charts.ChartData{
        private final HealthStatus status;
        private final String name;
        private final boolean summary;

        private ChartData(final String name, final HealthStatus status, final boolean summary){
            this.status = Objects.requireNonNull(status);
            this.name = Objects.requireNonNull(name);
            this.summary = summary;
        }

        static ChartData resourceStatus(final String resourceName, final HealthStatus resourceStatus){
            return new ChartData(resourceName, resourceStatus, false);
        }

        static ChartData summaryStatus(final String groupName, final HealthStatus summaryStatus){
            return new ChartData(groupName, summaryStatus, true);
        }

        @JsonProperty("summary")
        public boolean isSummary(){
            return summary;
        }

        @JsonProperty("status")
        @JsonSerialize(using = HealthStatusSerializer.class)
        public HealthStatus getStatus(){
            return status;
        }

        @JsonProperty("name")
        public String getName(){
            return name;
        }

        /**
         * Gets data for the specified dimension.
         *
         * @param dimension Zero-based dimension index.
         * @return Data for the specified dimension.
         * @throws IndexOutOfBoundsException Invalid dimension index.
         */
        @Override
        public Object getData(final int dimension) {
            switch (dimension){
                case 0:
                    return name;
                case 1:
                    return status;
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }

    private String groupName;
    private ResourceNameAxis axisX;
    private HealthStatusAxis axisY;

    public ResourceGroupHealthStatusChart(){
        groupName = "";
    }

    @JsonProperty("group")
    public void setGroupName(final String value){
        groupName = Objects.requireNonNull(value);
    }

    public String getGroupName(){
        return groupName;
    }

    public void setAxisX(final ResourceNameAxis value){
        axisX = Objects.requireNonNull(value);
    }

    public void setAxisY(final HealthStatusAxis value){
        axisY = Objects.requireNonNull(value);
    }

    /**
     * Gets information about X-axis.
     *
     * @return Information about X-axis.
     */
    @Nonnull
    @Override
    @JsonProperty("X")
    public ResourceNameAxis getAxisX() {
        if(axisX == null)
            axisX = new ResourceNameAxis();
        return axisX;
    }

    /**
     * Gets information about Y-axis.
     *
     * @return Information about Y-axis.
     */
    @Nonnull
    @Override
    @JsonProperty("Y")
    public HealthStatusAxis getAxisY() {
        if(axisY == null)
            axisY = new HealthStatusAxis();
        return axisY;
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
                return axisX;
            case 1:
                return axisY;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    private static Collection<ChartData> collectChartData(final ResourceGroupHealthStatus status, final String groupName) {
        final Collection<ChartData> result = new LinkedList<>();
        result.add(ChartData.summaryStatus(groupName, status.getSummaryStatus()));
        status.forEach((name, s) -> result.add(ChartData.resourceStatus(name, s)));
        return result;
    }

    //health status collector when supervisor is not available
    private static Collection<ChartData> collectChartData(final BundleContext context, final String groupName) {
        final class FakeResourceGroupHealthStatus extends HashMap<String, HealthStatus> implements ResourceGroupHealthStatus, Consumer<ManagedResourceConnectorClient> {
            private static final long serialVersionUID = 420503389377659109L;

            private void putStatus(final String resourceName, final ManagedResourceConnector connector) {
                put(resourceName,
                        connector.queryObject(HealthCheckSupport.class).map(HealthCheckSupport::getStatus).orElseGet(OkStatus::new));
            }

            @Override
            public void accept(final ManagedResourceConnectorClient client) {
                try {
                    putStatus(client.getManagedResourceName(), client);
                } finally {
                    client.close();
                }
            }
        }
        final FakeResourceGroupHealthStatus status = new FakeResourceGroupHealthStatus();
        for (final String resourceName : ManagedResourceConnectorClient.filterBuilder().setGroupName(groupName).getResources(context))
            ManagedResourceConnectorClient.tryCreate(context, resourceName).ifPresent(status);
        return collectChartData(status, groupName);
    }

    private static Collection<ChartData> collectChartData(final HealthStatusProvider provider, final String groupName) {
        return collectChartData(provider.getStatus(), groupName);
    }

    private static Collection<ChartData> collectChartData(final BundleContext context, final SupervisorClient supervisor) {
        final String groupName = supervisor.getGroupName();
        return supervisor.queryObject(HealthStatusProvider.class)
                .map(provider -> collectChartData(provider, groupName))
                .orElseGet(() -> collectChartData(context, groupName));
    }

    /**
     * Collects chart data.
     *
     * @param context Bundle context. Cannot be {@literal null}.
     * @return Chart data series.
     * @throws Exception The data cannot be collected.
     */
    @Override
    public Collection<ChartData> collectChartData(final BundleContext context) throws Exception {
        if(isNullOrEmpty(groupName))
            return ImmutableList.of();
        final Optional<SupervisorClient> supervisor = SupervisorClient.tryCreate(context, groupName);
        return supervisor.isPresent() ? collectChartData(context, supervisor.get()) : collectChartData(context, groupName);
    }
}
