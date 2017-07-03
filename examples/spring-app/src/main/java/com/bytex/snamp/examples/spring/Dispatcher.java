package com.bytex.snamp.examples.spring;

import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.SpanReporter;
import com.bytex.snamp.instrumentation.TraceScope;
import com.bytex.snamp.instrumentation.measurements.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * A source of all test requests for simple topology.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Component("dispatcher")
public final class Dispatcher {

    private boolean isDispatcher;
    private final RestTemplate restClient;
    private final SpanReporter spanReporter;

    public Dispatcher(@Value("${com.bytex.snamp.dispatcher:true}") final boolean dispatcher,
                      @Autowired final SpringMetricRegistry registry){
        isDispatcher = dispatcher;
        spanReporter = registry.notifyPassengerTracer();
        restClient = new RestTemplate();
    }

    @Scheduled(fixedRate = 5_000, initialDelay = 5_000)
    public void dispatcherRequester() {
        if (isDispatcher)
            try (final TraceScope scope = spanReporter.beginTrace(Identifier.randomID())) {
                //request driver for trip
                final HttpEntity<TripInfo> trip = new HttpEntity<>(new TripInfo("Saint-Petersburg", "Moscow"));
                trip.getHeaders().add(Span.CORRELATION_HTTP_HEADER, scope.getCorrelationID().toString());
                trip.getHeaders().add(Span.SPAN_HTTP_HEADER, scope.getSpanID().toString());
                final HttpEntity<DriverInfo> driver =
                        new HttpEntity<>(restClient.postForEntity("http://localhost:3737/driver-manager", trip, DriverInfo.class).getBody());
                //notify passenger
                driver.getHeaders().add(Span.CORRELATION_HTTP_HEADER, scope.getCorrelationID().toString());
                driver.getHeaders().add(Span.SPAN_HTTP_HEADER, scope.getSpanID().toString());
                restClient.postForEntity("http://localhost:3738/passenger-manager", driver, void.class);
            }
    }
}
