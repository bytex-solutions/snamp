@Grab(group = 'org.codehaus.groovy', module = 'groovy-json', version = '2.4.3')
@GrabConfig(initContextClassLoader = true)
import groovy.json.JsonSlurper

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
@GrabConfig(initContextClassLoader = true)
import groovyx.net.http.RESTClient

@Grab(group='org.codehaus.groovy', module='groovy-xml', version='2.4.3')
@GrabConfig(initContextClassLoader = true)
import groovy.util.slurpersupport.GPathResult

println initScript

if(!discovery) {
    def config = getResourceConfiguration resourceName

    println config.connectionString
    println config.connectionType
    println config.parameters.initScript
}

//attributes
attribute "DummyAttribute", [config: "a"]