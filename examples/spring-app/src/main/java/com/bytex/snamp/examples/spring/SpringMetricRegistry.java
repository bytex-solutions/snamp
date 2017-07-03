package com.bytex.snamp.examples.spring;

import com.bytex.snamp.instrumentation.MetricRegistry;
import com.bytex.snamp.instrumentation.SpanReporter;
import com.bytex.snamp.instrumentation.reporters.PrintStreamReporter;
import org.springframework.stereotype.Component;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Component
public final class SpringMetricRegistry extends MetricRegistry {
    public SpringMetricRegistry(){
        super(PrintStreamReporter.toStandardOutput());
    }

    public SpanReporter orderTripTracer(){
        return tracer("orderTrip");
    }

    public SpanReporter requestDriverTracer(){
        return tracer("requestDriver");
    }

    public SpanReporter notifyPassengerTracer(){
        return tracer("notifyPassenger");
    }
}
