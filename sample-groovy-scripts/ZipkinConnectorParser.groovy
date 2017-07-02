import zipkin.Span

def parse(headers, body){
    assert body instanceof Span
    def m = define measurement of integer
    m.name = "ts"
    m.timeStamp = body.timestamp ?: System.currentTimeMillis()
    m.value = body.traceId
}