package com.bytex.snamp.testing.supervision

@GrabConfig(initContextClassLoader = true)
@Grab(group = 'org.codehaus.groovy', module = 'groovy-json', version = '2.4.5')
import groovy.json.JsonSlurper

println "Was ${previousStatus}"
println "Became ${newStatus}"
final slurper = new JsonSlurper()
println slurper.parseText('56')