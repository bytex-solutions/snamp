package com.itworks.snamp.configuration;

import com.google.common.collect.ForwardingMap;
import com.itworks.snamp.SerializableMap;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.internal.annotations.SpecialUse;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents in-memory agent configuration that can be stored as serialized Java object.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class SerializableAgentConfiguration extends AbstractAgentConfiguration implements Externalizable {
    private final static byte FORMAT_VERSION = 1;
    private static final long serialVersionUID = 8461144056430141155L;

    static abstract class Resettable{
        abstract void reset();
    }

    /**
     * Represents serializable configuration entity.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface SerializableEntityConfiguration extends EntityConfiguration, Modifiable, Externalizable{
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

        public ModifiableParameters(){
            parameters = new HashMap<>(10);
        }

        @Override
        protected HashMap<String, String> delegate() {
            return parameters;
        }

        @Override
        protected final void writeKey(final String key, final ObjectOutput out) throws IOException {
            out.writeUTF(key);
        }

        @Override
        protected final void writeValue(final String value, final ObjectOutput out) throws IOException {
            out.writeUTF(value);
        }

        @Override
        protected final String readKey(final ObjectInput out) throws IOException {
            return out.readUTF();
        }

        @Override
        protected final String readValue(final ObjectInput out) throws IOException {
            return out.readUTF();
        }
    }

    private static abstract class ConfigurationEntityRegistry<E extends EntityConfiguration> extends ModifiableMap<String, E>{
        private static final long serialVersionUID = -3859844548619883398L;
        private final HashMap<String, E> entities;

        private ConfigurationEntityRegistry(){
            entities = new HashMap<>(10);
        }

        private <ERROR extends Exception> void modifiedResources(final RecordReader<String, ? super E, ERROR> reader) throws ERROR{
            for(final Entry<String, E> e: entrySet()){
                final E entity = e.getValue();
                final String name = e.getKey();
                if(entity instanceof Modifiable && ((Modifiable)entity).isModified())
                    reader.read(name, entity);
            }
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
            for(final EntityConfiguration entity: values())
                if(entity instanceof Resettable)
                    ((Resettable)entity).reset();
        }

        @Override
        protected final void writeKey(final String key, final ObjectOutput out) throws IOException {
            out.writeUTF(key);
        }

        @Override
        protected final String readKey(final ObjectInput out) throws IOException {
            return out.readUTF();
        }
    }

    private static final class AdapterRegistry extends ConfigurationEntityRegistry<ResourceAdapterConfiguration>{

        private static final long serialVersionUID = 8142154170844526063L;

        public AdapterRegistry() {
        }

        @Override
        protected void writeValue(final ResourceAdapterConfiguration value, final ObjectOutput out) throws IOException {
            out.writeObject(value);
        }

        @Override
        protected ResourceAdapterConfiguration readValue(final ObjectInput out) throws IOException, ClassNotFoundException {
            return Utils.safeCast(out.readObject(), ResourceAdapterConfiguration.class);
        }
    }

    private static final class ResourceRegistry extends ConfigurationEntityRegistry<ManagedResourceConfiguration>{
        private static final long serialVersionUID = 8031527910928209252L;

        public ResourceRegistry() {
        }

        @Override
        protected void writeValue(final ManagedResourceConfiguration value, final ObjectOutput out) throws IOException {
            out.writeObject(value);
        }

        @Override
        protected ManagedResourceConfiguration readValue(final ObjectInput out) throws IOException, ClassNotFoundException {
            return Utils.safeCast(out.readObject(), ManagedResourceConfiguration.class);
        }
    }

    private static final class OperationRegistry extends ConfigurationEntityRegistry<ManagedResourceConfiguration.OperationConfiguration>{
        private static final long serialVersionUID = -6621970441951257198L;

        @SpecialUse
        public OperationRegistry(){

        }

        @Override
        protected void writeValue(final ManagedResourceConfiguration.OperationConfiguration value, final ObjectOutput out) throws IOException {
            out.writeObject(value);
        }

        @Override
        protected ManagedResourceConfiguration.OperationConfiguration readValue(final ObjectInput out) throws IOException, ClassNotFoundException {
            return Utils.safeCast(out.readObject(), ManagedResourceConfiguration.OperationConfiguration.class);
        }
    }

    private static final class AttributeRegistry extends ConfigurationEntityRegistry<ManagedResourceConfiguration.AttributeConfiguration>{
        private static final long serialVersionUID = -9035924377259475433L;

        @SpecialUse
        public AttributeRegistry() {
        }

        @Override
        protected void writeValue(final ManagedResourceConfiguration.AttributeConfiguration value, final ObjectOutput out) throws IOException {
            out.writeObject(value);
        }

        @Override
        protected ManagedResourceConfiguration.AttributeConfiguration readValue(final ObjectInput out) throws IOException, ClassNotFoundException {
            return Utils.safeCast(out.readObject(), ManagedResourceConfiguration.AttributeConfiguration.class);
        }
    }

    private static final class EventRegistry extends ConfigurationEntityRegistry<ManagedResourceConfiguration.EventConfiguration>{
        private static final long serialVersionUID = -4425614353529830020L;

        @SpecialUse
        public EventRegistry() {
        }

        @Override
        protected void writeValue(final ManagedResourceConfiguration.EventConfiguration value, final ObjectOutput out) throws IOException {
            out.writeObject(value);
        }

        @Override
        protected ManagedResourceConfiguration.EventConfiguration readValue(final ObjectInput out) throws IOException, ClassNotFoundException {
            return Utils.safeCast(out.readObject(), ManagedResourceConfiguration.EventConfiguration.class);
        }
    }

    private abstract static class AbstractEntityConfiguration extends Resettable implements SerializableEntityConfiguration {
        private static final long serialVersionUID = -8455277079119895844L;
        private transient boolean modified;
        private final ModifiableParameters parameters;

        protected AbstractEntityConfiguration(){
            parameters = new ModifiableParameters();
            modified = false;
        }

        protected final void writeParameters(final ObjectOutput out) throws IOException {
            parameters.writeExternal(out);
        }

        protected final void readParameters(final ObjectInput in) throws IOException, ClassNotFoundException {
            parameters.readExternal(in);
        }

        @Override
        final void reset() {
            modified = false;
            parameters.reset();
            resetAdditionally();
        }

        void resetAdditionally(){

        }

        protected final void markAsModified(){
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

        /**
         * Overwrites a set of parameters.
         * @param parameters A new set of parameters
         */
        public final void setParameters(final Map<String, String> parameters){
            this.parameters.clear();
            this.parameters.putAll(parameters);
        }

        /**
         * Puts the parameter value.
         * @param name The name of the parameter.
         * @param value The value of the parameter.
         */
        public final void setParameter(final String name, final String value){
            parameters.put(name, value);
        }
    }

    /**
     * Represents adapter settings. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class SerializableResourceAdapterConfiguration extends AbstractEntityConfiguration implements ResourceAdapterConfiguration{
        private static final byte FORMAT_VERSION = 1;
        private static final long serialVersionUID = 7926704115151740217L;
        private String adapterName;

        /**
         * Initializes a new empty adapter settings.
         */
        public SerializableResourceAdapterConfiguration(){
            adapterName = "";
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
            markAsModified();
            this.adapterName = adapterName != null ? adapterName : "";
        }

        public boolean equals(final ResourceAdapterConfiguration other){
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
         * @throws java.io.IOException Includes any I/O exceptions that may occur
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
         * @throws java.io.IOException    if I/O errors occur
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
     * @since 1.0
     * @version 1.0
     */
    public static final class SerializableManagedResourceConfiguration extends AbstractEntityConfiguration implements ManagedResourceConfiguration{
        private static final long serialVersionUID = 5044050385424748355L;

        private static abstract class AbstractFeatureConfiguration extends AbstractEntityConfiguration implements FeatureConfiguration {

            private static final long serialVersionUID = -1609210097027316240L;
        }

        /**
         * Represents configuration of the managed resource operation. This class cannot be inherited.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        public static final class SerializableOperationConfiguration extends AbstractFeatureConfiguration implements OperationConfiguration{
            private static final long serialVersionUID = 8267389949041604889L;
            private String operationName = "";

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
                out.writeUTF(operationName != null ? operationName : "");
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
                operationName = in.readUTF();
                readParameters(in);
            }

            /**
             * Gets name of the managed resource operation.
             *
             * @return The name of the managed resource operation.
             */
            @Override
            public String getOperationName() {
                return operationName;
            }

            /**
             * Sets name of the managed resource operation.
             *
             * @param operationName Name of the managed resource operation.
             */
            @Override
            public void setOperationName(final String operationName) {
                this.operationName = operationName != null ? operationName : "";
            }
        }

        /**
         * Represents configuration of the event source. This class cannot be inherited.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        public static final class SerializableEventConfiguration extends AbstractFeatureConfiguration implements EventConfiguration{
            private static final long serialVersionUID = -6838585011981639479L;
            private String eventCategory;

            /**
             * Initializes a event configuration with predefined category.
             * @param category The event category.
             */
            public SerializableEventConfiguration(final String category){
                this.eventCategory = category != null ? category : "";
            }

            /**
             * The object implements the writeExternal method to save its contents
             * by calling the methods of DataOutput for its primitive values or
             * calling the writeObject method of ObjectOutput for objects, strings,
             * and arrays.
             *
             * @param out the stream to write the object to
             * @throws java.io.IOException Includes any I/O exceptions that may occur
             * @serialData Overriding methods should use this tag to describe
             * the data layout of this Externalizable object.
             * List the sequence of element types and, if possible,
             * relate the element to a public/protected field and/or
             * method of this Externalizable class.
             */
            @Override
            public void writeExternal(final ObjectOutput out) throws IOException {
                out.writeUTF(eventCategory != null ? eventCategory : "");
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
             * @throws java.io.IOException    if I/O errors occur
             * @throws ClassNotFoundException If the class for an object being
             *                                restored cannot be found.
             */
            @Override
            public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
                eventCategory = in.readUTF();
                readParameters(in);
            }

            /**
             * Initializes a new empty configuration of the event.
             */
            public SerializableEventConfiguration(){
                this("");
            }

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
                markAsModified();
                this.eventCategory = eventCategory != null ? eventCategory : "";
            }

            @Override
            public final boolean equals(final Object obj) {
                return obj instanceof EventConfiguration && Objects.equals(((EventConfiguration) obj).getCategory(), getCategory());
            }

            /**
             * Computes hash code for this object.
             * @return The hash code of this object.
             */
            @Override
            public int hashCode() {
                return eventCategory.hashCode() ^ getParameters().hashCode();
            }
        }

        /**
         * Represents configuration of the management attribute. This class cannot be inherited.
         * @since 1.0
         * @version 1.0
         */
        public static final class SerializableAttributeConfiguration extends AbstractFeatureConfiguration implements AttributeConfiguration{
            private static final long serialVersionUID = -2134014000719123759L;
            private TimeSpan readWriteTimeout;
            private String attributeName;

            /**
             * Initializes a new configuration of the management attribute.
             */
            public SerializableAttributeConfiguration(){
                readWriteTimeout = TimeSpan.INFINITE;
                attributeName = "";
            }

            /**
             * The object implements the writeExternal method to save its contents
             * by calling the methods of DataOutput for its primitive values or
             * calling the writeObject method of ObjectOutput for objects, strings,
             * and arrays.
             *
             * @param out the stream to write the object to
             * @throws java.io.IOException Includes any I/O exceptions that may occur
             * @serialData Overriding methods should use this tag to describe
             * the data layout of this Externalizable object.
             * List the sequence of element types and, if possible,
             * relate the element to a public/protected field and/or
             * method of this Externalizable class.
             */
            @Override
            public void writeExternal(final ObjectOutput out) throws IOException {
                out.writeUTF(attributeName != null ? attributeName : "");
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
             * @throws java.io.IOException    if I/O errors occur
             * @throws ClassNotFoundException If the class for an object being
             *                                restored cannot be found.
             */
            @Override
            public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
                attributeName = in.readUTF();
                final long timeout = in.readLong();
                readWriteTimeout = timeout < 0L ? TimeSpan.INFINITE : new TimeSpan(timeout);
                readParameters(in);
            }

            /**
             * Initializes a new configuration of the management attribute.
             * @param attributeName The name of the management attribute.
             */
            public SerializableAttributeConfiguration(final String attributeName){
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
                markAsModified();
                this.readWriteTimeout = (timeout == TimeSpan.INFINITE || timeout.duration == Long.MAX_VALUE) ?
                        TimeSpan.INFINITE :
                        timeout;
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
                markAsModified();
                this.attributeName = attributeName != null ? attributeName : "";
            }
        }

        private static final byte FORMAT_VERSION = 1;
        private String connectionString;
        private final ConfigurationEntityRegistry<AttributeConfiguration> attributes;
        private String connectionType;
        private final ConfigurationEntityRegistry<EventConfiguration> events;
        private final ConfigurationEntityRegistry<OperationConfiguration> operations;

        /**
         * Initializes a new empty configuration of the management information source.
         */
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
         * @throws java.io.IOException Includes any I/O exceptions that may occur
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
         * @throws java.io.IOException    if I/O errors occur
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
        public void setOperations(final Map<String, ? extends OperationConfiguration> operations){
            this.operations.clear();
            this.operations.putAll(operations);
        }

        /**
         * Overwrites a set of attributes.
         * @param attributes A new set of attributes.
         */
        public void setAttributes(final Map<String, ? extends AttributeConfiguration> attributes) {
            this.attributes.clear();
            this.attributes.putAll(attributes);
        }

        /**
         * Overwrites a set of events.
         * @param events A new set of events.
         */
        public void setEvents(final Map<String, ? extends EventConfiguration> events){
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
            markAsModified();
            this.connectionType = connectionType != null ? connectorType : "";
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
        public <T extends FeatureConfiguration> SerializableMap<String, T> getElements(final Class<T> elementType) {
            if(elementType == null) return null;
            else if(Objects.equals(elementType, AttributeConfiguration.class))
                return (SerializableMap<String, T>)getAttributes();
            else if(Objects.equals(elementType, EventConfiguration.class))
                return (SerializableMap<String, T>)getEvents();
            else if(Objects.equals(elementType, OperationConfiguration.class))
                return (SerializableMap<String, T>)getOperations();
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
        public <T extends FeatureConfiguration> T newElement(final Class<T> elementType) {
            if(elementType == null) return null;
            else if(elementType.isAssignableFrom(SerializableAttributeConfiguration.class))
                return elementType.cast(newAttributeConfiguration());
            else if(elementType.isAssignableFrom(SerializableEventConfiguration.class))
                return elementType.cast(newEventConfiguration());
            else if(elementType.isAssignableFrom(SerializableOperationConfiguration.class))
                return elementType.cast(newOperationConfiguration());
            else return null;
        }

        /**
         * Returns a set of configured operations.
         * @return A set of configured operations.
         */
        public SerializableMap<String, OperationConfiguration> getOperations(){
            return operations;
        }

        /**
         * Returns the managed resource attributes (key is a attribute identifier).
         *
         * @return The dictionary of management managementAttributes.
         */
        public final SerializableMap<String, AttributeConfiguration> getAttributes() {
            return attributes;
        }

        /**
         * Returns the event sources.
         *
         * @return A set of event sources.
         */
        public final SerializableMap<String, EventConfiguration> getEvents() {
            return events;
        }

        /**
         * Creates empty instance of the {@link OperationConfiguration} interface.
         * @return Empty operation configuration.
         */
        public SerializableOperationConfiguration newOperationConfiguration(){
            return new SerializableOperationConfiguration();
        }

        /**
         * Empty implementation of AttributeConfiguration interface
         *
         * @return implementation of AttributeConfiguration interface
         */
        public final SerializableAttributeConfiguration newAttributeConfiguration() {
            return new SerializableAttributeConfiguration();
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
        public SerializableEventConfiguration newEventConfiguration() {
            return new SerializableEventConfiguration();
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

    private final ConfigurationEntityRegistry<ManagedResourceConfiguration> resources;
    private final ConfigurationEntityRegistry<ResourceAdapterConfiguration> adapters;

    /**
     * Initializes a new empty agent configuration.
     */
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

    void reset(){
        adapters.reset();
        resources.reset();
    }

    /**
     * Determines whether this configuration is modified.
     * @return {@literal true}, if some part of this configuration is modified; otherwise, {@literal false}.
     */
    public boolean isModified(){
        return adapters.isModified() || resources.isModified();
    }

    /**
     * Enumerates all modified configuration entities.
     * @param handler A handle which will be called for each modified entity.
     * @param <E> Type of the exception that may be produced by handler.
     * @throws E Unable to process modified configuration entity.
     */
    public <E extends Exception> void modifiedEntities(final RecordReader<String, ? super EntityConfiguration, E> handler) throws E {
        modifiedResources(handler);
        modifiedAdapters(handler);
    }

    public <E extends Exception> void modifiedResources(final RecordReader<String, ? super ManagedResourceConfiguration, E> handler) throws E{
        resources.modifiedResources(handler);
    }

    public <E extends Exception> void modifiedAdapters(final RecordReader<String, ? super ResourceAdapterConfiguration, E> handler) throws E{
        adapters.modifiedResources(handler);
    }

    /**
     * Represents management resources.
     *
     * @return The dictionary of management resources (management back-ends).
     */
    @Override
    public final SerializableMap<String, ManagedResourceConfiguration> getManagedResources() {
        return resources;
    }

    /**
     * Gets a collection of resource adapters.
     *
     * @return A collection of resource adapters.
     */
    @Override
    public final SerializableMap<String, ResourceAdapterConfiguration> getResourceAdapters() {
        return adapters;
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
        if(entityType == null) return null;
        else if(entityType.isAssignableFrom(SerializableManagedResourceConfiguration.class))
            return entityType.cast(new SerializableManagedResourceConfiguration());
        else if(entityType.isAssignableFrom(SerializableResourceAdapterConfiguration.class))
            return entityType.cast(new SerializableResourceAdapterConfiguration());
        else return null;
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws java.io.IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @Override
    public final void writeExternal(final ObjectOutput out) throws IOException {
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
     * @throws java.io.IOException    if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @Override
    public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
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
    public final boolean isEmpty(){
        return adapters.isEmpty() && resources.isEmpty();
    }
}
