package com.bytex.snamp.examples.spring;

import com.bytex.snamp.instrumentation.SpanReporter;
import com.bytex.snamp.instrumentation.TraceScope;
import com.bytex.snamp.instrumentation.measurements.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@Controller
@RequestMapping("/passenger-manager")
public final class PassengerManagementController {
    private final SpanReporter spanReporter;

    public PassengerManagementController(@Autowired final SpringMetricRegistry registry){
        spanReporter = registry.notifyPassengerTracer();
    }

    @RequestMapping(method= RequestMethod.POST, consumes = "application/json")
    public @ResponseBody void driverIsAssigned(@RequestBody final DriverInfo assignedDriver,
                                               @RequestHeader(Span.CORRELATION_HTTP_HEADER) final String correlationID,
                                               @RequestHeader(Span.SPAN_HTTP_HEADER) final String requestID) throws InterruptedException {
        System.out.format("Car number is %s with correlation %s and request ID %s", assignedDriver.getCarNumber(), correlationID, requestID).println();
        try(final TraceScope scope = spanReporter.beginTrace(correlationID, requestID)) {
            Thread.sleep(100);//simulate hard work
        }
    }
}
