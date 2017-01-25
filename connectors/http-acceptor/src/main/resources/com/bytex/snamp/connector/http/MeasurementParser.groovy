package com.bytex.snamp.connector.http

import com.bytex.snamp.instrumentation.measurements.Measurement

private void parseMeasurement(Measurement measurement) {
    if (componentInstance == measurement.getInstanceName() && componentName == measurement.getComponentName())
        addMeasurement body
}

def parse(headers, body){
    if(body instanceof Measurement)
        parseMeasurement(body)
    else
        return delegateParsing(headers, body)
}