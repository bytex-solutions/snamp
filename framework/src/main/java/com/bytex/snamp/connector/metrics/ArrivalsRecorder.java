package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.math.Correlation;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

/**
 * Represents a gauge for queuing system with denials where
 * arrivals are determined by a Poisson process and job service times have an exponential distribution.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/M/M/1_queue">M/M/1 queue</a>
 */
public final class ArrivalsRecorder extends AbstractMetric implements Consumer<Duration>, Arrivals {
    private static final double NANOS_IN_SECOND = 1_000_000_000D;
    private final RateRecorder requestRate;
    private final TimingRecorder responseTime;
    private final Correlation rpsAndTimeCorrelation;  //correlation between rps and response time.

    public ArrivalsRecorder(final String name, final int samplingSize){
        super(name);
        requestRate = new RateRecorder(name);
        responseTime = new TimingRecorder(name, samplingSize, NANOS_IN_SECOND);
        rpsAndTimeCorrelation = new Correlation();
    }

    public ArrivalsRecorder(final String name){
        this(name, AbstractNumericGauge.DEFAULT_SAMPLING_SIZE);
    }

    public void setStartTime(final Instant value){
        requestRate.setStartTime(value);
    }

    private static double toSeconds(final Duration value){
        //value.toMillis() may return 0 when duration is less than 1 second
        return value.toNanos() / NANOS_IN_SECOND;
    }

    @Override
    public void accept(final Duration rt){
        requestRate.mark();
        responseTime.accept(rt);
        final double lastMeanRate = requestRate.getLastMeanRate(MetricsInterval.SECOND);
        final double lastMeanResponseTime = toSeconds(responseTime.getLastMeanValue(MetricsInterval.SECOND));
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

    private static double getAvailability(final double rps, final double responseTimeInSeconds, final int channels) {
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
    public double getMeanAvailability(final MetricsInterval interval, final int channels){
        return getAvailability(requestRate.getMeanRate(interval), toSeconds(responseTime.getMeanValue()), channels);
    }

    public double getMeanAvailability(final MetricsInterval interval){
        return getMeanAvailability(interval, 1);
    }

    @Override
    public double getInstantAvailability(final int channels){
        return getAvailability(requestRate.getLastRate(MetricsInterval.SECOND), toSeconds(responseTime.getLastValue()), channels);
    }

    /**
     * Gets instant availability of the single channel using characteristics of arrivals measured by this object.
     * @return Instant availability.
     */
    public double getInstantAvailability(){
        return getInstantAvailability(1);
    }

    /**
     * Gets ratio between summary duration of all requests and server uptime.
     * @return Utilization of server uptime, in percents.
     */
    @Override
    public double getEfficiency(){
        final double summaryDuration = toSeconds(responseTime.getSummaryValue());
        final double uptime = toSeconds(Duration.between(requestRate.getStartTime(), Instant.now()));
        return summaryDuration / uptime;
    }

    /**
     * Gets rate of arrivals.
     * @return Rate of arrivals.
     */
    @Override
    public Rate getRequestRate(){
        return requestRate;
    }

    /**
     * Gets measurement of response time.
     * @return Measurement of response time.
     */
    @Override
    public Timing getResponseTiming(){
        return responseTime;
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
        requestRate.reset();
        responseTime.reset();
        rpsAndTimeCorrelation.reset();
    }
}
