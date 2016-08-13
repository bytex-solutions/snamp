package com.bytex.snamp.configuration;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

/**
 * Represents management target configuration (back-end management information providers).
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface ManagedResourceConfiguration extends TypedEntityConfiguration {

    /**
     * Represents name of configuration parameter that can be used to enable Smart mode of the connector.
     */
    String SMART_MODE_KEY = "smartMode";

    /**
     * Represents name of configuration parameter that points to thread pool in {@link com.bytex.snamp.concurrent.ThreadPoolRepository}
     * service used by connector.
     * @since 1.2
     */
    String THREAD_POOL_KEY = "threadPool";

    /**
     * Represents a feature of the managed resource.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    interface FeatureConfiguration extends EntityConfiguration {
        /**
         * Represents configuration parameter containing alternative name of the feature.
         */
        String NAME_KEY = "name";

        /**
         * Represents configuration parameter indicating that this feature was created by machine, not by human.
         */
        String AUTOMATICALLY_ADDED_KEY = "automaticallyAdded";

        default void setAlternativeName(final String value){
            getParameters().put(NAME_KEY, value);
        }

        default String getAlternativeName(){
            return getParameters().get(NAME_KEY);
        }

        default boolean isAutomaticallyAdded(){
            return getParameters().containsKey(AUTOMATICALLY_ADDED_KEY);
        }

        default void setAutomaticallyAdded(final boolean value){
            if(value)
                getParameters().put(AUTOMATICALLY_ADDED_KEY, Boolean.TRUE.toString());
            else
                getParameters().remove(AUTOMATICALLY_ADDED_KEY);
        }
    }

    /**
     * Represents event configuration.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    interface EventConfiguration extends FeatureConfiguration {
    }

    /**
     * Represents attribute configuration.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    interface AttributeConfiguration extends FeatureConfiguration {
        /**
         * Recommended timeout for read/write of attribute in smart mode.
         */
        Duration TIMEOUT_FOR_SMART_MODE = Duration.ofSeconds(10);

        /**
         * Gets attribute value invoke/write operation timeout.
         * @return Gets attribute value invoke/write operation timeout.
         */
        Duration getReadWriteTimeout();

        default long getReadWriteTimeout(final TemporalUnit unit){
            return getReadWriteTimeout().get(unit);
        }

        /**
         * Sets attribute value invoke/write operation timeout.
         * @param value A new value of the timeout.
         */
        void setReadWriteTimeout(final Duration value);

        default void setReadWriteTimeout(final long amount, final TemporalUnit unit){
            setReadWriteTimeout(Duration.of(amount, unit));
        }
    }

    /**
     * Represents configuration of the managed resource operation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    interface OperationConfiguration extends FeatureConfiguration {
        /**
         * Recommended timeout for invocation of operation in smart mode.
         */
        Duration TIMEOUT_FOR_SMART_MODE = Duration.ofSeconds(10);

        /**
         * Gets timeout of operation invocation.
         * @return Timeout value.
         */
        Duration getInvocationTimeout();

        /**
         * Sets timeout of operation invocation.
         * @param value A new timeout value.
         */
        void setInvocationTimeout(final Duration value);
    }

    /**
     * Sets resource group for this resource.
     * @param value The name of the resource group. Cannot be {@literal null}.
     */
    void setGroupName(final String value);

    /**
     * Gets name of resource group.
     * @return Name of resource group; or empty string, if group is not assigned.
     */
    String getGroupName();

    /**
     * Gets the management target connection string.
     * @return The connection string that is used to connect to the management server.
     */
    String getConnectionString();

    /**
     * Sets the management target connection string.
     * @param connectionString The connection string that is used to connect to the management server.
     */
    void setConnectionString(final String connectionString);

    /**
     * Gets a collection of configured manageable elements for this target.
     * @param featureType The type of the manageable element.
     * @param <T> The type of the manageable element.
     * @return A map of manageable elements; or {@literal null}, if element type is not supported.
     * @see AttributeConfiguration
     * @see EventConfiguration
     */
    <T extends FeatureConfiguration> EntityMap<? extends T> getFeatures(final Class<T> featureType);
}
