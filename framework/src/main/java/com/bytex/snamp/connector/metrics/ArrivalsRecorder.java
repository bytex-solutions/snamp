package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.moa.Correlation;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents a gauge for queuing system with denials where
 * arrivals are determined by a Poisson process and job service times have an exponential distribution.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/M/M/1_queue">M/M/1 queue</a>
 */
public final class ArrivalsRecorder extends RatedTimeRecorder implements Arrivals {
    private static final double NANOS_IN_SECOND = Duration.ofSeconds(1L).toNanos();
    private static final long serialVersionUID = 6146322787615499495L;
    private final Correlation rpsAndTimeCorrelation;  //correlation between rps and response time.
    private long channels;

    public ArrivalsRecorder(final String name, final int samplingSize){
        super(name, samplingSize, NANOS_IN_SECOND);
        rpsAndTimeCorrelation = new Correlation();
        channels = 1;
    }

    public ArrivalsRecorder(final String name){
        this(name, AbstractNumericGauge.DEFAULT_SAMPLING_SIZE);
    }

    private ArrivalsRecorder(final ArrivalsRecorder source){
        super(source);
        rpsAndTimeCorrelation = source.rpsAndTimeCorrelation.clone();
    }

    @Override
    public long getChannels() {
        return channels;
    }

    public void setChannels(final long value){
        if(value < 1)
            throw new IllegalArgumentException("Number of channels cannot be less than 1");
        channels = value;
    }

    @Override
    public ArrivalsRecorder clone() {
        return new ArrivalsRecorder(this);
    }

    private static double toSeconds(final Duration value){
        //value.toMillis() may return 0 when duration is less than 1 second
        return value.toNanos() / NANOS_IN_SECOND;
    }

    @Override
    protected void writeValue(final Duration value) {
        super.writeValue(value);
        final double lastMeanRate = getLastMeanRate(MetricsInterval.SECOND);
        final double lastMeanResponseTime = toSeconds(getLastMeanValue(MetricsInterval.SECOND));
        rpsAndTimeCorrelation.applyAsDouble(/*rps*/lastMeanRate, /*response time*/lastMeanResponseTime);
    }

    private static double fact(long i) {
        if(i > 170)     //170! is the maximum factorial value for DOUBLE data type
            return Double.POSITIVE_INFINITY;
        double result = 1D;
        while (i > 1)
            result *= i--;
        return result;
    }

    private static double getAvailability(final double rps, final double responseTimeInSeconds, final long channels) {
        if (channels == 0)
            return 0;
        else if (responseTimeInSeconds == 0D || rps == 0D)
            return 1D;
        else {
            //http://latex.codecogs.com/gif.latex?\rho=\lambda\times&space;t
            final double intensity = rps * responseTimeInSeconds; //workload intensity
            //http://latex.codecogs.com/gif.latex?p_{0}=\frac{1}{\sum_{i=0}^{k}\frac{\rho^{i}}{i!}}
            double denialProbability = 1D;
            for (int i = 1; i <= channels; i++)
                denialProbability += (Math.pow(intensity, i) / fact(i));
            denialProbability = 1D / denialProbability;
            //http://latex.codecogs.com/gif.latex?P=1-\frac{\rho^{k}}{k!}\rho_{0}
            return denialProbability == 0D ?
                    1D :         //little optimization
                    (1D - (Math.pow(intensity, channels) / channels) * denialProbability);  //availability
        }
    }

    @Override
    public double getLastMeanAvailability(final MetricsInterval interval){
        return getAvailability(getLastMeanRate(interval), toSeconds(getLastMeanValue(interval)), channels);
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
        final double uptime = toSeconds(Duration.between(getStartTime(), Instant.now()));
        return Double.min(summaryDuration / uptime, 1D);
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
