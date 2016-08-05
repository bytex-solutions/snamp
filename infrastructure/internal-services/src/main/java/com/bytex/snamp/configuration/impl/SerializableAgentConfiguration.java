package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.SerializableMap;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.google.common.collect.ForwardingMap;

import java.io.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents in-memory agent configuration that can be stored as serialized Java object.
 * @author Roman Sakno
 * @since 1.2
 * @version 1.2
 */
public final class SerializableAgentConfiguration extends AbstractAgentConfiguration implements Externalizable {
    private final static byte FORMAT_VERSION = 1;
    private static final long serialVersionUID = 8461144056430141155L;

    static abstract class Resettable{
        abstract void reset();
    }

    /**
     * Represents serializable configuration entity.
     * @author Roman Sakno
     * @since 1.2
     * @version 1.2
     */
    interface SerializableEntityConfiguration extends EntityConfiguration, Modifiable, Externalizable{
        /**
         * Determines whether this configuration entity is modified after deserialization.
         * @return {@literal true}, if this configuration entity is modified; otherwise, {@literal false}.
         */
        @Override
        boolean isModified();
    }

    @SuppressWarnings("NullableProblems")
    private static abstract class ModifiableMap<K, V> extends ForwardingMap<K, V> implements Externalizable, Modifiable, SerializableMap<K, V>{
        private static final long serialVersionUID = -8689048750446731607L;
        private transient boolean modified = false;

        @Override
        public boolean isModified() {
            return modified;
        }

        @Override
        public final V remove(final Object key) {
            modified = containsKey(key);
            return super.remove(key);
        }

        @Override
        public final void clear() {
            modified = true;
            super.clear();
        }

        @Override
        public final V put(final K key, final V value) {
            modified = true;
            return super.put(key, value);
        }

        @Override
        public final void putAll(final Map<? extends K, ? extends V> map) {
            modified = true;
            super.putAll(map);
        }

        void reset() {
            modified = false;
        }

        protected abstract void writeKey(final K key, final ObjectOutput out) throws IOException;

        protected abstract void writeValue(final V value, final ObjectOutput out) throws IOException;

        protected abstract K readKey(final ObjectInput out) throws IOException;

        protected abstract V readValue(final ObjectInput out) throws IOException, ClassNotFoundException;

        @Override
        public final void writeExternal(final ObjectOutput out) throws IOException {
            out.writeInt(size());
            for(final Entry<K, V> entry: entrySet()){
                writeKey(entry.getKey(), out);
                writeValue(entry.getValue(), out);
            }
        }

        @Override
        public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            final int size = in.readInt();
            for(int i = 0; i < size; i++){
                final K key = readKey(in);
                final V value = readValue(in);
                if(key != null && value != null)
                    put(key, value);
            }
        }
    }


    private static final class ModifiableParameters extends ModifiableMap<String, String> implements Serializable, Modifiable{
        private static final long serialVersionUID = 6594540590402879949L;
        private final HashMap<String, String> parameters;

        @SpecialUse
        public ModifiableParameters(){
            parameters = new HashMap<>(10);
        }

        @Override
        protected HashMap<String, String> delegate() {
            return parameters;
        }

        @Override
        protected void writeKey(final String key, final ObjectOutput out) throws IOException {
            out.writeUTF(key);
        }

        @Override
        protected void writeValue(final String value, final ObjectOutput out) throws IOException {
            out.writeUTF(value);
        }

        @Override
        protected String readKey(final ObjectInput out) throws IOException {
            return out.readUTF();
        }

        @Override
        protected String readValue(final ObjectInput out) throws IOException {
            return out.readUTF();
        }
    }

    private static abstract class ConfigurationEntityRegistry<E extends EntityConfiguration> extends ModifiableMap<String, E> implements EntityMap<E>{
        private static final long serialVersionUID = -3859844548619883398L;
        private final HashMap<String, E> entities;

        private ConfigurationEntityRegistry(){
            entities = new HashMap<>(10);
        }

        private <ERROR extends Exception> void modifiedEntries(final EntryReader<String, ? super E, ERROR> reader) throws ERROR{
            for(final Entry<String, E> e: entrySet()){
                final E entity = e.getValue();
                final String name = e.getKey();
                if(entity instanceof Modifiable && ((Modifiable)entity).isModified())
                    if(!reader.read(name, entity)) break;
            }
        }

        @Override
        public final E getOrAdd(final String entityID) {
            final E result;
            if(containsKey(entityID))
                result = get(entityID);
            else {
                result = createEntity();
                put(entityID, result);
            }
            return result;
        }

        @Override
        protected final void writeValue(final E value, final ObjectOutput out) throws IOException {
            out.writeObject(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected final E readValue(final ObjectInput out) throws IOException, ClassNotFoundException {
            return (E) out.readObject();
        }

        @Override
        protected final HashMap<String, E> delegate() {
            return entities;
        }

        @Override
        public final boolean isModified() {
            if(super.isModified()) return true;
            else for(final EntityConfiguration entity: values())
                if(entity instanceof Modifiable && ((Modifiable)entity).isModified()) return true;
            return false;
        }

        @Override
        public final void reset() {
            super.reset();
            values().stream().filter(entity -> entity instanceof Resettable).forEach(entity -> ((Resettable) entity).reset());
        }

        @Override
        protected final void writeKey(final String key, final ObjectOutput out) throws IOException {
            out.writeUTF(key);
        }

        @Override
        protected final String readKey(final ObjectInput out) throws IOException {
            return out.readUTF();
        }

        protected abstract E createEntity();
    }

    private static final class AdapterRegistry extends ConfigurationEntityRegistry<SerializableResourceAdapterConfiguration>{
        private static final long serialVersionUID = 8142154170844526063L;

        @SpecialUse
        public AdapterRegistry() {
        }

        @Override
        protected SerializableResourceAdapterConfiguration createEntity() {
            return new SerializableResourceAdapterConfiguration();
        }
    }

    private static final class ResourceRegistry extends ConfigurationEntityRegistry<SerializableManagedResourceConfiguration>{
        private static final long serialVersionUID = 8031527910928209252L;

        @SpecialUse
        public ResourceRegistry() {
        }

        @Override
        protected SerializableManagedResourceConfiguration createEntity() {
            return new SerializableManagedResourceConfiguration();
        }
    }

    private abstract static class AbstractEntityConfiguration extends Resettable implements SerializableEntityConfiguration {
        private static final long serialVersionUID = -8455277079119895844L;
        private transient boolean modified;
        private final ModifiableParameters parameters;

        AbstractEntityConfiguration(){
            parameters = new ModifiableParameters();
            modified = false;
        }

        final void writeParameters(final ObjectOutput out) throws IOException {
            parameters.writeExternal(out);
        }

        final void readParameters(final ObjectInput in) throws IOException, ClassNotFoundException {
            parameters.readExternal(in);
        }

        @Override
        final void reset() {
            modified = false;
            parameters.reset();
            resetAdditionally();
        }

        @Override
        public final void setParameters(final Map<String, String> value) {
            parameters.clear();
            parameters.putAll(value);
            markAsModified();
        }

        void resetAdditionally(){

        }

        final void markAsModified(){
            modified = true;
        }

        /**
         * Determines whether this configuration entity is modified after deserialization.
         *
         * @return {@literal true}, if this configuration entity is modified; otherwise, {@literal false}.
         */
        @Override
        public boolean isModified() {
            return modified || parameters.isModified();
        }

        /**
         * Gets serializable configuration parameters of this entity.
         *
         * @return A map of configuration parameters.
         */
        @Override
        public final SerializableMap<String, String> getParameters() {
            return parameters;
        }

    }

    /**
     * Represents adapter settings. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.2
     * @version 1.2
     */
    public static final class SerializableResourceAdapterConfiguration extends AbstractEntityConfiguration implements ResourceAdapterConfiguration{
        private static final byte FORMAT_VERSION = 1;
        private static final long serialVersionUID = 7926704115151740217L;
        private String adapterName;

        /**
         * Initializes a new empty adapter settings.
         */
        @SpecialUse
        public SerializableResourceAdapterConfiguration(){
            adapterName = "";
        }

        /**
         * Gets the hosting adapter name.
         *
         * @return The name of the adapter.
         */
        @Override
        public String getAdapterName() {
            return adapterName;
        }

        /**
         * Sets the hosting adapter name.
         *
         * @param adapterName The adapter name.
         */
        @Override
        public void setAdapterName(final String adapterName) {
            markAsModified();
            this.adapterName = adapterName != null ? adapterName : "";
        }

        boolean equals(final ResourceAdapterConfiguration other){
            return AbstractAgentConfiguration.equals(this, other);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof ResourceAdapterConfiguration &&
                    equals((ResourceAdapterConfiguration)other);
        }

        @Override
        public int hashCode() {
            return getParameters().hashCode() ^ adapterName.hashCode();
        }

        /**
         * The object implements the writeExternal method to save its contents
         * by calling the methods of DataOutput for its primitive values or
         * calling the writeObject method of ObjectOutput for objects, strings,
         * and arrays.
         *
         * @param out the stream to write the object to
         * @throws IOException Includes any I/O exceptions that may occur
         * @serialData Overriding methods should use this tag to describe
         * the data layout of this Externalizable object.
         * List the sequence of element types and, if possible,
         * relate the element to a public/protected field and/or
         * method of this Externalizable class.
         */
        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeByte(FORMAT_VERSION);
            out.writeUTF(adapterName != null ? adapterName : "");
            writeParameters(out);
        }

        /**
         * The object implements the readExternal method to restore its
         * contents by calling the methods of DataInput for primitive
         * types and readObject for objects, strings and arrays.  The
         * readExternal method must read the values in the same sequence
         * and with the same types as were written by writeExternal.
         *
         * @param in the stream to read data from in order to restore the object
         * @throws IOException    if I/O errors occur
         * @throws ClassNotFoundException If the class for an object being
         *                                restored cannot be found.
         */
        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            final byte version = in.readByte();
            if(version != FORMAT_VERSION)
                throw new IOException(String.format("Adapter configuration has invalid binary format version. Expected %s but actual %s", FORMAT_VERSION, version));
            adapterName = in.readUTF();
            readParameters(in);
        }
    }

    /**
     * Represents configuration of the management information provider. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.2
     * @version 1.2
     */
    static final class SerializableManagedResourceConfiguration extends AbstractEntityConfiguration implements ManagedResourceConfiguration{
        private static final long serialVersionUID = 5044050385424748355L;

        private static final class OperationRegistry extends ConfigurationEntityRegistry<SerializableManagedResourceConfiguration.SerializableOperationConfiguration>{
            private static final long serialVersionUID = -6621970441951257198L;

            @SpecialUse
            public OperationRegistry(){

            }

            @Override
            protected SerializableOperationConfiguration createEntity() {
                return new SerializableOperationConfiguration();
            }
        }

        private static final class AttributeRegistry extends ConfigurationEntityRegistry<SerializableManagedResourceConfiguration.SerializableAttributeConfiguration>{
            private static final long serialVersionUID = -9035924377259475433L;

            @SpecialUse
            public AttributeRegistry() {
            }

            @Override
            protected SerializableAttributeConfiguration createEntity() {
                return new SerializableAttributeConfiguration();
            }
        }

        private static final class EventRegistry extends ConfigurationEntityRegistry<SerializableManagedResourceConfiguration.SerializableEventConfiguration>{
            private static final long serialVersionUID = -4425614353529830020L;

            @SpecialUse
            public EventRegistry() {
            }

            @Override
            protected SerializableEventConfiguration createEntity() {
                return new SerializableEventConfiguration();
            }
        }

        private static abstract class AbstractFeatureConfiguration extends AbstractEntityConfiguration implements FeatureConfiguration {
            private static final long serialVersionUID = -1609210097027316240L;
        }

        /**
         * Represents configuration of the managed resource operation. This class cannot be inherited.
         * @author Roman Sakno
         * @since 1.2
         * @version 1.2
         */
        public static final class SerializableOperationConfiguration extends AbstractFeatureConfiguration implements OperationConfiguration{
            private static final long serialVersionUID = 8267389949041604889L;
            private Duration timeout;

            @SpecialUse
            public SerializableOperationConfiguration(){

            }

            /**
             * The object implements the writeExternal method to save its contents
             * by calling the methods of DataOutput for its primitive values or
             * calling the writeObject method of ObjectOutput for objects, strings,
             * and arrays.
             *
             * @param out the stream to write the object to
             * @throws IOException Includes any I/O exceptions that may occur
             * @serialData Overriding methods should use this tag to describe
             * the data layout of this Externalizable object.
             * List the sequence of element types and, if possible,
             * relate the element to a public/protected field and/or
             * method of this Externalizable class.
             */
            @Override
            public void writeExternal(final ObjectOutput out) throws IOException {
                out.writeLong(timeout != null ? timeout.toMillis() : -1L);
                writeParameters(out);
            }

            /**
             * The object implements the readExternal method to restore its
             * contents by calling the methods of DataInput for primitive
             * types and readObject for objects, strings and arrays.  The
             * readExternal method must read the values in the same sequence
             * and with the same types as were written by writeExternal.
             *
             * @param in the stream to read data from in order to restore the object
             * @throws IOException            if I/O errors occur
             * @throws ClassNotFoundException If the class for an object being
             *                                restored cannot be found.
             */
            @Override
            public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
                long timeout = in.readLong();
                this.timeout = timeout < 0L ? null : Duration.ofMillis(timeout);
                readParameters(in);
            }

            @Override
            public Duration getInvocationTimeout() {
                return timeout;
            }

            @Override
            public void setInvocationTimeout(final Duration value) {
                markAsModified();
                this.timeout = value;
            }

            private boolean equals(final OperationConfiguration other){
                return AbstractAgentConfiguration.equals(this, other);
            }

            @Override
            public boolean equals(final Object other) {
                return other instanceof OperationConfiguration && equals((OperationConfiguration)other);
            }
        }

        /**
         * Represents configuration of the event source. This class cannot be inherited.
         * @author Roman Sakno
         * @since 1.2
         * @version 1.2
         */
        static final class SerializableEventConfiguration extends AbstractFeatureConfiguration implements EventConfiguration {
            private static final long serialVersionUID = -6838585011981639479L;

            @SpecialUse
            public SerializableEventConfiguration(){

            }

            /**
             * The object implements the writeExternal method to save its contents
             * by calling the methods of DataOutput for its primitive values or
             * calling the writeObject method of ObjectOutput for objects, strings,
             * and arrays.
             *
             * @param out the stream to write the object to
             * @throws IOException Includes any I/O exceptions that may occur
             * @serialData Overriding methods should use this tag to describe
             * the data layout of this Externalizable object.
             * List the sequence of element types and, if possible,
             * relate the element to a public/protected field and/or
             * method of this Externalizable class.
             */
            @Override
            public void writeExternal(final ObjectOutput out) throws IOException {
                writeParameters(out);
            }

            /**
             * The object implements the readExternal method to restore its
             * contents by calling the methods of DataInput for primitive
             * types and readObject for objects, strings and arrays.  The
             * readExternal method must read the values in the same sequence
             * and with the same types as were written by writeExternal.
             *
             * @param in the stream to read data from in order to restore the object
             * @throws IOException    if I/O errors occur
             * @throws ClassNotFoundException If the class for an object being
             *                                restored cannot be found.
             */
            @Override
            public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
                readParameters(in);
            }

            private boolean equals(final EventConfiguration other) {
                return AbstractAgentConfiguration.equals(this, other);
            }

            @Override
            public boolean equals(final Object other) {
                return other instanceof EventConfiguration && equals((EventConfiguration) other);
            }

            /**
             * Computes hash code for this object.
             *
             * @return The hash code of this object.
             */
            @Override
            public int hashCode() {
                return getParameters().hashCode();
            }
        }

        /**
         * Represents configuration of the management attribute. This class cannot be inherited.
         * @since 1.2
         * @version 1.2
         */
        static final class SerializableAttributeConfiguration extends AbstractFeatureConfiguration implements AttributeConfiguration{
            private static final long serialVersionUID = -2134014000719123759L;
            private Duration readWriteTimeout;

            @SpecialUse
            public SerializableAttributeConfiguration() {
            }

            /**
             * The object implements the writeExternal method to save its contents
             * by calling the methods of DataOutput for its primitive values or
             * calling the writeObject method of ObjectOutput for objects, strings,
             * and arrays.
             *
             * @param out the stream to write the object to
             * @throws IOException Includes any I/O exceptions that may occur
             * @serialData Overriding methods should use this tag to describe
             * the data layout of this Externalizable object.
             * List the sequence of element types and, if possible,
             * relate the element to a public/protected field and/or
             * method of this Externalizable class.
             */
            @Override
            public void writeExternal(final ObjectOutput out) throws IOException {
                out.writeLong(readWriteTimeout != null ? readWriteTimeout.toMillis() : -1L);
                writeParameters(out);
            }

            /**
             * The object implements the readExternal method to restore its
             * contents by calling the methods of DataInput for primitive
             * types and readObject for objects, strings and arrays.  The
             * readExternal method must read the values in the same sequence
             * and with the same types as were written by writeExternal.
             *
             * @param in the stream to read data from in order to restore the object
             * @throws IOException    if I/O errors occur
             * @throws ClassNotFoundException If the class for an object being
             *                                restored cannot be found.
             */
            @Override
            public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
                final long timeout = in.readLong();
                readWriteTimeout = timeout < 0L ? null : Duration.ofMillis(timeout);
                readParameters(in);
            }

            /**
             * Gets attribute value invoke/write operation timeout.
             *
             * @return The attribute invoke/write operation timeout.
             */
            @Override
            public Duration getReadWriteTimeout() {
                return readWriteTimeout;
            }

            /**
             * Sets attribute value invoke/write operation timeout.
             * @param timeout A new value invoke/write operation timeout.
             */
            @Override
            public void setReadWriteTimeout(final Duration timeout) {
                markAsModified();
                this.readWriteTimeout = timeout;
            }

            private boolean equals(final AttributeConfiguration other){
                return AbstractAgentConfiguration.equals(this, other);
            }

            @Override
            public boolean equals(final Object other) {
                return other instanceof AttributeConfiguration &&
                        equals((AttributeConfiguration)other);
            }
        }

        private static final byte FORMAT_VERSION = 1;
        private String connectionString;
        private final ConfigurationEntityRegistry<SerializableAttributeConfiguration> attributes;
        private String connectionType;
        private final ConfigurationEntityRegistry<SerializableEventConfiguration> events;
        private final ConfigurationEntityRegistry<SerializableOperationConfiguration> operations;

        /**
         * Initializes a new empty configuration of the management information source.
         */
        @SpecialUse
        public SerializableManagedResourceConfiguration(){
            connectionString = connectionType = "";
            this.attributes = new AttributeRegistry();
            this.events = new EventRegistry();
            this.operations = new OperationRegistry();
        }

        /**
         * The object implements the writeExternal method to save its contents
         * by calling the methods of DataOutput for its primitive values or
         * calling the writeObject method of ObjectOutput for objects, strings,
         * and arrays.
         *
         * @param out the stream to write the object to
         * @throws IOException Includes any I/O exceptions that may occur
         * @serialData Overriding methods should use this tag to describe
         * the data layout of this Externalizable object.
         * List the sequence of element types and, if possible,
         * relate the element to a public/protected field and/or
         * method of this Externalizable class.
         */
        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeByte(FORMAT_VERSION);
            out.writeUTF(connectionType != null ? connectionType : "");
            out.writeUTF(connectionString != null ? connectionString : "");
            writeParameters(out);
            attributes.writeExternal(out);
            events.writeExternal(out);
        }

        /**
         * The object implements the readExternal method to restore its
         * contents by calling the methods of DataInput for primitive
         * types and readObject for objects, strings and arrays.  The
         * readExternal method must read the values in the same sequence
         * and with the same types as were written by writeExternal.
         *
         * @param in the stream to read data from in order to restore the object
         * @throws IOException    if I/O errors occur
         * @throws ClassNotFoundException If the class for an object being
         *                                restored cannot be found.
         */
        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            final byte version = in.readByte();
            if(version != FORMAT_VERSION)
                throw new IOException(String.format("Managed resource configuration has invalid binary version. Expected %s but actual %s", FORMAT_VERSION, version));
            connectionType = in.readUTF();
            connectionString = in.readUTF();
            readParameters(in);
            attributes.readExternal(in);
            events.readExternal(in);
        }

        /**
         * Overwrites a set of operations.
         * @param operations A new set of operations.
         */
        void setOperations(final Map<String, ? extends SerializableOperationConfiguration> operations){
            this.operations.clear();
            this.operations.putAll(operations);
        }

        /**
         * Overwrites a set of attributes.
         * @param attributes A new set of attributes.
         */
        void setAttributes(final Map<String, ? extends SerializableAttributeConfiguration> attributes) {
            this.attributes.clear();
            this.attributes.putAll(attributes);
        }

        /**
         * Overwrites a set of events.
         * @param events A new set of events.
         */
        void setEvents(final Map<String, ? extends SerializableEventConfiguration> events){
            this.events.clear();
            this.events.putAll(events);
        }

        @Override
        void resetAdditionally() {
            attributes.reset();
            events.reset();
            operations.reset();
        }

        /**
         * Gets the management target connection string.
         *
         * @return The connection string that is used to connect to the management server.
         */
        @Override
        public String getConnectionString() {
            return connectionString;
        }

        /**
         * Sets the management target connection string.
         *
         * @param connectionString The connection string that is used to connect to the management server.
         */
        @Override
        public void setConnectionString(final String connectionString) {
            markAsModified();
            this.connectionString = connectionString != null ? connectionString : "";
        }

        /**
         * Gets the type of the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         *
         * @return The management connector type.
         */
        @Override
        public String getConnectionType() {
            return connectionType;
        }

        /**
         * Sets the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         *
         * @param connectorType The management connector type.
         */
        @Override
        public void setConnectionType(final String connectorType) {
            markAsModified();
            this.connectionType = connectionType != null ? connectorType : "";
        }

        /**
         * Gets a collection of configured manageable elements for this target.
         *
         * @param featureType The type of the manageable element.
         * @return A map of manageable elements; or {@literal null}, if element type is not supported.
         * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration
         * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T extends FeatureConfiguration> EntityMap<? extends T> getFeatures(final Class<T> featureType) {
            if(featureType == null) return null;
            else if(Objects.equals(featureType, AttributeConfiguration.class))
                return (EntityMap<? extends T>)getAttributes();
            else if(Objects.equals(featureType, EventConfiguration.class))
                return (EntityMap<? extends T>)getEvents();
            else if(Objects.equals(featureType, OperationConfiguration.class))
                return (EntityMap<? extends T>)getOperations();
            else return null;
        }

        /**
         * Returns a set of configured operations.
         * @return A set of configured operations.
         */
        EntityMap<SerializableOperationConfiguration> getOperations(){
            return operations;
        }

        /**
         * Returns the managed resource attributes (key is a attribute identifier).
         *
         * @return The dictionary of management managementAttributes.
         */
        EntityMap<SerializableAttributeConfiguration> getAttributes() {
            return attributes;
        }

        /**
         * Returns the event sources.
         *
         * @return A set of event sources.
         */
        EntityMap<SerializableEventConfiguration> getEvents() {
            return events;
        }

        /**
         * Determines whether this configuration entity is modified after deserialization.
         *
         * @return {@literal true}, if this configuration entity is modified; otherwise, {@literal false}.
         */
        @Override
        public boolean isModified() {
            return super.isModified() || attributes.isModified() || events.isModified();
        }

        private boolean equalsImpl(final ManagedResourceConfiguration other){
            return AbstractAgentConfiguration.equals(this, other);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof ManagedResourceConfiguration &&
                    equalsImpl((ManagedResourceConfiguration)other);
        }

        @Override
        public int hashCode() {
            return connectionString.hashCode() ^
                    connectionType.hashCode() ^
                    attributes.hashCode() ^
                    events.hashCode();
        }
    }

    private final ConfigurationEntityRegistry<SerializableManagedResourceConfiguration> resources;
    private final ConfigurationEntityRegistry<SerializableResourceAdapterConfiguration> adapters;

    /**
     * Initializes a new empty agent configuration.
     */
    @SpecialUse
    public SerializableAgentConfiguration(){
        adapters = new AdapterRegistry();
        resources = new ResourceRegistry();
    }

    /**
     * Clones this instance of agent configuration.
     *
     * @return A new cloned instance of the {@link SerializableAgentConfiguration}.
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public SerializableAgentConfiguration clone() {
        final SerializableAgentConfiguration clonedConfig = new SerializableAgentConfiguration();
        clonedConfig.load(this);
        clonedConfig.reset();
        return clonedConfig;
    }

    private void reset(){
        adapters.reset();
        resources.reset();
    }

    <E extends Exception> void modifiedResources(final EntryReader<String, ? super SerializableManagedResourceConfiguration, E> handler) throws E{
        resources.modifiedEntries(handler);
    }

    <E extends Exception> void modifiedAdapters(final EntryReader<String, ? super SerializableResourceAdapterConfiguration, E> handler) throws E{
        adapters.modifiedEntries(handler);
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeByte(FORMAT_VERSION);
        //write adapters
        adapters.writeExternal(out);
        //write connectors
        resources.writeExternal(out);
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException    if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte();
        //check version
        if(version != FORMAT_VERSION)
            throw new IOException(String.format("Unknown version of configuration format. Expected %s but actual %s", FORMAT_VERSION, version));
        //read adapters
        adapters.readExternal(in);
        //read connectors
        resources.readExternal(in);
    }

    /**
     * Determines whether this configuration is empty.
     * @return {@literal true}, if this configuration is empty; otherwise, {@literal false}.
     */
    boolean isEmpty(){
        return adapters.isEmpty() && resources.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends EntityConfiguration> ConfigurationEntityRegistry<? extends E> getEntities(final Class<E> entityType) {
        final ConfigurationEntityRegistry result;
        if (entityType.isAssignableFrom(SerializableManagedResourceConfiguration.class))
            result = resources;
        else if (entityType.isAssignableFrom(SerializableResourceAdapterConfiguration.class))
            result = adapters;
        else
            result = null;
        return result;
    }

    ConfigurationEntityRegistry<SerializableResourceAdapterConfiguration> getResourceAdapters() {
        return adapters;
    }


    ConfigurationEntityRegistry<SerializableManagedResourceConfiguration> getManagedResources() {
        return resources;
    }

    /**
     * Obtain configuration of SNAMP entity or register new.
     *
     * @param entityType Type of entity. You can use {@link ManagedResourceConfiguration} or {@link ResourceAdapterConfiguration} as entities.
     * @param entityID   Entity ID.
     * @return An instance of existing entity or newly created entity. {@literal null}, if entity type is not supported by SNAMP configuration subsystem.
     * @since 1.2
     */
    @Override
    public <E extends EntityConfiguration> E getOrRegisterEntity(final Class<E> entityType, final String entityID) {
        if (entityType.isAssignableFrom(SerializableManagedResourceConfiguration.class))
            return entityType.cast(resources.getOrAdd(entityID));
        else if (entityType.isAssignableFrom(SerializableResourceAdapterConfiguration.class))
            return entityType.cast(adapters.getOrAdd(entityID));
        else
            return null;
    }

    /**
     * Creates a new instance of entity configuration.
     *
     * @param entityType Type of entity. Can be {@link ManagedResourceConfiguration},
     *                   {@link ResourceAdapterConfiguration}. {@link ManagedResourceConfiguration.AttributeConfiguration}, {@link ManagedResourceConfiguration.EventConfiguration}, {@link ManagedResourceConfiguration.OperationConfiguration}.
     * @return A new instance of entity configuration; or {@literal null}, if entity is not supported.
     */
    @Override
    public <E extends EntityConfiguration> E createEntityConfiguration(final Class<E> entityType) {
        return newEntityConfiguration(entityType);
    }

    /**
     * Creates a new instance of entity configuration.
     *
     * @param entityType Type of entity. Can be {@link ManagedResourceConfiguration},
     *                   {@link ResourceAdapterConfiguration}. {@link ManagedResourceConfiguration.AttributeConfiguration}, {@link ManagedResourceConfiguration.EventConfiguration}, {@link ManagedResourceConfiguration.OperationConfiguration}.
     * @return A new instance of entity configuration; or {@literal null}, if entity is not supported.
     */
    public static  <E extends EntityConfiguration> E newEntityConfiguration(final Class<E> entityType) {
        if(entityType.isAssignableFrom(SerializableManagedResourceConfiguration.class))
            return entityType.cast(new SerializableManagedResourceConfiguration());
        else if(entityType.isAssignableFrom(SerializableResourceAdapterConfiguration.class))
            return entityType.cast(new SerializableResourceAdapterConfiguration());
        else if(entityType.isAssignableFrom(SerializableManagedResourceConfiguration.SerializableAttributeConfiguration.class))
            return entityType.cast(new SerializableManagedResourceConfiguration.SerializableAttributeConfiguration());
        else if(entityType.isAssignableFrom(SerializableManagedResourceConfiguration.SerializableEventConfiguration.class))
            return entityType.cast(new SerializableManagedResourceConfiguration.SerializableEventConfiguration());
        else if(entityType.isAssignableFrom(SerializableManagedResourceConfiguration.SerializableOperationConfiguration.class))
            return entityType.cast(new SerializableManagedResourceConfiguration.SerializableOperationConfiguration());
        else return null;
    }
}
