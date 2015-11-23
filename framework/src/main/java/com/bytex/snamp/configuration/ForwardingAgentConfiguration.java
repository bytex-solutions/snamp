package com.bytex.snamp.configuration;

import com.google.common.collect.ForwardingObject;
import com.bytex.snamp.TimeSpan;

import java.util.Map;

/**
 * Represents an abstract decorator of {@link com.bytex.snamp.configuration.AgentConfiguration}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ForwardingAgentConfiguration extends ForwardingObject implements AgentConfiguration {

    /**
     * Represents an abstract decorator of {@link com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration}.
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
     * Represents an abstract decorator of {@link com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration}.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static abstract class ForwardingManagedResourceConfiguration extends ForwardingObject implements ManagedResourceConfiguration{

        /**
         * Represents an abstract decorator of {@link com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration}.
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
             * @param value Read/write timeout.
             */
            @Override
            public void setReadWriteTimeout(final TimeSpan value) {
                delegate().setReadWriteTimeout(value);
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
         * Represents an abstract decorator of {@link com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration}.
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
         * @param featureType The type of the manageable element.
         * @return A map of manageable elements; or {@literal null}, if element type is not supported.
         * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration
         * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration
         */
        @Override
        public <T extends FeatureConfiguration> EntityMap<? extends T> getFeatures(final Class<T> featureType) {
            return delegate().getFeatures(featureType);
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
    public EntityMap<? extends ResourceAdapterConfiguration> getResourceAdapters() {
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
    public EntityMap<? extends ManagedResourceConfiguration> getManagedResources() {
        return delegate().getManagedResources();
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
