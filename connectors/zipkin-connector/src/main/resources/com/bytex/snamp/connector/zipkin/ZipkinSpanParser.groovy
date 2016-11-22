package com.bytex.snamp.connector.zipkin

import com.bytex.snamp.SpecialUse
import com.bytex.snamp.instrumentation.Identifier
import com.bytex.snamp.io.Buffers
import zipkin.Span

import java.util.concurrent.TimeUnit

def parseZipkinSpan(Span span) {
    def result = define measurement of span
    result.name = span.name
    result.spanID = Identifier.ofLong span.id
    if (span.parentId != null)   //because parentId may be 0
        result.parentSpanID = Identifier.ofLong span.parentId
    //parse traceID as correlation
    def traceId128 = Buffers.allocByteBuffer(16, false);
    traceId128.putLong span.traceIdHigh
    traceId128.putLong span.traceId
    result.correlationID = Identifier.ofBytes(traceId128.array())

    result.timeStamp = span.timestamp ?: System.currentTimeMillis()
    if (span.duration)
        result.setDuration(span.duration, TimeUnit.MICROSECONDS)
    //using microseconds according with Zipkin's Span specification
}

@SpecialUse
def parse(headers, body){
    assert body instanceof Span
    parseZipkinSpan(body)
}