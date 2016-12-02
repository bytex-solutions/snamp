package com.bytex.snamp.instrumentation.reporting;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents registry of all metrics.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class MetricRegistry implements Iterable<Reporter> {
    private final Iterable<Reporter> reporters;
    private final ConcurrentMap<String, IntegerMeasurementReporter> integerReporters = new ConcurrentHashMap<String, IntegerMeasurementReporter>();

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

    public MetricRegistry(final Reporter... reporters){
        this.reporters = Arrays.asList(reporters);
    }

    public final IntegerMeasurementReporter intGauge(final String name) {
        IntegerMeasurementReporter reporter = integerReporters.get(name);
        if (reporter == null) {
            final IntegerMeasurementReporter existing = integerReporters.putIfAbsent(name, reporter = new IntegerMeasurementReporter(reporters, name));
            if (existing != null)
                reporter = existing;
        }
        return reporter;
    }

    /**
     * Clears this registry.
     */
    public void clear(){
        integerReporters.clear();
    }

    /**
     * Returns iterator through all reporters used by this registry.
     * @return Iterator through all reporters used by this registry.
     */
    @Override
    public final Iterator<Reporter> iterator() {
        return reporters.iterator();
    }
}
