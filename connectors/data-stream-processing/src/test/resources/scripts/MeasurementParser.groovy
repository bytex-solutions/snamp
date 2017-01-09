package scripts

def parse(headers, body){
    def measurement = define measurement of bool
    measurement.name = body.toString()
    measurement.getAnnotations['contentType'] = headers['Content-Type']
    measurement.value = true
}