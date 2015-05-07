package com.itworks.snamp.management.impl;

import com.itworks.snamp.TimeSpan;
import org.osgi.service.log.LogService;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The type Statistic counters.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class StatisticCounters {
    private long frequency;
    private long timer;
    private final AtomicLong numberOfFaults;
    private final AtomicLong numberOfWarnings;
    private final AtomicLong numberOfDebugMessages;
    private final AtomicLong numberOfInformationMessages;

    /**
     * Initializes a new countdown timer.
     *
     * @param frequency The initial timer value.
     * @throws IllegalArgumentException initial is null.
     */
    public StatisticCounters(final TimeSpan frequency) {
        this.frequency = frequency.toMillis();
        numberOfFaults = new AtomicLong(0L);
        numberOfDebugMessages = new AtomicLong(0L);
        numberOfInformationMessages = new AtomicLong(0L);
        numberOfWarnings = new AtomicLong(0L);
        timer = System.currentTimeMillis();
    }

    /**
     * Set renewal time.
     *
     * @param value Renewal time, in millis.
     */
    public synchronized void setRenewalTime(final long value){
        timer = System.currentTimeMillis();
        frequency = value;
    }

    /**
     * Get renewal time.
     *
     * @return Renewal time, in millis.
     */
    public synchronized long getRenewalTime(){
        return frequency;
    }

    public long getValue(final int level){
        resetCountersIfNecessary();
        switch (level){
            case LogService.LOG_ERROR: return numberOfFaults.get();
            case LogService.LOG_WARNING: return numberOfWarnings.get();
            case LogService.LOG_DEBUG: return numberOfDebugMessages.get();
            default: return numberOfInformationMessages.get();
        }
    }

    private synchronized void resetCountersIfNecessary() {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - timer <= frequency) return;
        timer = currentTime;
        numberOfInformationMessages.set(0L);
        numberOfFaults.set(0L);
        numberOfWarnings.set(0L);
        numberOfDebugMessages.set(0L);
    }

    /**
     * Increment void.
     *
     * @param eventType the event type
     */
    public void increment(final int eventType){
        resetCountersIfNecessary();
        final AtomicLong counter;
        switch (eventType){
            case LogService.LOG_ERROR: counter = numberOfFaults; break;
            case LogService.LOG_DEBUG: counter = numberOfDebugMessages; break;
            case LogService.LOG_WARNING: counter = numberOfWarnings; break;
            default: counter = numberOfInformationMessages; break;
        }
        counter.incrementAndGet();
    }
}