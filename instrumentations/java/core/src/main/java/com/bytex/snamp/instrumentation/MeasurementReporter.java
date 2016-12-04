package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.Measurement;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents abstract reporter for all measurements.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class MeasurementReporter<M extends Measurement> {
    /**
     * Represents reporter task used for periodic reporting based on timer.
     */
    public interface ReportingTask<R extends MeasurementReporter<?>> {
        /**
         * Reports about new measurement.
         * @param reporter Measurement reporter.
         * @return {@literal true} to continue reporting timer-specific delay; otherwise, {@literal false}.
         */
        boolean report(final R reporter);
    }

    private static final AtomicLong timerCounter = new AtomicLong(0L);
    private static final Logger LOGGER = Logger.getLogger("SnampMeasurementsReporter");
    private final Iterable<Reporter> reporters;
    private final String name;
    private final Map<String, String> userData;
    private String componentName;
    private String instanceName;
    private volatile ScheduledExecutorService scheduler;  //scheduler has lazy instantiation to reduce memory pressure and number of application threads.

    MeasurementReporter(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData){
        this.reporters = reporters;
        this.name = name;
        this.userData = userData;
        componentName = instanceName = "";
    }

    final void setApplicationInfo(final String name, final String instance){
        componentName = name;
        instanceName = instance;
    }

    private static ThreadFactory createThreadFactory(final String reporterName) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r, reporterName + "-reporter-" + timerCounter.getAndIncrement());
                t.setDaemon(true);
                t.setPriority(3);   //at the middle between MIN and NORM priority to save CPU time when report measurements
                return t;
            }
        };
    }

    private static MeasurementScope taskScope(final Future<?> future){
        return new MeasurementScope() {
            @Override
            public void close() {
                future.cancel(true);
            }
        };
    }

    static <R extends MeasurementReporter<?>> Runnable createTask(final R reporter, final ReportingTask<? super R> task){
        return new Runnable() {
            @Override
            public void run() {
                if(!task.report(reporter))
                    Thread.currentThread().interrupt();
            }
        };
    }

    private synchronized ScheduledExecutorService getScheduler(){
        if(scheduler == null) {
            final ScheduledThreadPoolExecutor executor;
            final int corePoolSize = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
            scheduler = executor = new ScheduledThreadPoolExecutor(corePoolSize, createThreadFactory(name));
            executor.setKeepAliveTime(1, TimeUnit.MINUTES);
            executor.allowCoreThreadTimeOut(true);
        }
        return scheduler;
    }

    final MeasurementScope scheduleReporting(final Runnable task, final long delay, final TimeUnit unit) {
        ScheduledExecutorService scheduler = this.scheduler;
        if (scheduler == null)
            scheduler = getScheduler();
        return taskScope(scheduler.scheduleWithFixedDelay(task, delay, delay, unit));
    }

    /**
     * Reports a new measurement.
     * @param measurement Measurement to report.
     */
    protected final void report(final M measurement) {
        measurement.setName(name);
        measurement.addUserData(userData);
        measurement.setComponentName(componentName);
        measurement.setInstanceName(instanceName);
        for (final Reporter reporter : reporters)
            if (reporter.isConnected())
                try {
                    reporter.report(measurement);
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, String.format("Failed to report measurement %s via %s", measurement, reporter));
                }
            else
                LOGGER.log(Level.WARNING, String.format("Reporter %s is not connected", reporter));
    }
}
