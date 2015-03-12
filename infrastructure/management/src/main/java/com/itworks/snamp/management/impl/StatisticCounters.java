package com.itworks.snamp.management.impl;

import com.google.common.base.Stopwatch;
import com.itworks.snamp.TimeSpan;
import org.osgi.service.log.LogService;

import java.util.concurrent.atomic.AtomicLong;

/**
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

    public synchronized void setRenewalTime(final TimeSpan value){
        timer.stop();
        this.frequency = value;
        resetCounters();
        timer.start();
    }

    public TimeSpan getRenewalTime(){
        return frequency;
    }

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

    void start() {
        timer.start();
    }
}
