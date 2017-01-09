package scripts

def parse(headers, body){
    addMeasurement headers['m1']
    addMeasurement headers['m2']
}
