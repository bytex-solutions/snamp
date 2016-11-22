package com.bytex.snamp.connector.zipkin

import com.bytex.snamp.SpecialUse
import com.bytex.snamp.instrumentation.Identifier
import com.bytex.snamp.io.Buffers
import zipkin.Span

import java.util.concurrent.TimeUnit

def parseZipkinSpan(Span zipkinSpan) {
    def result = define measurement of span
    result.name = zipkinSpan.name
    result.spanID = Identifier.ofLong zipkinSpan.id
    if (zipkinSpan.parentId != null)   //because parentId may be 0
        result.parentSpanID = Identifier.ofLong zipkinSpan.parentId
    //parse traceID as correlation
    def traceId128 = Buffers.allocByteBuffer(16, false);
    traceId128.putLong zipkinSpan.traceIdHigh
    traceId128.putLong zipkinSpan.traceId
    result.correlationID = Identifier.ofBytes(traceId128.array())

    result.timeStamp = zipkinSpan.timestamp ?: System.currentTimeMillis()
    if (zipkinSpan.duration)
        result.setDuration(zipkinSpan.duration, TimeUnit.MICROSECONDS) //using microseconds according with Zipkin's Span specification
}

@SpecialUse
def parse(headers, body){
    assert body instanceof Span
    parseZipkinSpan(body)
}