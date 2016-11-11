package scripts

def parse(headers, body){
    def measurement = create measurement of bool
    measurement.message = body.toString()
    measurement.userData['contentType'] = headers['Content-Type']
    measurement.value = true
    measurement
}