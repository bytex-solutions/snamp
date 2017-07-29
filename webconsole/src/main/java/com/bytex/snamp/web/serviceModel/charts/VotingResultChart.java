package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.elasticity.ElasticityManager;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.osgi.framework.BundleContext;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Provides voting state.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("votesForScaling")
public final class VotingResultChart extends AbstractChart {

    public static final class ChartData implements com.bytex.snamp.web.serviceModel.charts.ChartData{
        private final double value;
        private final double castingVoteWeight;

        private ChartData(final ElasticityManager manager) {
            castingVoteWeight = manager.getCastingVoteWeight();
            final double scaleIn = manager.getVotesForScaleIn();
            final double scaleOut = manager.getVotesForScaleOut();
            value = scaleIn > scaleOut ? -scaleIn : scaleOut;
        }

        @JsonProperty("votingResult")
        public double getVotingResult(){
            return value;
        }

        @JsonProperty("castingVote")
        public double getCastingVoteWeight() {
            return castingVoteWeight;
        }
        
        @Override
        public Object getData(final int dimension) {
            switch (dimension){
                case 0:
                    return value;
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }

    private String groupName;
    private NumericAxis axis;

    @JsonProperty("group")
    public void setGroupName(final String value) {
        groupName = Objects.requireNonNull(value);
    }

    public String getGroupName() {
        return groupName;
    }

    /**
     * Gets number of dimensions.
     *
     * @return Number of dimensions.
     */
    @Override
    @JsonIgnore
    public int getDimensions() {
        return 1;
    }

    @JsonProperty("X")
    public NumericAxis getAxis(){
        if(axis == null){
            axis = new NumericAxis();
            axis.setUOM("weight");
            axis.setName("votesForScaling");
        }
        return axis;
    }

    public void setAxis(final NumericAxis value){
        axis = value;
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
                return getAxis();
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
    public Iterable<ChartData> collectChartData(final BundleContext context) throws Exception {
        if (!isNullOrEmpty(groupName)) {
            final Optional<SupervisorClient> supervisorRef = SupervisorClient.tryCreate(context, groupName);
            if (supervisorRef.isPresent())
                try (final SupervisorClient client = supervisorRef.get()) {
                    return client.queryObject(ElasticityManager.class)
                            .map(ChartData::new)
                            .map(Collections::singletonList)
                            .orElseGet(Collections::emptyList);
                }
        }
        return Collections.emptyList();
    }
}
