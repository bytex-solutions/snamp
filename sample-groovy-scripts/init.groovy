@Grab(group = 'org.codehaus.groovy', module = 'groovy-json', version = '2.4.5')
@GrabConfig(initContextClassLoader = true) @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
@GrabConfig(initContextClassLoader = true) @Grab(group='org.codehaus.groovy', module='groovy-xml', version='2.4.5')
@GrabConfig(initContextClassLoader = true)
import java.lang.Object

println initScript

if(!discovery) {
    def config = getResourceConfiguration resourceName

    println config.connectionString
    println config.type
    println config.parameters.initScript
}

//attributes
attribute "DummyAttribute", [config: "a"]
