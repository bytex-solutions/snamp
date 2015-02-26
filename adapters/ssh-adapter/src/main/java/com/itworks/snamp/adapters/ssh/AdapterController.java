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

    /**
     * Processes the attribute.
     * @param attributeID ID of the attribute.
     * @param handler The attribute processor.
     * @return {@literal true}, if attribute exists; otherwise, {@literal false}.
     * @throws E Unable to process attribute.
     */
    <E extends Exception> boolean processAttribute(final String attributeID, final Consumer<SshAttributeView, E> handler) throws E;

    /**
     * Gets a collection of available notifications.
     * @param resourceName The name of the managed resource.
     * @return A collection of available notifications.
     */
    Set<String> getNotifications(final String resourceName);

    <E extends Exception> boolean processNotification(final String notificationID,
                                                      final Consumer<SshNotificationView, E> handler) throws E;

    Notification poll(final String resourceName);

    Notification poll();
}
