import com.bytex.snamp.connector.supervision.HealthStatus

public HealthStatus statusChanged(final HealthStatus previousStatus, final HealthStatus newStatus) {
    System.out.println newStatus
    return newStatus
}