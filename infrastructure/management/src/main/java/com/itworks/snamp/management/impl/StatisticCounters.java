package com.itworks.snamp.management.impl;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.internal.CountdownTimer;
import org.osgi.service.log.LogService;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class StatisticCounters extends CountdownTimer {
    private TimeSpan frequency;
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
        super(frequency);
        this.frequency = frequency;
        numberOfFaults = new AtomicLong(0L);
        numberOfDebugMessages = new AtomicLong(0L);
        numberOfInformationMessages = new AtomicLong(0L);
        numberOfWarnings = new AtomicLong(0L);
    }

    public synchronized void setRenewalTime(final TimeSpan value){
        stop();
        setTimerValue(frequency = value);
        resetCounters();
        start();
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

    private synchronized boolean resetTimerIfNecessary(){
        stop();
        if(isEmpty()) {
            setTimerValue(frequency);
            return start();
        }
        else{
            start();
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
}
