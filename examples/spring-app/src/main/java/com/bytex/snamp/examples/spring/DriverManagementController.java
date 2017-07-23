package com.bytex.snamp.examples.spring;

import com.bytex.snamp.instrumentation.SpanReporter;
import com.bytex.snamp.instrumentation.TraceScope;
import com.bytex.snamp.instrumentation.measurements.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/driver-manager")
public final class DriverManagementController {
    private final SpanReporter spanReporter;

    public DriverManagementController(@Autowired final SpringMetricRegistry registry){
        spanReporter = registry.requestDriverTracer();
    }

    @RequestMapping(method= RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody DriverInfo requestDriver(@RequestBody final TripInfo trip,
                                                  @RequestHeader(Span.CORRELATION_HTTP_HEADER) final String correlationID,
                                                  @RequestHeader(Span.SPAN_HTTP_HEADER) final String requestID) throws InterruptedException {
        System.out.format("Requested destination: %s with correlation %s and request ID %s", trip.getDestination(), correlationID, requestID).println();
        try(final TraceScope scope = spanReporter.beginTrace(correlationID, requestID)) {
            Thread.sleep(100);//simulate hard work
            return new DriverInfo("A956OM", "Will", "Smith");
        }
    }
}
