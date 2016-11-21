package com.bytex.snamp.connector.http

import com.bytex.snamp.instrumentation.Measurement

/*
    Trivial parsing because body is always of type Measurement
 */
def parse(headers, body){
    assert body instanceof Measurement
    addMeasurement body
}