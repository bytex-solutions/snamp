package com.bytex.snamp.testing.connector.supervision

import com.bytex.snamp.connector.health.HealthStatus

public HealthStatus statusChanged(final HealthStatus previousStatus, final HealthStatus newStatus) {
    System.out.println newStatus
    return newStatus
}