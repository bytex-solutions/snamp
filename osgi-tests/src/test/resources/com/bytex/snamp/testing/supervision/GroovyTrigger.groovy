package com.bytex.snamp.testing.supervision

import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus

public void statusChanged(ResourceGroupHealthStatus previousStatus, ResourceGroupHealthStatus newStatus) {
    System.out.println "Was ${previousStatus}"
    System.out.println "Became ${newStatus}"
}