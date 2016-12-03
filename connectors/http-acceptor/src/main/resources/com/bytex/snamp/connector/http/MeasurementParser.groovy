package com.bytex.snamp.connector.http

import com.bytex.snamp.instrumentation.measurements.Measurement

def parse(headers, body){
    if(body instanceof Measurement){
        addMeasurement body
    } else
        return delegateParsing(headers, body)
}