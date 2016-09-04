package com.bytex.snamp.management.jmx;

import com.bytex.snamp.concurrent.TimeLimitedLong;
import org.osgi.service.log.LogService;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

/**
 * The type Statistic counters.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class StatisticCounters {
    private static final class LogEventCounter extends TimeLimitedLong {
        private LogEventCounter(final AtomicLong timeout){
            super(0L, timeout::get);
        }

        @Override
        protected long accumulate(final long delta) {
            return addAndGet(delta);
        }
    }

    private final AtomicLong timeout;
    private final LogEventCounter numberOfFaults;
    private final LogEventCounter numberOfWarnings;
    private final LogEventCounter numberOfDebugMessages;
    private final LogEventCounter numberOfInformationMessages;

    /**
     * Initializes a new countdown timer.
     *
     * @param frequency The initial timer value.
     * @throws IllegalArgumentException initial is null.
     */
    StatisticCounters(final Duration frequency) {
        this.timeout = new AtomicLong(frequency.toMillis());
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
        timeout.set(value);
    }

    /**
     * Get renewal time.
     *
     * @return Renewal time, in millis.
     */
    long getRenewalTime(){
        return timeout.get();
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