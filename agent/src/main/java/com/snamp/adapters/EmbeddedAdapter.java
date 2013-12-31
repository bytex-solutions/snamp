package com.snamp.adapters;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import com.snamp.connectors.util.*;
import static com.snamp.connectors.NotificationSupport.NotificationListener;


import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;

import java.io.IOException;
import java.util.*;

/**
 * Represents embedded adapter that can be used to embed communication with
 * management connector into your application.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class EmbeddedAdapter extends AbstractAdapter implements NotificationPublisher {
    private final AbstractAttributesRegistry attributes;
    private final AbstractSubscriptionList notifications;

    /**
     * Initializes a new instance of the adapter.
     *
     * @param adapterName
     */
    public EmbeddedAdapter(final String adapterName) {
        super(adapterName);
        attributes = new AbstractAttributesRegistry() {
            @Override
            protected ConnectedAttributes createBinding(final AttributeSupport connector) {
                return new ConnectedAttributes(connector) {
                    @Override
                    public String makeAttributeId(final String prefix, final String postfix) {
                        return makeEntityId(prefix, postfix);
                    }
                };
            }
        };
        notifications = new AbstractSubscriptionList() {
            @Override
            protected EnabledNotification createBinding(final NotificationSupport connector) {
                return new EnabledNotification(connector) {
                    @Override
                    public String makeListId(final String prefix, final String postfix) {
                        return makeEntityId(prefix, postfix);
                    }
                };
            }
        };
    }

    private static String makeEntityId(final String prefix, final String postfix){
        return String.format("%s/%s", prefix, postfix);
    }

    /**
     * Exposes the connector to the world.
     *
     * @param parameters The adapter startup parameters.
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean start(final Map<String, String> parameters) throws IOException {
        return true;
    }

    /**
     * Stops the connector hosting.
     *
     * @param saveState {@literal true} to save previously exposed attributes for reuse; otherwise,
     *                       clear internal list of exposed attributes.
     * @return {@literal true}, if adapter is previously started; otherwise, {@literal false}.
     */
    @Override
    public final boolean stop(final boolean saveState) {
        return true;
    }



    /**
     * Exposes management attributes.
     *
     * @param connector  Management connector that provides access to the specified attributes.
     * @param namespace  The attributes namespace.
     * @param attributes The dictionary of attributes.
     */
    @Override
    public final void exposeAttributes(final AttributeSupport connector, final String namespace, final Map<String, AttributeConfiguration> attributes) {
        this.attributes.putAll(connector, namespace, attributes);
    }

    /**
     * Reads the attribute value in type-safe manner.
     * @param namespace
     * @param id
     * @param attributeType Type of the attribute value.
     * @param defaultValue The default value of the attribute if it cannot be obtained from the underlying connector.
     * @param <T> Type of the attribute value.
     * @return Strongly typed attribute value.
     */
    protected final <T> T getAttribute(final String namespace, final String id, final Class<T> attributeType, final T defaultValue){
        return this.attributes.getAttribute(namespace, id, attributeType, defaultValue, TimeSpan.INFINITE);
    }

    /**
     * Sets the attribute value.
     * @param namespace The attribute namespace.
     * @param id The attribute identifier.
     * @param value A new attribute value.
     * @return {@literal true}, if attribute is overwritten successfully; otherwise, {@literal false};
     */
    protected final boolean setAttribute(final String namespace, final String id, final Object value){
        return this.attributes.setAttribute(namespace, id, value, TimeSpan.INFINITE);
    }

    /**
     * Exposes monitoring events.
     *
     * @param connector The management connector that provides notification listening and subscribing.
     * @param namespace The events namespace.
     * @param events    The collection of configured notifications.
     */
    @Override
    public final void exposeEvents(final NotificationSupport connector, final String namespace, final Map<String, EventConfiguration> events) {
        notifications.putAll(connector, namespace, events);
    }

    /**
     * Attaches the specified notification listener.
     * @param namespace
     * @param postfix
     * @param listener
     * @return An identifier of the subscription.
     */
    protected final Object subscribe(final String namespace, final String postfix, final NotificationListener listener){
        return notifications.subscribe(namespace, postfix, listener);
    }

    /**
     * Removes the subscription.
     * @param listenerId An identifier of the subscription returned by {@link #subscribe(String, String, com.snamp.connectors.NotificationSupport.NotificationListener)} method.
     * @return {@literal true}, if the specified subscription is removed successfully; otherwise, {@literal false}.
     */
    protected final boolean unsubscribe(final Object listenerId){
        return listenerId instanceof AbstractSubscriptionList.Subscription &&
                ((AbstractSubscriptionList.Subscription)listenerId).unsubscribe();
    }

    /**
     * Releases all resources associated with this adapter.
     */
    @Override
    public void close() {
        attributes.clear();
    }

    private static final boolean isVoid(final Class<?> t){
        return t == null || Objects.equals(void.class, t) || Objects.equals(Void.class, t);
    }
}
