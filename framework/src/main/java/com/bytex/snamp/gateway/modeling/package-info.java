/**
 * Provides a set of classes that helps to model and implement internal structure of the gateways:
 * <li>
 *     <ul>{@link com.bytex.snamp.gateway.modeling.ModelOfAttributes} - a storage for a set of connected attributes. Each attribute in
 *     this storage is represented by {@link com.bytex.snamp.gateway.modeling.AttributeAccessor} class or its derivatives</ul>
 *     <ul>{@link com.bytex.snamp.gateway.modeling.ModelOfNotifications} - a storage for a set of connected attributes. Each attribute in
 *     this storage is represented by {@link com.bytex.snamp.gateway.modeling.NotificationAccessor} class</ul>
 * </li>
 * Information model of the resource consists of the specified views: {@link com.bytex.snamp.connector.attributes.AttributeManager},
 * {@link com.bytex.snamp.connector.notifications.NotificationManager} and etc. So the view represents an entry point for accessing
 * all features of the specified type. But gateway is a multiplexer of many connected resources. Therefore, the gateway performs
 * a decomposition on the managed resource so that each feature is represented as a separated entity without
 * aggregation by {@link com.bytex.snamp.connector.ManagedResourceConnector} or one of its views. Decoupled features are
 * represented as separated accessors derived from {@link com.bytex.snamp.gateway.modeling.FeatureAccessor} class such
 * as {@link com.bytex.snamp.gateway.modeling.AttributeAccessor} or {@link com.bytex.snamp.gateway.modeling.NotificationAccessor}.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
package com.bytex.snamp.gateway.modeling;