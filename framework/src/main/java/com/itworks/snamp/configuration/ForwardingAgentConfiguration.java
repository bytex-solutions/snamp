package com.itworks.snamp.configuration;

import com.google.common.collect.ForwardingObject;
import com.itworks.snamp.TimeSpan;

import java.util.Map;

/**
 * Represents an abstract decorator of {@link com.itworks.snamp.configuration.AgentConfiguration}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ForwardingAgentConfiguration extends ForwardingObject implements AgentConfiguration {

    /**
     * Represents an abstract decorator of {@link com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration}.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static abstract class ForwardingResourceAdapterConfiguration extends ForwardingObject implements ResourceAdapterConfiguration{
        /**
         * Returns the backing delegate instance that methods are forwarded to.
         * Abstract subclasses generally override this method with an abstract method
         * that has a more specific return type, such as {@link
         * com.google.common.collect.ForwardingSet#delegate}. Concrete subclasses override this method to supply
         * the instance being decorated.
         */
        @Override
        protected abstract ResourceAdapterConfiguration delegate();

        /**
         * Gets the hosting adapter name.
         *
         * @return The hosting adapter name.
         */
        @Override
        public String getAdapterName() {
            return delegate().getAdapterName();
        }

        /**
         * Sets the hosting adapter name.
         *
         * @param adapterName The adapter name.
         */
        @Override
        public void setAdapterName(final String adapterName) {
            delegate().setAdapterName(adapterName);
        }

        /**
         * Gets configuration parameters of this entity.
         *
         * @return A map of configuration parameters.
         */
        @Override
        public Map<String, String> getParameters() {
            return delegate().getParameters();
        }
    }

    /**
     * Represents an abstract decorator of {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration}.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static abstract class ForwardingManagedResourceConfiguration extends ForwardingObject implements ManagedResourceConfiguration{

        /**
         * Represents an abstract decorator of {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration}.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        public static abstract class ForwardingAttributeConfiguration extends ForwardingObject implements AttributeConfiguration{
            /**
             * Gets attribute value invoke/write operation timeout.
             *
             * @return Gets attribute value invoke/write operation timeout.
             */
            @Override
            public TimeSpan getReadWriteTimeout() {
                return delegate().getReadWriteTimeout();
            }

            /**
             * Sets attribute value invoke/write operation timeout.
             *
             * @param time
             */
            @Override
            public void setReadWriteTimeout(final TimeSpan time) {
                delegate().setReadWriteTimeout(time);
            }

            /**
             * Returns the attribute name.
             *
             * @return The attribute name.
             */
            @Override
            public String getAttributeName() {
                return delegate().getAttributeName();
            }

            /**
             * Sets the attribute name.
             *
             * @param attributeName The attribute name.
             */
            @Override
            public void setAttributeName(final String attributeName) {
                delegate().setAttributeName(attributeName);
            }

            /**
             * Gets configuration parameters of this entity.
             *
             * @return A map of configuration parameters.
             */
            @Override
            public Map<String, String> getParameters() {
                return delegate().getParameters();
            }

            /**
             * Returns the backing delegate instance that methods are forwarded to.
             * Abstract subclasses generally override this method with an abstract method
             * that has a more specific return type, such as {@link
             * com.google.common.collect.ForwardingSet#delegate}. Concrete subclasses override this method to supply
             * the instance being decorated.
             */
            @Override
            protected abstract AttributeConfiguration delegate();
        }

        /**
         * Represents an abstract decorator of {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration}.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        public static abstract class ForwardingEventConfiguration extends ForwardingObject implements EventConfiguration{
            /**
             * Gets the event category.
             *
             * @return The event category.
             */
            @Override
            public String getCategory() {
                return delegate().getCategory();
            }

            /**
             * Sets the category of the event to listen.
             *
             * @param eventCategory The category of the event to listen.
             */
            @Override
            public void setCategory(final String eventCategory) {
                delegate().setCategory(eventCategory);
            }

            /**
             * Gets configuration parameters of this entity.
             *
             * @return A map of configuration parameters.
             */
            @Override
            public Map<String, String> getParameters() {
                return delegate().getParameters();
            }

            /**
             * Returns the backing delegate instance that methods are forwarded to.
             * Abstract subclasses generally override this method with an abstract method
             * that has a more specific return type, such as {@link
             * com.google.common.collect.ForwardingSet#delegate}. Concrete subclasses override this method to supply
             * the instance being decorated.
             */
            @Override
            protected abstract EventConfiguration delegate();
        }

        /**
         * Returns the backing delegate instance that methods are forwarded to.
         * Abstract subclasses generally override this method with an abstract method
         * that has a more specific return type, such as {@link
         * com.google.common.collect.ForwardingSet#delegate}. Concrete subclasses override this method to supply
         * the instance being decorated.
         */
        @Override
        protected abstract ManagedResourceConfiguration delegate();

        /**
         * Gets the management target connection string.
         *
         * @return The connection string that is used to connect to the management server.
         */
        @Override
        public String getConnectionString() {
            return delegate().getConnectionString();
        }

        /**
         * Sets the management target connection string.
         *
         * @param connectionString The connection string that is used to connect to the management server.
         */
        @Override
        public void setConnectionString(final String connectionString) {
            delegate().setConnectionString(connectionString);
        }

        /**
         * Gets the type of the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         *
         * @return The management connector type.
         */
        @Override
        public String getConnectionType() {
            return delegate().getConnectionType();
        }

        /**
         * Sets the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         *
         * @param connectorType The management connector type.
         */
        @Override
        public void setConnectionType(final String connectorType) {
            delegate().setConnectionType(connectorType);
        }

        /**
         * Gets a collection of configured manageable elements for this target.
         *
         * @param elementType The type of the manageable element.
         * @return A map of manageable elements; or {@literal null}, if element type is not supported.
         * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration
         * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration
         */
        @Override
        public <T extends FeatureConfiguration> Map<String, T> getElements(final Class<T> elementType) {
            return delegate().getElements(elementType);
        }

        /**
         * Creates a new instances of the specified manageable element.
         *
         * @param elementType Type of the required manageable element.
         * @return A new empty manageable element; or {@literal null},
         * if the specified element type is not supported.
         */
        @Override
        public <T extends FeatureConfiguration> T newElement(final Class<T> elementType) {
            return delegate().newElement(elementType);
        }

        /**
         * Returns the dictionary of additional configuration parameters.
         *
         * @return The dictionary of additional configuration parameters.
         */
        @Override
        public Map<String, String> getParameters() {
            return delegate().getParameters();
        }
    }

    /**
     * Returns the backing delegate instance that methods are forwarded to.
     * Abstract subclasses generally override this method with an abstract method
     * that has a more specific return type, such as {@link
     * com.google.common.collect.ForwardingSet#delegate}. Concrete subclasses override this method to supply
     * the instance being decorated.
     */
    @Override
    protected abstract AgentConfiguration delegate();

    /**
     * Creates clone of this configuration.
     *
     * @return The cloned instance of this configuration.
     */
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    @Override
    public abstract ForwardingAgentConfiguration clone();

    /**
     * Gets a collection of resource adapters.
     * <p>
     * The key represents user-defined unique name of the adapter.
     * </p>
     *
     * @return A collection of resource adapters.
     */
    @Override
    public Map<String, ResourceAdapterConfiguration> getResourceAdapters() {
        return delegate().getResourceAdapters();
    }

    /**
     * Gets a collection of managed resources.
     * <p>
     * The key represents user-defined name of the managed resource.
     * </p>
     *
     * @return The dictionary of managed resources.
     */
    @Override
    public Map<String, ManagedResourceConfiguration> getManagedResources() {
        return delegate().getManagedResources();
    }

    /**
     * Creates a new instance of the configuration entity.
     *
     * @param entityType Type of the entity to instantiate.
     * @return A new instance of the configuration entity; or {@literal null}, if entity
     * is not supported.
     * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration
     * @see com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration
     */
    @Override
    public <T extends EntityConfiguration> T newConfigurationEntity(final Class<T> entityType) {
        return delegate().newConfigurationEntity(entityType);
    }

    /**
     * Imports the state of specified object into this object.
     *
     * @param input The import source.
     */
    @Override
    public void load(final AgentConfiguration input) {
        delegate().load(input);
    }

    /**
     * Clears this configuration.
     */
    @Override
    public void clear() {
        delegate().clear();
    }


}
