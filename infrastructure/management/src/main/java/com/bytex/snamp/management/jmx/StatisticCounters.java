package com.bytex.snamp.management.jmx;

import com.bytex.snamp.concurrent.TimeLimitedLong;
import org.osgi.service.log.LogService;

import java.time.Duration;
import java.util.function.LongConsumer;

/**
 * The type Statistic counters.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class StatisticCounters {
    private static final class LogEventCounter extends TimeLimitedLong {
        private static final long serialVersionUID = -470327556746659772L;

        private LogEventCounter(final long timeout){
            super(0L, Duration.ofMillis(timeout));
        }

        private LogEventCounter(final LogEventCounter source){
            super(source);
        }

        @Override
        public LogEventCounter clone() {
            return new LogEventCounter(this);
        }

        @Override
        protected long accumulate(final long delta) {
            return addAndGet(delta);
        }
    }

    private volatile long timeout;
    private volatile LogEventCounter numberOfFaults;
    private volatile LogEventCounter numberOfWarnings;
    private volatile LogEventCounter numberOfDebugMessages;
    private volatile LogEventCounter numberOfInformationMessages;

    /**
     * Initializes a new countdown timer.
     *
     * @param frequency The initial timer value.
     * @throws IllegalArgumentException initial is null.
     */
    StatisticCounters(final Duration frequency) {
        updateCounters(this.timeout = frequency.toMillis());
    }

    private void updateCounters(final long timeout){
        numberOfFaults = new LogEventCounter(timeout);
        numberOfDebugMessages = new LogEventCounter(timeout);
        numberOfInformationMessages = new LogEventCounter(timeout);
        numberOfWarnings = new LogEventCounter(timeout);
    }

    /**
     * Set renewal time.
     *
     * @param value Renewal time, in millis.
     */
    void setRenewalTime(final long value){
        timeout = value;
        updateCounters(value);
    }

    /**
     * Get renewal time.
     *
     * @return Renewal time, in millis.
     */
    long getRenewalTime(){
        return timeout;
    }

    long getValue(final int level){
        switch (level){
            case LogService.LOG_ERROR: return numberOfFaults.getAsLong();
            case LogService.LOG_WARNING: return numberOfWarnings.getAsLong();
            case LogService.LOG_DEBUG: return numberOfDebugMessages.getAsLong();
            default: return numberOfInformationMessages.getAsLong();
        }
    }
    /**
     * Increase counter.
     *
     * @param eventType the event type
     */
    void increment(final int eventType){
        final LongConsumer counter;
        switch (eventType){
            case LogService.LOG_ERROR: counter = numberOfFaults; break;
            case LogService.LOG_DEBUG: counter = numberOfDebugMessages; break;
            case LogService.LOG_WARNING: counter = numberOfWarnings; break;
            default: counter = numberOfInformationMessages; break;
        }
        counter.accept(1L);
    }
}