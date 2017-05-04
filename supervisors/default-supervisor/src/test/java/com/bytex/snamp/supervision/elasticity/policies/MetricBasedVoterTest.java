package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.moa.DoubleReservoir;
import com.bytex.snamp.moa.RangeUtils;
import com.bytex.snamp.moa.ReduceOperation;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * Test for {@link MetricBasedVoter}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MetricBasedVoterTest extends Assert {
    @Test
    public void votingTest() throws InterruptedException {
        final double WEIGHT = 10D;
        final MetricBasedVoter voter = new MetricBasedVoter("dummy",
                WEIGHT,
                RangeUtils.parseDoubleRange("[3‥5]"));
        voter.setValuesAggregator(ReduceOperation.MAX);
        final DoubleReservoir reservoir = new DoubleReservoir(2);
        reservoir.add(3D);
        reservoir.add(4D);
        assertEquals(0D, voter.vote(reservoir), 0.01D);
        reservoir.reset();
        reservoir.add(3D);
        reservoir.add(5D);
        assertEquals(0D, voter.vote(reservoir), 0.01D);
        reservoir.reset();
        reservoir.add(6D);
        assertEquals(WEIGHT, voter.vote(reservoir), 0.01D);
        //set incremental option
        voter.reset();
        voter.setIncrementalVoteWeight(true);
        voter.setObservationTime(Duration.ofMillis(100));
        reservoir.reset();
        reservoir.add(6D);
        assertEquals(0D, voter.vote(reservoir), 0.01D);
        Thread.sleep(202);
        assertEquals(WEIGHT * 2, voter.vote(reservoir), 0.01D);
        Thread.sleep(102);
        assertEquals(WEIGHT * 3, voter.vote(reservoir), 0.01D);
        reservoir.reset();
        reservoir.add(3D);
        assertEquals(0D, voter.vote(reservoir), 0.01D);
    }
}
