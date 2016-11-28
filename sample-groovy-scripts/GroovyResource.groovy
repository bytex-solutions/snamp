@GrabConfig(initContextClassLoader = true)
@Grab(group = 'org.codehaus.groovy', module = 'groovy-json', version = '2.4.5')
import groovy.json.JsonSlurper

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
@Grab(group='org.codehaus.groovy', module='groovy-xml', version='2.4.5')
@GrabConfig(initContextClassLoader = true)
import groovyx.net.http.RESTClient

def longValue = 10L

if(!discovery) {
    def config = getResourceConfiguration resourceName

    println config.connectionString
}

attribute {
    name "DummyAttribute"
    type INT32
    get {longValue}
    set {value -> longValue = value}
}

attribute {
    name "JsonAttribute"
    type INT32
    get {
        def slurper = new JsonSlurper()
        slurper.parseText('56')
    }
}

attribute {
    name "Yahoo"
    type INT32
    get {
        def yahooClient = new RESTClient('http://finance.yahoo.com/')
        def response = yahooClient.get(path: 'webservice/v1/symbols/allcurrencies/quote', query: [format: "json"])
        response.data.list.meta.count
    }
}

attribute {
    name "Dictionary"
    type DICTIONARY('GroovyType', 'GroovyDescr', [key1: [type: INT64, description: 'descr'], key2: [type: BOOL, description: 'descr']])
    get { asDictionary(type(), [key1: 67L, key2: true])}
    set {value -> System.out.println(value)}
}

attribute {
    name "Table"
    type TABLE('GroovyTable', 'desc', [column1: [type: INT32, description: 'descr', indexed: true], column2: [type: BOOL, description: 'descr']])
    get { asTable(type(), [[column1: 6, column2: false], [column1: 7, column2: true]]) }
    set {value -> System.out.println(value)}
}

event {
    name "GroovyEvent"
}

def action = { emit("GroovyEvent", 'Dummy event') }

job = schedule action, 300

void close(){
    super.close()
    job.close()
}