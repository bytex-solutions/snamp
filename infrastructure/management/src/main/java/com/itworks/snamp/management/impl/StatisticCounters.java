package com.itworks.snamp.management.impl;

import com.google.common.base.Stopwatch;
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
    private TimeSpan frequency;
    private final AtomicLong numberOfFaults;
    private final AtomicLong numberOfWarnings;
    private final AtomicLong numberOfDebugMessages;
    private final AtomicLong numberOfInformationMessages;
    private final Stopwatch timer;

    /**
     * Initializes a new countdown timer.
     *
     * @param frequency The initial timer value.
     * @throws IllegalArgumentException initial is null.
     */
    public StatisticCounters(final TimeSpan frequency) {
        this.frequency = frequency;
        numberOfFaults = new AtomicLong(0L);
        numberOfDebugMessages = new AtomicLong(0L);
        numberOfInformationMessages = new AtomicLong(0L);
        numberOfWarnings = new AtomicLong(0L);
        timer = Stopwatch.createUnstarted();
    }

    /**
     * Set renewal time.
     *
     * @param value the value
     */
    public synchronized void setRenewalTime(final TimeSpan value){
        timer.stop();
        this.frequency = value;
        resetCounters();
        timer.start();
    }

    /**
     * Get renewal time.
     *
     * @return the time span
     */
    public TimeSpan getRenewalTime(){
        return frequency;
    }

    /**
     * Get value.
     *
     * @param level the level
     * @return the long
     */
    public long getValue(final int level){
        if(resetTimerIfNecessary())
            resetCounters();
        switch (level){
            case LogService.LOG_ERROR: return numberOfFaults.get();
            case LogService.LOG_WARNING: return numberOfWarnings.get();
            case LogService.LOG_DEBUG: return numberOfDebugMessages.get();
            default: return numberOfInformationMessages.get();
        }
    }

    private void resetCounters(){
        numberOfInformationMessages.set(0L);
        numberOfFaults.set(0L);
        numberOfWarnings.set(0L);
        numberOfDebugMessages.set(0L);
    }

    private boolean isEmpty(){
        final long duration = timer.elapsed(frequency.unit);
        return duration >= frequency.duration;
    }

    private synchronized boolean resetTimerIfNecessary(){
        timer.stop();
        if(isEmpty()) {
            timer.reset().start();
            return true;
        }
        else{
            timer.start();
            return false;
        }
    }

    /**
     * Increment void.
     *
     * @param eventType the event type
     */
    public void increment(final int eventType){
        if(resetTimerIfNecessary())
            resetCounters();
        final AtomicLong counter;
        switch (eventType){
            case LogService.LOG_ERROR: counter = numberOfFaults; break;
            case LogService.LOG_DEBUG: counter = numberOfDebugMessages; break;
            case LogService.LOG_WARNING: counter = numberOfWarnings; break;
            default: counter = numberOfInformationMessages; break;
        }
        counter.incrementAndGet();
    }

    /**
     * Start void.
     */
    void start() {
        timer.start();
    }
}
