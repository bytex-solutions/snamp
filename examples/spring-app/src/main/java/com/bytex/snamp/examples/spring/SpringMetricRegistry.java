package com.bytex.snamp.examples.spring;

import com.bytex.snamp.instrumentation.MetricRegistry;
import com.bytex.snamp.instrumentation.SpanReporter;
import com.bytex.snamp.instrumentation.reporters.http.HttpReporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Component
public final class SpringMetricRegistry extends MetricRegistry {
    public SpringMetricRegistry(@Value("${com.bytex.snamp.http.acceptor}") final String snampEndpoint) throws URISyntaxException {
        super(new HttpReporter(snampEndpoint));
    }

    SpanReporter orderTripTracer(){
        return tracer("orderTrip");
    }

    SpanReporter requestDriverTracer(){
        return tracer("requestDriver");
    }

    SpanReporter notifyPassengerTracer(){
        return tracer("notifyPassenger");
    }
}
