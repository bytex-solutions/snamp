/**
 * Provides supervision of the health and workload states.
 * <ul>
 *     <li>{@link com.bytex.snamp.connector.supervision.HealthCheckSupport} can be implemented by {@link com.bytex.snamp.connector.ManagedResourceConnector} to provide
 *     health information about individual managed resource in real-time.</li>
 *     <li>{@link com.bytex.snamp.connector.supervision.HealthSupervisor} used to supervise status of the group of managed resources.</li>
 * </ul>
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see com.bytex.snamp.connector.supervision.HealthCheckSupport
 */
package com.bytex.snamp.connector.supervision;