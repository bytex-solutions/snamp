package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.Consumer;

import javax.management.Notification;
import java.util.Set;

/**
 * Represents mediation layer between SNAMP infrastructure and Secure Shell Interpreter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AdapterController {
    /**
     * Gets a collection of connected managed resources.
     * @return A collection of connected managed resources.
     */
    Set<String> getConnectedResources();

    /**
     * Gets IDs of attributes exposed by the specified managed resources.
     * @param resourceName The name of the managed resource.
     * @return A collection of connected attributes.
     */
    Set<String> getAttributes(final String resourceName);

    <E extends Exception> boolean processAttribute(final String resourceName,
                                                   final String attributeID,
                                                   final Consumer<? super SshAttributeMapping, E> handler) throws E;

    /**
     * Gets a collection of available notifications.
     * @param resourceName The name of the managed resource.
     * @return A collection of available notifications.
     */
    Set<String> getNotifications(final String resourceName);

    <E extends Exception> boolean processNotification(final String resourceName,
                                                      final String notificationID,
                                                      final Consumer<? super SshNotificationMapping, E> handler) throws E;

    Notification poll(final String resourceName);

    Notification poll();
}
