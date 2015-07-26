/**
 * Provides a set of classes that helps to model and implement internal structure of the resource adapter:
 * <li>
 *     <ul>{@link com.bytex.snamp.adapters.modeling.ModelOfAttributes} - a storage for a set of connected attributes. Each attribute in
 *     this storage is represented by {@link com.bytex.snamp.adapters.modeling.AttributeAccessor} class or its derivatives</ul>
 *     <ul>{@link com.bytex.snamp.adapters.modeling.ModelOfNotifications} - a storage for a set of connected attributes. Each attribute in
 *     this storage is represented by {@link com.bytex.snamp.adapters.modeling.NotificationAccessor} class</ul>
 * </li>
 * Information model of the resource consists of the specified views: {@link com.bytex.snamp.connectors.attributes.AttributeSupport},
 * {@link com.bytex.snamp.connectors.notifications.NotificationSupport} and etc. So the view represents an entry point for accessing
 * all features of the specified type. But resource adapter is a multiplexer of many connected resources. Therefore, the resource adapter performs
 * a decomposition on the managed resource so that each feature is represented as a separated entity without
 * aggregation by {@link com.bytex.snamp.connectors.ManagedResourceConnector} or one of its views. Decoupled features are
 * represented as separated accessors derived from {@link com.bytex.snamp.adapters.modeling.FeatureAccessor} class such
 * as {@link com.bytex.snamp.adapters.modeling.AttributeAccessor} or {@link com.bytex.snamp.adapters.modeling.NotificationAccessor}.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
package com.bytex.snamp.adapters.modeling;