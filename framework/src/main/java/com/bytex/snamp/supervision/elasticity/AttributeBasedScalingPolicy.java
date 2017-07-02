package com.bytex.snamp.supervision.elasticity;

import com.bytex.snamp.moa.ReduceOperation;
import com.google.common.collect.Range;

import javax.annotation.Nonnull;

/**
 * Represents scaling policy based on values of the attribute collected from all members of resource group.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface AttributeBasedScalingPolicy extends WeightedScalingPolicy {
    /**
     * Gets name of the attribute.
     * @return Name of the attribute.
     */
    @Nonnull
    String getAttributeName();

    /**
     * Gets operational range of attribute values.
     * @return Operation range.
     * @implSpec If aggregated value <pre>X</pre> is less than operational range then this policy will vote for downscale.
     *     If aggregated value <pre>X</pre> is greater than operational range then this policy will vote for upscale.
     */
    @Nonnull
    Range<Double> getOperationalRange();

    /**
     * Gets way of aggregation applied to attributes values.
     * @return Aggregation method.
     */
    @Nonnull
    ReduceOperation getAggregator();

    /**
     * Gets advice about more optimal operational range.
     * @return More optimal operational range; or empty range if recommendation is not supported.
     */
    @Nonnull
    Range<Double> getRecommendation();
}
