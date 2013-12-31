package com.snamp.adapters;

import static com.snamp.hosting.AgentConfiguration.ConfigurationEntity;

/**
 * Represents a base interface for all other interfaces that describes
 * publishing of the specified management information.
 * @param <F> Type of the management connector feature, such as {@link com.snamp.connectors.NotificationSupport}.
 * @param <C> Type of the configuration element that describes the configuration of the MIB element.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface FeaturePublisher<F, C extends ConfigurationEntity> {
}
