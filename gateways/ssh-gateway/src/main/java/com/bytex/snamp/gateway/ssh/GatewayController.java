package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.jmx.ExpressionBasedDescriptorFilter;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;

import javax.management.Notification;
import java.io.Writer;
import java.util.Set;

/**
 * Represents mediation layer between SNAMP infrastructure and Secure Shell Interpreter.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface GatewayController {
    /**
     * Gets a collection of connected managed resources.
     * @return A collection of connected managed resources.
     */
    Set<String> getConnectedResources();

    /**
     * Gets health status of the resource.
     * @param resourceName Resource name.
     * @return Health status of the resource; or {@literal null}, if resource doesn't exist.
     */
    HealthStatus getResourceStatus(final String resourceName);

    /**
     * Gets health status of all resources in the group.
     * @param groupName Name of the group.
     * @return Health status of the group; or {@literal nulll}, if group doesn't exist.
     */
    ResourceGroupHealthStatus getGroupStatus(final String groupName);

    /**
     * Gets IDs of attributes exposed by the specified managed resources.
     * @param resourceName The name of the managed resource.
     * @return A collection of connected attributes.
     */
    Set<String> getAttributes(final String resourceName);

    <E extends Exception> boolean processAttribute(final String resourceName,
                                                   final String attributeID,
                                                   final Acceptor<? super SshAttributeMapping, E> handler) throws E;

    Notification poll(final ExpressionBasedDescriptorFilter filter);

    void print(final Notification notif, final Writer output);
}
