package com.bytex.snamp.connector.zipkin

import com.bytex.snamp.SpecialUse
import com.bytex.snamp.instrumentation.Identifier
import com.bytex.snamp.io.Buffers
import zipkin.Span

def parseZipkinSpan(Span span){
    def result = define measurement of span
    result.name = span.name
    result.spanID = Identifier.ofLong span.id
    if(span.parentId != null)
        result.parentSpanID = Identifier.ofLong span.parentId
    if(span.traceIdHigh == 0)
        result.correlationID = Identifier.ofLong(span.traceId)
    else {
        def traceId128 = Buffers.allocByteBuffer(16, false);
        traceId128.putLong span.traceIdHigh
        traceId128.putLong span.traceId
        result.correlationID = Identifier.ofBytes(traceId128.array())
    }
}

@SpecialUse
def parse(headers, body){
    assert body instanceof Span
    parseZipkinSpan(body)
}