package com.bytex.snamp.connector.zipkin

import com.bytex.snamp.SpecialUse
import com.bytex.snamp.instrumentation.Identifier
import com.bytex.snamp.io.Buffers
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TMemoryInputTransport
import zipkin.BinaryAnnotation
import zipkin.Constants
import zipkin.Span

import java.util.concurrent.TimeUnit

private static void expandUserData(BinaryAnnotation annotation, Map<String, String> userData){
    def valueParser = new TBinaryProtocol(new TMemoryInputTransport(annotation.value))
    final String value
    switch (annotation.key){
        case BinaryAnnotation.Type.BOOL:
            value = Boolean.toString valueParser.readBool()
            break
        case BinaryAnnotation.Type.STRING:
            value = valueParser.readString()
            break
        case BinaryAnnotation.Type.DOUBLE:
            value = Double.toString valueParser.readDouble()
            break
        case BinaryAnnotation.Type.I16:
            value = Short.toString valueParser.readI16()
            break
        case BinaryAnnotation.Type.I32:
            value = Integer.toString valueParser.readI32()
            break
        case BinaryAnnotation.Type.I64:
            value = Long.toString valueParser.readI64()
            break
        case BinaryAnnotation.Type.BYTES:
        default:
            value = Base64.getEncoder().encodeToString annotation.value
            break
    }
    userData[annotation.key] = value
}

private void parseZipkinSpan(Span zipkinSpan) {
    def result = define measurement of span
    result.name = zipkinSpan.name
    result.spanID = Identifier.ofLong zipkinSpan.id
    if (zipkinSpan.parentId != null)   //because parentId may be 0
        result.parentSpanID = Identifier.ofLong zipkinSpan.parentId
    //parse traceID as correlation
    def traceId128 = Buffers.allocByteBuffer 16, false
    traceId128.putLong zipkinSpan.traceIdHigh
    traceId128.putLong zipkinSpan.traceId
    result.correlationID = Identifier.ofBytes traceId128.array()

    result.timeStamp = zipkinSpan.timestamp ?: System.currentTimeMillis()
    if (zipkinSpan.duration)
        result.setDuration zipkinSpan.duration, TimeUnit.MICROSECONDS //using microseconds according with Zipkin's Span specification
    //parse binary annotations
    zipkinSpan.binaryAnnotations.every {
        switch (it.key){
            case Constants.ERROR:
                def protocol = new TBinaryProtocol(new TMemoryInputTransport(it.value))
                def notif = define notification
                notif.source = this
                notif.message = protocol.readString()
                notif.type = "zipkin.error"
                break
            default:
                expandUserData(it, result.userData)
        }
    }
}

@SpecialUse
def parse(headers, body){
    assert body instanceof Span
    parseZipkinSpan(body)
}