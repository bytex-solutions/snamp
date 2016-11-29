package com.bytex.snamp.connector.zipkin

import com.bytex.snamp.SpecialUse
import com.bytex.snamp.instrumentation.Identifier
import com.bytex.snamp.io.Buffers
import org.apache.thrift.TDeserializer
import org.apache.thrift.TFieldIdEnum
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TMemoryInputTransport
import zipkin.Annotation
import zipkin.BinaryAnnotation
import zipkin.Constants
import zipkin.Span
import zipkin.TraceKeys

import java.util.concurrent.TimeUnit


void parseZipkinSpan(Span zipkinSpan) {
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
    //parse binary annotations
    zipkinSpan.binaryAnnotations.every {
        def protocol = new TBinaryProtocol(new TMemoryInputTransport(it.value))
        final String value;
        switch (it.type){
            case BinaryAnnotation.Type.BOOL:
                value = Boolean.toString(protocol.readBool())
                break
            case BinaryAnnotation.Type.STRING:
                value = protocol.readString()
                break
            default:
                value = Base64.getEncoder().encodeToString(it.value)
                break
        }
        result.userData[it.key] = value
    }
}

@SpecialUse
def parse(headers, body){
    assert body instanceof Span
    parseZipkinSpan(body)
}