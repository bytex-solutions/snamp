package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.connector.health.InvalidAttributeValue;
import com.bytex.snamp.connector.health.MalfunctionStatus;
import com.bytex.snamp.moa.DoubleReservoir;
import com.bytex.snamp.moa.RangeUtils;
import com.bytex.snamp.moa.ReduceOperation;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import javax.management.Attribute;
import java.io.IOException;
import java.time.Duration;

/**
 * Test for {@link AttributeBasedScalingPolicy} and {@link HealthStatusBasedScalingPolicy}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class WeightedScalingPolicyTest extends Assert {
    @Test
    public void healthStatusVoting() throws InterruptedException {
        final double WEIGHT = 8D;
        final HealthStatusBasedScalingPolicy voter = new HealthStatusBasedScalingPolicy(WEIGHT, MalfunctionStatus.Level.MODERATE);
        assertEquals(0D, voter.vote(new InvalidAttributeValue(new Attribute("a", 10), false)), 0.01D);
        assertEquals(WEIGHT, voter.vote(new InvalidAttributeValue(new Attribute("a", 10), true)), 0.01D);
        voter.reset();
        voter.setIncrementalVoteWeight(true);
        voter.setObservationTime(Duration.ofMillis(100));
        assertEquals(0, voter.vote(new InvalidAttributeValue(new Attribute("a", 10), true)), 0.01D);
        Thread.sleep(201);
        assertEquals(2 * WEIGHT, voter.vote(new InvalidAttributeValue(new Attribute("a", 10), true)), 0.01D);
        Thread.sleep(101);
        assertEquals(3 * WEIGHT, voter.vote(new InvalidAttributeValue(new Attribute("a", 10), true)), 0.01D);
        assertEquals(0, voter.vote(new InvalidAttributeValue(new Attribute("a", 10), false)), 0.01D);
    }

    @Test
    public void attributeBasedVoting() throws InterruptedException {
        final double WEIGHT = 10D;
        final AttributeBasedScalingPolicy voter = new AttributeBasedScalingPolicy("dummy",
                WEIGHT,
                RangeUtils.parseDoubleRange("[3‥5]"));
        voter.setValuesAggregator(ReduceOperation.MAX);
        final DoubleReservoir reservoir = new DoubleReservoir(2);
        reservoir.add(3D);
        reservoir.add(4D);
        assertEquals(0D, voter.vote(reservoir), 0.01D);
        reservoir.reset();
        reservoir.add(5D);
        reservoir.add(3D);
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

    @Test
    public void metricBasedSerialization() throws IOException {
        final double WEIGHT = 10D;
        final AttributeBasedScalingPolicy voter = new AttributeBasedScalingPolicy("dummy",
                WEIGHT,
                RangeUtils.parseDoubleRange("[3‥5]"));
        voter.setValuesAggregator(ReduceOperation.PERCENTILE_90);
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(voter);
        assertNotNull(json);
        final AttributeBasedScalingPolicy deserializedVoter = AttributeBasedScalingPolicy.parse(json, mapper);
        assertNotNull(deserializedVoter);
        assertEquals(voter.getAttributeName(), deserializedVoter.getAttributeName());
        assertEquals(voter.isIncrementalVoteWeight(), deserializedVoter.isIncrementalVoteWeight());
        assertEquals(voter.getVoteWeight(), deserializedVoter.getVoteWeight(), 0.01D);
        assertEquals(voter.getOperationalRange(), deserializedVoter.getOperationalRange());
        assertEquals(voter.getAggregator(), deserializedVoter.getAggregator());
    }

    @Test
    public void healthStatusBasedSerialization() throws IOException {
        final double WEIGHT = 10D;
        final HealthStatusBasedScalingPolicy voter = new HealthStatusBasedScalingPolicy(WEIGHT, MalfunctionStatus.Level.CRITICAL);
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(voter);
        assertNotNull(json);
        final HealthStatusBasedScalingPolicy deserializedVoter = HealthStatusBasedScalingPolicy.parse(json, mapper);
        assertNotNull(deserializedVoter);
        assertEquals(voter.getLevel(), deserializedVoter.getLevel());
        assertEquals(voter.isIncrementalVoteWeight(), deserializedVoter.isIncrementalVoteWeight());
        assertEquals(voter.getVoteWeight(), deserializedVoter.getVoteWeight(), 0.01D);
    }
}
