package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.math.Correlation;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents a counter for single-channel queuing system with denials where
 * arrivals are determined by a Poisson process and job service times have an exponential distribution.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/M/M/1_queue">M/M/1 queue</a>
 */
public final class MM1Counter extends AbstractMetric {
    private final RateRecorder requestRate;
    private final TimingRecorder responseTime;
    private final Correlation rpsAndTimeCorrelation;  //correlation between rps and response time.

    public MM1Counter(final String name, final int samplingSize){
        super(name);
        requestRate = new RateRecorder(name);
        responseTime = new TimingRecorder(name);
        rpsAndTimeCorrelation = new Correlation();
    }

    public void setStartTime(final Instant value){
        requestRate.setStartTime(value);
    }

    private static double toSeconds(final Duration value){
        //value.toMillis() may return 0 when duration is less than 1 millisecond
        return value.toNanos() / 1_000_000_000D;
    }

    public void update(final Duration responseTime){
        requestRate.update();
        this.responseTime.update(responseTime);
        final double responseTimeInSeconds = toSeconds(responseTime);
        rpsAndTimeCorrelation.applyAsDouble(/*rps*/requestRate.getLastMeanRate(MetricsInterval.SECOND), /*response time*/responseTimeInSeconds);
    }

    private static long fact(long i) {
        long result = 1;
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
            return (1D - (Math.pow(intensity, channels) / channels) * denialProbability); //availability
        }
    }

    public double getMeanAvailability(final MetricsInterval interval, final int channels){
        return getAvailability(requestRate.getMeanRate(interval), toSeconds(responseTime.getMeanValue(interval)), channels);
    }

    public double getMeanAvailability(final MetricsInterval interval){
        return getMeanAvailability(interval, 1);
    }

    public double getInstantAvailability(final int channels){
        return getAvailability(requestRate.getLastMeanRate(MetricsInterval.SECOND), toSeconds(responseTime.getLastValue()), channels);
    }

    public double getInstantAvailability(){
        return getInstantAvailability(1);
    }

    public double getEfficiency(){
        final double summaryDuration = toSeconds(responseTime.getSummaryValue());
        final double uptime = toSeconds(Duration.between(requestRate.getStartTime(), Instant.now()));
        return summaryDuration / uptime;
    }

    public Duration getResponseTimeQuantile(final double quantile){
        return responseTime.getQuantile(quantile);
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
