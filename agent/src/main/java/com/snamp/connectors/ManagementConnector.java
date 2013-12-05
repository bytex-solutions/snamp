package com.snamp.connectors;

import com.snamp.*;
import net.xeoh.plugins.base.Plugin;

import java.util.*;
import java.util.concurrent.*;

/**
 * Represents management connector that exposes management attributes of the remote provider.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Lifecycle(InstanceLifecycle.NORMAL)
public interface ManagementConnector extends Iterable<String>, AutoCloseable {

    /**
     * Connects to the specified attribute.
     * @param id A key string that is used to read attribute from this connector.
     * @param attributeName The name of the attribute.
     * @param options The attribute discovery options.
     * @return The description of the attribute.
     */
    public AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options);

    /**
     * Returns the attribute value.
     * @param id  A key string that is used to read attribute from this connector.
     * @param readTimeout The attribute value read operation timeout.
     * @param defaultValue The default value of the attribute if it is real value is not available.
     * @return The value of the attribute, or default value.
     * @throws TimeoutException The attribute value cannot be read in the specified duration.
     */
    public Object getAttribute(final String id, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException;

    /**
     * Reads a set of attributes.
     * @param output The dictionary with set of attribute keys to read and associated default values.
     * @param readTimeout The attribute value read operation timeout.
     * @return The set of attributes ids really written to the dictionary.
     * @throws TimeoutException The attribute value cannot be read in the specified duration.
     */
    public Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException;

    /**
     * Writes the value of the specified attribute.
     * @param id An identifier of the attribute,
     * @param writeTimeout The attribute value write operation timeout.
     * @param value The value to write.
     * @return {@literal true} if attribute set operation is supported by remote provider; otherwise, {@literal false}.
     * @throws TimeoutException The attribute value cannot be write in the specified duration.
     */
    public boolean setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException;

    /**
     * Writes a set of attributes inside of the transaction.
     * @param values The dictionary of attributes keys and its values.
     * @param writeTimeout
     * @return {@literal null}, if the transaction is committed; otherwise, {@literal false}.
     * @throws TimeoutException
     */
    public boolean setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException;

    /**
     * Removes the attribute from the connector.
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    public boolean disconnectAttribute(final String id);

    /**
     * Returns the information about the connected attribute.
     * @param id An identifier of the attribute.
     * @return The attribute descriptor; or {@literal null} if attribute is not connected.
     */
    public AttributeMetadata getAttributeInfo(final String id);

    /**
     * Executes remote action.
     * @param actionName The name of the action,
     * @param args The invocation arguments.
     * @param timeout The Invocation timeout.
     * @return The invocation result.
     */
    public Object doAction(final String actionName, final Arguments args, final TimeSpan timeout) throws UnsupportedOperationException, TimeoutException;

    /**
     * Enables event listening for the specified category of events.
     * <p>
     *     categoryId can be used for enabling notifications for the same category
     *     but with different options.
     * </p>
     * @param listId An identifier of the subscription list.
     * @param category The name of the category to listen.
     * @param options Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     */
    public NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options);

    /**
     * Disables event listening for the specified category of events.
     * <p>
     *     This method removes all listeners associated with the specified subscription list.
     * </p>
     * @param listId The identifier of the subscription list.
     * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
     */
    public boolean disableNotifications(final String listId);

    /**
     * Gets the notification metadata by its category.
     * @param listId The identifier of the subscription list.
     * @return The metadata of the specified notification category; or {@literal null}, if notifications
     * for the specified category is not enabled by {@link #enableNotifications(String, String, java.util.Map)} method.
     */
    public NotificationMetadata getNotificationInfo(final String listId);

    /**
     * Attaches the notification listener.
     * @param listId The identifier of the subscription list.
     * @param listener The notification listener.
     * @return An identifier of the notification listener generated by this connector.
     * @throws UnsupportedOperationException Notifications are not supported.
     */
    public Object subscribe(final String listId, final NotificationListener listener) throws UnsupportedOperationException;

    /**
     * Removes the notification listener.
     * @param listenerId An identifier previously returned by {@link #subscribe(String, NotificationListener)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    public boolean unsubscribe(final Object listenerId) throws UnsupportedOperationException;
}
