package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents registry of all metrics.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class MetricRegistry implements Iterable<Reporter>, Closeable {
    //@FunctionalInterface
    private interface MeasurementReporterFactory<R extends MeasurementReporter<?>>{
        R create(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData);
    }
    private static final MeasurementReporterFactory<IntegerMeasurementReporter> INT_REPORTER_FACTORY = new MeasurementReporterFactory<IntegerMeasurementReporter>() {
        @Override
        public IntegerMeasurementReporter create(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
            return new IntegerMeasurementReporter(reporters, name, userData);
        }
    };
    private static final MeasurementReporterFactory<FloatingPointMeasurementReporter> FP_REPORTER_FACTORY = new MeasurementReporterFactory<FloatingPointMeasurementReporter>() {
        @Override
        public FloatingPointMeasurementReporter create(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
            return new FloatingPointMeasurementReporter(reporters, name, userData);
        }
    };
    private static final MeasurementReporterFactory<BooleanMeasurementReporter> BOOL_REPORTER_FACTORY = new MeasurementReporterFactory<BooleanMeasurementReporter>() {
        @Override
        public BooleanMeasurementReporter create(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
            return new BooleanMeasurementReporter(reporters, name, userData);
        }
    };
    private static final MeasurementReporterFactory<StringMeasurementReporter> STR_REPORTER_FACTORY = new MeasurementReporterFactory<StringMeasurementReporter>() {
        @Override
        public StringMeasurementReporter create(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
            return new StringMeasurementReporter(reporters, name, userData);
        }
    };
    private static final MeasurementReporterFactory<TimeMeasurementReporter> TIME_REPORTER_FACTORY = new MeasurementReporterFactory<TimeMeasurementReporter>() {
        @Override
        public TimeMeasurementReporter create(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
            return new TimeMeasurementReporter(reporters, name, userData);
        }
    };
    private static final MeasurementReporterFactory<SpanReporter> SPAN_REPORTER_FACTORY = new MeasurementReporterFactory<SpanReporter>() {
        @Override
        public SpanReporter create(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
            return new SpanReporter(reporters, name, userData);
        }
    };

    private final Iterable<Reporter> reporters;
    private final ConcurrentMap<String, IntegerMeasurementReporter> integerReporters = new ConcurrentHashMap<String, IntegerMeasurementReporter>();
    private final ConcurrentMap<String, FloatingPointMeasurementReporter> fpReporters = new ConcurrentHashMap<String, FloatingPointMeasurementReporter>();
    private final ConcurrentMap<String, BooleanMeasurementReporter> boolReporters = new ConcurrentHashMap<String, BooleanMeasurementReporter>();
    private final ConcurrentMap<String, StringMeasurementReporter> stringReporters = new ConcurrentHashMap<String, StringMeasurementReporter>();
    private final ConcurrentMap<String, TimeMeasurementReporter> timeReporters = new ConcurrentHashMap<String, TimeMeasurementReporter>();
    private final ConcurrentMap<String, SpanReporter> spanReporters = new ConcurrentHashMap<String, SpanReporter>();

    /**
     * Initializes a new registry with reporters loaded from specified class loader.
     * @param reporterLoader Class loader used to load reporters. Cannot be {@literal null}.
     */
    public MetricRegistry(final ClassLoader reporterLoader){
        reporters = ServiceLoader.load(Reporter.class, reporterLoader);
    }

    /**
     * Initializes a new registry with reporters loaded from {@link Thread#getContextClassLoader()}.
     */
    public MetricRegistry(){
        reporters = ServiceLoader.load(Reporter.class);
    }

    /**
     * Initializes a new registry with the specified set of reporters.
     * @param reporters Set of reporters.
     */
    public MetricRegistry(final Reporter... reporters){
        this.reporters = Arrays.asList(reporters.clone());
    }

    private static <T> T coalesce(final T first, final T second){
        return first == null ? second : first;
    }

    private <R extends MeasurementReporter<?>> R getOrAddReporter(final ConcurrentMap<String, R> repository,
                                                                         final String name,
                                                                         final Map<String, String> userData,
                                                                         final MeasurementReporterFactory<R> factory) {
        R reporter = repository.get(name);
        if (reporter == null) {
            reporter = factory.create(reporters, name, userData);
            return coalesce(repository.putIfAbsent(name, reporter), reporter);
        }
        return reporter;
    }

    /**
     * Gets measurement reporter for {@code long} values.
     * @param name Name of the metric.
     * @param userData Additional data that will be associated with each measurement report.
     * @return Measurement reporter.
     */
    public final IntegerMeasurementReporter integer(final String name, final Map<String, String> userData) {
        return getOrAddReporter(integerReporters, name, userData, INT_REPORTER_FACTORY);
    }

    /**
     * Gets measurement reporter for {@code long} values.
     * @param name Name of the metric.
     * @return Measurement reporter.
     */
    public final IntegerMeasurementReporter integer(final String name) {
        return integer(name, Collections.<String, String>emptyMap());
    }

    /**
     * Gets measurement reporter for {@code double} values.
     * @param name Name of the metric.
     * @param userData Additional data to be associated with each measurement report.
     * @return Measurement reporter.
     */
    public final FloatingPointMeasurementReporter floatingPoint(final String name, final Map<String, String> userData) {
        return getOrAddReporter(fpReporters, name, userData, FP_REPORTER_FACTORY);
    }

    /**
     * Gets measurement reporter for {@code double} values.
     * @param name Name of the metric.
     * @return Measurement reporter.
     */
    public final FloatingPointMeasurementReporter floatingPoint(final String name) {
        return floatingPoint(name, Collections.<String, String>emptyMap());
    }

    /**
     * Gets measurement reporter for {@code boolean} values.
     * @param name Name of the metric.
     * @param userData Additional data to be associated with each measurement report.
     * @return Measurement reporter.
     */
    public final BooleanMeasurementReporter bool(final String name, final Map<String, String> userData){
        return getOrAddReporter(boolReporters, name, userData, BOOL_REPORTER_FACTORY);
    }

    /**
     * Gets measurement reporter for {@code boolean} values.
     * @param name Name of the metric.
     * @return Measurement reporter.
     */
    public final BooleanMeasurementReporter bool(final String name){
        return bool(name, Collections.<String, String>emptyMap());
    }

    /**
     * Gets measurement reporter for {@link String} values.
     * @param name Name of the metric.
     * @param userData Additional data to be associated with each measurement report.
     * @return Measurement reporter.
     */
    public final StringMeasurementReporter string(final String name, final Map<String, String> userData){
        return getOrAddReporter(stringReporters, name, userData, STR_REPORTER_FACTORY);
    }

    /**
     * Gets measurement reporter for {@link String} values.
     * @param name Name of the metric.
     * @return Measurement reporter.
     */
    public final StringMeasurementReporter string(final String name){
        return string(name, Collections.<String, String>emptyMap());
    }

    /**
     * Gets time measurer.
     * @param name Name of the metric.
     * @param userData Additional data to be associated with each time report.
     * @return Time reporter.
     */
    public final TimeMeasurementReporter timer(final String name, final Map<String, String> userData){
        return getOrAddReporter(timeReporters, name, userData, TIME_REPORTER_FACTORY);
    }

    /**
     * Gets time measurer.
     * @param name Name of the metric.
     * @return Time reporter.
     */
    public final TimeMeasurementReporter timer(final String name){
        return timer(name, Collections.<String, String>emptyMap());
    }

    /**
     * Gets tracer with the specified name.
     * @param name Name of all traces detected by the tracer.
     * @param userData Additional data to be associated with each trace.
     * @return A new tracer.
     */
    public final SpanReporter tracer(final String name, final Map<String, String> userData){
        return getOrAddReporter(spanReporters, name, userData, SPAN_REPORTER_FACTORY);
    }

    /**
     * Gets tracer with the specified name.
     * @param name Name of all traces detected by the tracer.
     * @return A new tracer.
     */
    public final SpanReporter tracer(final String name){
        return tracer(name, Collections.<String, String>emptyMap());
    }

    /**
     * Clears this registry.
     */
    public void clear(){
        integerReporters.clear();
        fpReporters.clear();
        boolReporters.clear();
        stringReporters.clear();
        timeReporters.clear();
    }

    /**
     * Returns iterator through all reporters used by this registry.
     * @return Iterator through all reporters used by this registry.
     */
    @Override
    public final Iterator<Reporter> iterator() {
        return reporters.iterator();
    }

    /**
     * Clears this registry and close all reporters associated with it.
     * @throws IOException One of the reporters cannot be closed.
     */
    @Override
    public void close() throws IOException {
        clear();
        for(final Reporter reporter: reporters)
            reporter.close();
    }
}
