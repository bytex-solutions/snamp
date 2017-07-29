package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.moa.Correlation;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static com.bytex.snamp.moa.Q.getAvailability;

/**
 * Represents a gauge for queuing system with denials where
 * arrivals are determined by a Poisson process and job service times have an exponential distribution.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/M/M/1_queue">M/M/1 queue</a>
 */
public final class ArrivalsRecorder extends RatedTimeRecorder implements Arrivals {
    private static final double NANOS_IN_SECOND = Duration.ofSeconds(1L).toNanos();
    private static final long serialVersionUID = 6146322787615499495L;
    private final Correlation rpsAndTimeCorrelation;  //correlation between rps and response time.
    private int channels;
    private Instant startTime;

    public ArrivalsRecorder(final String name, final int samplingSize){
        super(name, samplingSize, NANOS_IN_SECOND);
        rpsAndTimeCorrelation = new Correlation();
        channels = 1;
        startTime = Instant.now();
    }

    public ArrivalsRecorder(final String name){
        this(name, AbstractNumericGauge.DEFAULT_SAMPLING_SIZE);
    }

    private ArrivalsRecorder(final ArrivalsRecorder source){
        super(source);
        rpsAndTimeCorrelation = source.rpsAndTimeCorrelation.clone();
        startTime = source.startTime;
    }

    @Override
    public int getChannels() {
        return channels;
    }

    public void setChannels(final int value){
        if(value < 1)
            throw new IllegalArgumentException("Number of channels cannot be less than 1");
        channels = value;
    }

    @Override
    public ArrivalsRecorder clone() {
        return new ArrivalsRecorder(this);
    }

    private static double toSeconds(final Duration value) {
        //value.toMillis() may return 0 when duration is less than 1 second
        return value.getSeconds() + value.getNano() / NANOS_IN_SECOND;
    }

    @Override
    protected void writeValue(final Duration value) {
        super.writeValue(value);
        rpsAndTimeCorrelation.applyAsDouble(getTotalRate(), toSeconds(getSummaryValue()));
    }

    @Override
    public double getLastMeanAvailability(final MetricsInterval interval) {
        return getAvailability(getLastRate(interval), toSeconds(getMeanValue(interval)), channels);
    }

    @Override
    public double getInstantAvailability(){
        return getAvailability(getLastRate(MetricsInterval.SECOND), toSeconds(getLastValue()), channels);
    }

    /**
     * Gets ratio between summary duration of all requests and server uptime.
     * @return Utilization of server uptime, in percents.
     */
    @Override
    public double getEfficiency(){
        final double summaryDuration = toSeconds(getSummaryValue());
        final double uptime = toSeconds(Duration.between(startTime, Instant.now()));
        return Double.min(summaryDuration / uptime, 1D);
    }

    public void setStartTime(@Nonnull final Instant value){
        startTime = Objects.requireNonNull(value);
    }

    /**
     * Gets correlation between arrivals and response time.
     * @return The correlation between arrivals and response time.
     */
    @Override
    public double getCorrelation(){
        return rpsAndTimeCorrelation.getAsDouble();
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        super.reset();
        rpsAndTimeCorrelation.reset();
    }
}
