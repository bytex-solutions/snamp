package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Represents configuration of the management information provider. This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.0
 */
final class SerializableManagedResourceConfiguration extends AbstractEntityConfiguration implements ManagedResourceConfiguration {
    private static final long serialVersionUID = 5044050385424748355L;

    private static final class OperationRegistry extends ConfigurationEntityRegistry<SerializableOperationConfiguration>{
        private static final long serialVersionUID = -6621970441951257198L;

        @SpecialUse
        public OperationRegistry(){

        }

        @Override
        protected SerializableOperationConfiguration createEntity() {
            final SerializableOperationConfiguration result = new SerializableOperationConfiguration();
            result.markAsModified();
            return result;
        }
    }

    private static final class AttributeRegistry extends ConfigurationEntityRegistry<SerializableAttributeConfiguration>{
        private static final long serialVersionUID = -9035924377259475433L;

        @SpecialUse
        public AttributeRegistry() {
        }

        @Override
        protected SerializableAttributeConfiguration createEntity() {
            final SerializableAttributeConfiguration result = new SerializableAttributeConfiguration();
            result.markAsModified();
            return result;
        }
    }

    private static final class EventRegistry extends ConfigurationEntityRegistry<SerializableEventConfiguration>{
        private static final long serialVersionUID = -4425614353529830020L;

        @SpecialUse
        public EventRegistry() {
        }

        @Override
        protected SerializableEventConfiguration createEntity() {
            final SerializableEventConfiguration result = new SerializableEventConfiguration();
            result.markAsModified();
            return result;
        }
    }

    private static abstract class AbstractFeatureConfiguration extends AbstractEntityConfiguration implements FeatureConfiguration {
        private static final long serialVersionUID = -1609210097027316240L;
    }

    /**
     * Represents configuration of the managed resource operation. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.2
     * @version 2.0
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
            out.writeObject(timeout);
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
            timeout = (Duration) in.readObject();
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
            return getParameters().equals(other.getParameters()) &&
                    Objects.equals(timeout, other.getInvocationTimeout());
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
     * @version 2.0
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
            return getParameters().equals(other.getParameters());
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
     * @version 2.0
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
            out.writeObject(readWriteTimeout);
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
            readWriteTimeout = (Duration)in.readObject();
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
            return Objects.equals(readWriteTimeout, other.getReadWriteTimeout()) &&
                    getParameters().equals(other.getParameters());
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
    public String getType() {
        return connectionType;
    }

    /**
     * Sets the management connector that is used to organize monitoring data exchange between
     * agent and the management provider.
     *
     * @param connectorType The management connector type.
     */
    @Override
    public void setType(final String connectorType) {
        markAsModified();
        this.connectionType = connectionType != null ? connectorType : "";
    }

    /**
     * Gets a collection of configured manageable elements for this target.
     *
     * @param featureType The type of the manageable element.
     * @return A map of manageable elements; or {@literal null}, if element type is not supported.
     * @see AttributeConfiguration
     * @see EventConfiguration
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

    private boolean equals(final ManagedResourceConfiguration other){
        return Objects.equals(getConnectionString(),  other.getConnectionString()) &&
                Objects.equals(getType(), other.getType()) &&
                attributes.equals(other.getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class)) &&
                events.equals(other.getFeatures(ManagedResourceConfiguration.EventConfiguration.class)) &&
                operations.equals(other.getFeatures(ManagedResourceConfiguration.OperationConfiguration.class)) &&
                getParameters().equals(other.getParameters());
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ManagedResourceConfiguration &&
                equals((ManagedResourceConfiguration)other);
    }

    @Override
    public int hashCode() {
        return connectionString.hashCode() ^
                connectionType.hashCode() ^
                attributes.hashCode() ^
                events.hashCode();
    }
}
