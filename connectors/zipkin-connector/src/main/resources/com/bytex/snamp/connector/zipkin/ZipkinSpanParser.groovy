package com.bytex.snamp.connector.zipkin

import com.bytex.snamp.ArrayUtils
import com.bytex.snamp.instrumentation.Identifier
import com.bytex.snamp.instrumentation.measurements.CorrelationPolicy
import com.google.common.net.InetAddresses
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TMemoryInputTransport
import zipkin.Annotation
import zipkin.BinaryAnnotation
import zipkin.Constants
import zipkin.Endpoint
import zipkin.Span

import java.util.concurrent.TimeUnit

private static void parseBinaryAnnotation(String key, TBinaryProtocol value, Map<String, String> userData){
    switch (key) {
        case BinaryAnnotation.Type.BOOL:
            userData[key] = Boolean.toString value.readBool()
            break
        case BinaryAnnotation.Type.STRING:
            userData[key] = value.readString()
            break
        case BinaryAnnotation.Type.DOUBLE:
            userData[key] = Double.toString value.readDouble()
            break
        case BinaryAnnotation.Type.I16:
            userData[key] = Short.toString value.readI16()
            break
        case BinaryAnnotation.Type.I32:
            userData[key] = Integer.toString value.readI32()
            break
        case BinaryAnnotation.Type.I64:
            userData[key] = Long.toString value.readI64()
            break
    }
}

private static Optional<InetAddress> getInetAddress(Endpoint zipkinEndpoint) {
    if (zipkinEndpoint.ipv4 != 0)
        Optional.of InetAddresses.fromInteger(zipkinEndpoint.ipv4)
    else if (zipkinEndpoint.ipv6 != null && zipkinEndpoint.ipv6.length > 0)
        Optional.of InetAddress.getByAddress(zipkinEndpoint.ipv6)
    else
        Optional.empty()
}

private void parseZipkinSpan(Span zipkinSpan) {
    final result = new com.bytex.snamp.instrumentation.measurements.Span()
    result.name = zipkinSpan.name
    result.spanID = Identifier.ofLong zipkinSpan.id
    if (zipkinSpan.parentId != null)   //because parentId may be 0
        result.parentSpanID = Identifier.ofLong zipkinSpan.parentId
    //parse traceID as correlation
    result.correlationID = Identifier.ofBytes ArrayUtils.toByteArray([zipkinSpan.traceIdHigh, zipkinSpan.traceId] as long[])
    result.correlationPolicy = CorrelationPolicy.GLOBAL

    result.timeStamp = zipkinSpan.timestamp ?: System.currentTimeMillis()
    if (zipkinSpan.duration)
        result.setDuration zipkinSpan.duration, TimeUnit.MICROSECONDS //using microseconds according with Zipkin's Span specification
    else
        result.setDuration 0, TimeUnit.MILLISECONDS

    //parse special annotations
    for (final Annotation annotation : zipkinSpan.annotations)
        switch (annotation.value) {
            case Constants.SERVER_RECV:
            case Constants.SERVER_SEND:
                //leave parser if instance name doesn't match to the reporter's service name
                //otherwise, SNAMP user should rely on LDAP filter configured in connector parameters
                if (useServiceNameAsInstanceName) {
                    if (annotation.endpoint.serviceName != componentInstance)
                        return
                }
                //resolve instance name using IP address
                getInetAddress(annotation.endpoint).ifPresent({ result.annotations["address"] = it.hostAddress })
                if (annotation.endpoint.port)
                    result.annotations["port"] = annotation.endpoint.port.toString()
        }
    //parse binary annotations
    for(final BinaryAnnotation annotation: zipkinSpan.binaryAnnotations)
        new TMemoryInputTransport(annotation.value).withCloseable {
            final protocol = new TBinaryProtocol(it)
            switch (it.key) {
                case Constants.ERROR:
                    def notif = define notification
                    notif.message = protocol.readString()
                    notif.type = "zipkin.error"
                    return
                default:
                    parseBinaryAnnotation(annotation.key, protocol, result.annotations)
                    return
            }
        }

    addMeasurement result
}

def parse(headers, body){
    assert body instanceof Span
    parseZipkinSpan(body)
}