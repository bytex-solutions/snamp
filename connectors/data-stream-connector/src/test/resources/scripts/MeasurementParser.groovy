package scripts

def parse(headers, body){
    def measurement = define measurement of bool
    measurement.name = body.toString()
    measurement.annotations['contentType'] = headers['Content-Type']
    measurement.value = true
}