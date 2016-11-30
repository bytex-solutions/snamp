import groovy.util.slurpersupport.GPathResult

private void sendTextMeasurement(String value){
    def m = define measurement of string
    m.name = "customStrings"
    m.value = value
    m.timeStamp = System.currentTimeMillis()
}

private def sendJsonMeasurement(json){
    def m = define measurement of string
    m.name = "customStrings"
    m.value = json.content
    m.timeStamp = System.currentTimeMillis()
}

private def sendXmlMeasurement(xml){
    def m = define measurement of string
    m.name = "customStrings"
    m.value = xml.content.text()
    m.timeStamp = System.currentTimeMillis()
}

def parse(headers, body){
    if(body instanceof String)
        sendTextMeasurement((String) body)
    else if(body instanceof GPathResult)
        sendXmlMeasurement(body)
    else
        sendJsonMeasurement(body)
}