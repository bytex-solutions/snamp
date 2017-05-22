package com.bytex.snamp.testing.supervision

@GrabConfig(initContextClassLoader = true)
@Grab(group = 'org.codehaus.groovy', module = 'groovy-json', version = '2.4.5')
import groovy.json.JsonSlurper
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus

public void statusChanged(ResourceGroupHealthStatus previousStatus, ResourceGroupHealthStatus newStatus) {
    System.out.println "Was ${previousStatus}"
    System.out.println "Became ${newStatus}"
    final slurper = new JsonSlurper()
    println slurper.parseText('56')
}