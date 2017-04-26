package com.bytex.snamp.testing.supervision

import com.bytex.snamp.connector.health.HealthStatus

public HealthStatus statusChanged(HealthStatus previousStatus, HealthStatus newStatus) {
    System.out.println newStatus
    return newStatus
}