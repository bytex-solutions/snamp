package com.itworks.snamp.configuration;

import com.itworks.snamp.TimeSpan;

import java.io.*;
import java.util.*;

/**
 * Represents in-memory agent configuration that can be stored as serialized Java object.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class InMemoryAgentConfiguration extends AbstractAgentConfiguration implements Serializable {

    /**
     * Represents adapter settings. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class InMemoryHostingConfiguration implements HostingConfiguration, Serializable{
        private String adapterName;
        private final Map<String, String> additionalElements;

        /**
         * Initializes a new empty adapter settings.
         */
        public InMemoryHostingConfiguration(){
            adapterName = "";
            additionalElements = new HashMap<>(10);
        }

        /**
         * Gets the hosting adapter name.
         *
         * @return The name of the adapter.
         */
        @Override
        public final String getAdapterName() {
            return adapterName;
        }

        /**
         * Sets the hosting adapter name.
         *
         * @param adapterName The adapter name.
         */
        @Override
        public final void setAdapterName(final String adapterName) {
            this.adapterName = adapterName != null ? adapterName : "";
        }

        /**
         * Returns a dictionary of hosting parameters, such as port and hosting address.
         *
         * @return The map of additional hosting parameters.
         */
        @Override
        public Map<String, String> getHostingParams() {
            return additionalElements;
        }
    }

    /**
     * Represents configuration of the management information provider. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class InMemoryManagedResourceConfiguration implements ManagedResourceConfiguration, Serializable{

        /**
         * Represents configuration of the event source. This class cannot be inherited.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        public static final class InMemoryEventConfiguration implements EventConfiguration, Serializable{
            private final Map<String, String> additionalElements = new HashMap<>();
            private String eventCategory = "";


            /**
             * Gets the event category.
             *
             * @return The event category.
             */
            @Override
            public final String getCategory() {
                return eventCategory;
            }

            /**
             * Sets the category of the event to listen.
             *
             * @param eventCategory The category of the event to listen.
             */
            @Override
            public final void setCategory(final String eventCategory) {
                this.eventCategory = eventCategory != null ? eventCategory : "";
            }

            /**
             * Gets a map of event options.
             *
             * @return The map of event options.
             */
            @Override
            public final Map<String, String> getParameters() {
                return additionalElements;
            }

            /**
             * Returns a hash code value for the object. This method is
             * supported for the benefit of hash tables such as those provided by
             * {@link java.util.HashMap}.
             * <p/>
             * The general contract of {@code hashCode} is:
             * <ul>
             * <li>Whenever it is invoked on the same object more than once during
             * an execution of a Java application, the {@code hashCode} method
             * must consistently return the same integer, provided no information
             * used in {@code equals} comparisons on the object is modified.
             * This integer need not remain consistent from one execution of an
             * application to another execution of the same application.
             * <li>If two objects are equal according to the {@code equals(Object)}
             * method, then calling the {@code hashCode} method on each of
             * the two objects must produce the same integer result.
             * <li>It is <em>not</em> required that if two objects are unequal
             * according to the {@link Object#equals(Object)}
             * method, then calling the {@code hashCode} method on each of the
             * two objects must produce distinct integer results.  However, the
             * programmer should be aware that producing distinct integer results
             * for unequal objects may improve the performance of hash tables.
             * </ul>
             * <p/>
             * As much as is reasonably practical, the hashCode method defined by
             * class {@code Object} does return distinct integers for distinct
             * objects. (This is typically implemented by converting the internal
             * address of the object into an integer, but this implementation
             * technique is not required by the
             * Java<font size="-2"><sup>TM</sup></font> programming language.)
             *
             * @return a hash code value for this object.
             * @see Object#equals(Object)
             * @see System#identityHashCode
             */
            @Override
            public final int hashCode() {
                return eventCategory.hashCode();
            }

            /**
             * Indicates whether some other object is "equal to" this one.
             * <p/>
             * The {@code equals} method implements an equivalence relation
             * on non-null object references:
             * <ul>
             * <li>It is <i>reflexive</i>: for any non-null reference value
             * {@code x}, {@code x.equals(x)} should return
             * {@code true}.
             * <li>It is <i>symmetric</i>: for any non-null reference values
             * {@code x} and {@code y}, {@code x.equals(y)}
             * should return {@code true} if and only if
             * {@code y.equals(x)} returns {@code true}.
             * <li>It is <i>transitive</i>: for any non-null reference values
             * {@code x}, {@code y}, and {@code z}, if
             * {@code x.equals(y)} returns {@code true} and
             * {@code y.equals(z)} returns {@code true}, then
             * {@code x.equals(z)} should return {@code true}.
             * <li>It is <i>consistent</i>: for any non-null reference values
             * {@code x} and {@code y}, multiple invocations of
             * {@code x.equals(y)} consistently return {@code true}
             * or consistently return {@code false}, provided no
             * information used in {@code equals} comparisons on the
             * objects is modified.
             * <li>For any non-null reference value {@code x},
             * {@code x.equals(null)} should return {@code false}.
             * </ul>
             * <p/>
             * The {@code equals} method for class {@code Object} implements
             * the most discriminating possible equivalence relation on objects;
             * that is, for any non-null reference values {@code x} and
             * {@code y}, this method returns {@code true} if and only
             * if {@code x} and {@code y} refer to the same object
             * ({@code x == y} has the value {@code true}).
             * <p/>
             * Note that it is generally necessary to override the {@code hashCode}
             * method whenever this method is overridden, so as to maintain the
             * general contract for the {@code hashCode} method, which states
             * that equal objects must have equal hash codes.
             *
             * @param obj the reference object with which to compare.
             * @return {@code true} if this object is the same as the communicableObject
             *         argument; {@code false} otherwise.
             * @see #hashCode()
             * @see java.util.HashMap
             */
            @Override
            public final boolean equals(final Object obj) {
                return obj instanceof EventConfiguration && Objects.equals(((EventConfiguration) obj).getCategory(), getCategory());
            }
        }

        /**
         * Represents configuration of the management attribute. This class cannot be inherited.
         * @since 1.0
         * @version 1.0
         */
        public static final class InMemoryAttributeConfiguration implements AttributeConfiguration, Serializable{
            private TimeSpan readWriteTimeout;
            private String attributeName;
            private final Map<String, String> additionalElements;

            /**
             * Initializes a new configuration of the management attribute.
             */
            public InMemoryAttributeConfiguration(){
                readWriteTimeout = TimeSpan.INFINITE;
                attributeName = "";
                additionalElements = new HashMap<>();
            }

            /**
             * Initializes a new configuration of the management attribute.
             * @param attributeName The name of the management attribute.
             */
            @SuppressWarnings("UnusedDeclaration")
            public InMemoryAttributeConfiguration(final String attributeName){
                this();
                this.attributeName = attributeName;
            }

            /**
             * Gets attribute value invoke/write operation timeout.
             *
             * @return The attribute invoke/write operation timeout.
             */
            @Override
            public final TimeSpan getReadWriteTimeout() {
                return readWriteTimeout;
            }

            /**
             * Sets attribute value invoke/write operation timeout.
             * @param timeout A new value invoke/write operation timeout.
             */
            @Override
            public final void setReadWriteTimeout(final TimeSpan timeout) {
                this.readWriteTimeout = timeout;
            }

            /**
             * Returns the attribute name.
             *
             * @return The attribute name,
             */
            @Override
            public final String getAttributeName() {
                return attributeName;
            }

            /**
             * Sets the attribute name.
             *
             * @param attributeName The attribute name.
             */
            @Override
            public final void setAttributeName(final String attributeName) {
                this.attributeName = attributeName != null ? attributeName : "";
            }

            /**
             * Returns the additional configuration elements.
             *
             * @return Additional options associated with the management attribute configuration.
             */
            @Override
            public final Map<String, String> getParameters() {
                return additionalElements;
            }
        }

        private String connectionString;
        private final Map<String, AttributeConfiguration> attributes;
        private String connectionType;
        private String namespace;
        private final Map<String, String> additionalElements;
        private final Map<String, EventConfiguration> events;

        /**
         * Initializes a new empty configuration of the management information source.
         */
        public InMemoryManagedResourceConfiguration(){
            connectionString = connectionType = namespace = "";
            attributes = new HashMap<>(10);
            additionalElements = new HashMap<>(10);
            this.events = new HashMap<>(10);
        }

        /**
         * Gets the management target connection string.
         *
         * @return The connection string that is used to connect to the management server.
         */
        @Override
        public final String getConnectionString() {
            return connectionString;
        }

        /**
         * Sets the management target connection string.
         *
         * @param connectionString The connection string that is used to connect to the management server.
         */
        @Override
        public final void setConnectionString(final String connectionString) {
            this.connectionString = connectionString != null ? connectionString : "";
        }

        /**
         * Gets the type of the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         *
         * @return The management connector type.
         */
        @Override
        public final String getConnectionType() {
            return connectionType;
        }

        /**
         * Sets the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         *
         * @param connectorType The management connector type.
         */
        @Override
        public final void setConnectionType(final String connectorType) {
            this.connectionType = connectionType != null ? connectorType : "";
        }

        /**
         * Returns the monitoring namespace that is visible outside from the agent and the front-end.
         *
         * @return The namespace of the management target (such as SNMP OID prefix).
         */
        @Override
        public final String getNamespace() {
            return namespace;
        }

        /**
         * Sets the monitoring namespace.
         *
         * @param namespace The namespace of the management target (such as SNMP OID prefix).
         */
        @Override
        public final void setNamespace(final String namespace) {
            this.namespace = namespace != null ? namespace : "";
        }

        /**
         * Gets a collection of configured manageable elements for this target.
         *
         * @param elementType The type of the manageable element.
         * @return A map of manageable elements; or {@literal null}, if element type is not supported.
         * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration
         * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T extends ManagedEntity> Map<String, T> getElements(final Class<T> elementType) {
            if(elementType == null) return null;
            else if(Objects.equals(elementType, AttributeConfiguration.class))
                return (Map<String, T>)getAttributes();
            else if(Objects.equals(elementType, EventConfiguration.class))
                return (Map<String, T>)getEvents();
            else return null;
        }

        /**
         * Creates a new instances of the specified manageable element.
         *
         * @param elementType Type of the required manageable element.
         * @return A new empty manageable element; or {@literal null},
         * if the specified element type is not supported.
         */
        @Override
        public <T extends ManagedEntity> T newElement(final Class<T> elementType) {
            if(elementType == null) return null;
            else if(elementType.isAssignableFrom(InMemoryAttributeConfiguration.class))
                return elementType.cast(newAttributeConfiguration());
            else if(elementType.isAssignableFrom(InMemoryEventConfiguration.class))
                return elementType.cast(newEventConfiguration());
            else return null;
        }

        /**
         * Returns the management managementAttributes (key is a attribute identifier).
         *
         * @return The dictionary of management managementAttributes.
         */
        public final Map<String, AttributeConfiguration> getAttributes() {
            return attributes;
        }

        /**
         * Returns the event sources.
         *
         * @return A set of event sources.
         */
        public final Map<String, EventConfiguration> getEvents() {
            return events;
        }

        /**
         * Returns the dictionary of additional configuration elements.
         *
         * @return The dictionary of additional configuration elements.
         */
        @Override
        public final Map<String, String> getParameters() {
            return additionalElements;
        }

        /**
         * Empty implementation of AttributeConfiguration interface
         *
         * @return implementation of AttributeConfiguration interface
         */
        public final InMemoryAttributeConfiguration newAttributeConfiguration() {
            return new InMemoryAttributeConfiguration();
        }

        /**
         * Creates an empty event configuration.
         * <p>
         * Usually, this method is used for adding new events in the collection
         * returned by {@link #getEvents()} method.
         * </p>
         *
         * @return An empty event configuration.
         */
        public InMemoryEventConfiguration newEventConfiguration() {
            return new InMemoryEventConfiguration();
        }
    }

    private final InMemoryHostingConfiguration hostingConfig;
    private final Map<String, ManagedResourceConfiguration> targets;

    /**
     * Initializes a new empty agent configuration.
     */
    public InMemoryAgentConfiguration(){
        hostingConfig = new InMemoryHostingConfiguration();
        targets = new HashMap<>(10);
    }

    /**
     * Clones this instance of agent configuration.
     *
     * @return A new cloned instance of the {@link InMemoryAgentConfiguration}.
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public InMemoryAgentConfiguration clone() {
        final InMemoryAgentConfiguration clonedConfig = new InMemoryAgentConfiguration();
        clonedConfig.load(this);
        return clonedConfig;
    }

    /**
     * Returns the agent hosting configuration.
     *
     * @return The agent hosting configuration.
     */
    @Override
    public final InMemoryHostingConfiguration getAgentHostingConfig() {
        return hostingConfig;
    }

    /**
     * Represents management targets.
     *
     * @return The dictionary of management targets (management back-ends).
     */
    @Override
    public final Map<String, ManagedResourceConfiguration> getManagedResources() {
        return targets;
    }

    /**
     * Creates a new instance of the {@link com.itworks.snamp.configuration.InMemoryAgentConfiguration.InMemoryManagedResourceConfiguration}.
     *
     * @return A new instance of the {@link com.itworks.snamp.configuration.InMemoryAgentConfiguration.InMemoryManagedResourceConfiguration}.
     */
    @Override
    public final InMemoryManagedResourceConfiguration newManagedResourceConfiguration() {
        return new InMemoryManagedResourceConfiguration();
    }

    /**
     * Serializes this object into the specified stream.
     *
     * @param output An output stream to receive configuration data.
     * @throws java.io.IOException           Some I/O error occurs.
     */
    @Override
    public final void save(final OutputStream output) throws IOException {
        try(final ObjectOutputStream serializer = new ObjectOutputStream(output)){
            serializer.writeObject(this);
        }
    }

    /**
     * Reads the file and fills the current instance.
     *
     * @param input Configuration content source.
     * @throws java.io.IOException           Cannot invoke from the specified stream.
     */
    @Override
    public final void load(final InputStream input) throws IOException {
        try(final ObjectInputStream deserializer = new ObjectInputStream(input)){
            load((AgentConfiguration)deserializer.readObject());
        }
        catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
