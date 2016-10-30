package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents template of managed resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractManagedResourceTemplate extends AbstractEntityConfiguration {
    private static final long serialVersionUID = -9024738184822056816L;


    private static final class OperationList extends ConfigurationEntityList<SerializableOperationConfiguration> {
        private static final long serialVersionUID = -6621970441951257198L;

        @SpecialUse
        public OperationList(){

        }

        @Override
        protected SerializableOperationConfiguration createEntity() {
            final SerializableOperationConfiguration result = new SerializableOperationConfiguration();
            result.markAsModified();
            return result;
        }
    }

    private static final class AttributeList extends ConfigurationEntityList<SerializableAttributeConfiguration> {
        private static final long serialVersionUID = -9035924377259475433L;

        @SpecialUse
        public AttributeList() {
        }

        @Override
        protected SerializableAttributeConfiguration createEntity() {
            final SerializableAttributeConfiguration result = new SerializableAttributeConfiguration();
            result.markAsModified();
            return result;
        }
    }

    private static final class EventList extends ConfigurationEntityList<SerializableEventConfiguration> {
        private static final long serialVersionUID = -4425614353529830020L;

        @SpecialUse
        public EventList() {
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
    static final class SerializableOperationConfiguration extends AbstractFeatureConfiguration implements OperationConfiguration {
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

        @Override
        public int hashCode() {
            return timeout != null ? Objects.hash(timeout, getParameters()) : getParameters().hashCode();
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
    static final class SerializableAttributeConfiguration extends AbstractFeatureConfiguration implements AttributeConfiguration {
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

        @Override
        public int hashCode() {
            return readWriteTimeout != null ? Objects.hash(readWriteTimeout, getParameters()) : getParameters().hashCode();
        }
    }

    private final ConfigurationEntityList<SerializableAttributeConfiguration> attributes;
    private final ConfigurationEntityList<SerializableEventConfiguration> events;
    private final ConfigurationEntityList<SerializableOperationConfiguration> operations;
    private String connectionType;

    AbstractManagedResourceTemplate(){
        attributes = new AttributeList();
        events = new EventList();
        operations = new OperationList();
        connectionType = "";
    }

    public final String getType(){
        return connectionType;
    }

    public final void setType(final String value){
        connectionType = firstNonNull(value, "");
        markAsModified();
    }

    final ConfigurationEntityList<SerializableAttributeConfiguration> getAttributes(){
        return attributes;
    }

    final ConfigurationEntityList<SerializableEventConfiguration> getEvents(){
        return events;
    }

    final ConfigurationEntityList<SerializableOperationConfiguration> getOperations(){
        return operations;
    }

    @SuppressWarnings("unchecked")
    public final <T extends FeatureConfiguration> EntityMap<? extends T> getFeatures(final Class<T> featureType) {
        final ConfigurationEntityList result;
        if(featureType == null)
            result = null;
        else if(featureType.isAssignableFrom(SerializableAttributeConfiguration.class))
            result = attributes;
        else if(featureType.isAssignableFrom(SerializableEventConfiguration.class))
            result = events;
        else if(featureType.isAssignableFrom(SerializableOperationConfiguration.class))
            result = operations;
        else
            result = null;
        return result;
    }

    /**
     * Determines whether this configuration entity is modified after deserialization.
     *
     * @return {@literal true}, if this configuration entity is modified; otherwise, {@literal false}.
     */
    @Override
    public final boolean isModified() {
        return super.isModified() || attributes.isModified() || events.isModified() || operations.isModified();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(connectionType);
        attributes.writeExternal(out);
        events.writeExternal(out);
        operations.writeExternal(out);
        writeParameters(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        connectionType = in.readUTF();
        attributes.readExternal(in);
        events.readExternal(in);
        operations.readExternal(in);
        readParameters(in);
    }

    @Override
    public void reset() {
        attributes.reset();
        events.reset();
        operations.reset();
        super.reset();
    }

    /**
     * Overwrites a set of operations.
     * @param operations A new set of operations.
     */
    final void setOperations(final Map<String, ? extends SerializableOperationConfiguration> operations){
        this.operations.clear();
        this.operations.putAll(operations);
    }

    /**
     * Overwrites a set of attributes.
     * @param attributes A new set of attributes.
     */
    final void setAttributes(final Map<String, ? extends SerializableAttributeConfiguration> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
    }

    /**
     * Overwrites a set of events.
     * @param events A new set of events.
     */
    final void setEvents(final Map<String, ? extends SerializableEventConfiguration> events){
        this.events.clear();
        this.events.putAll(events);
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(final Object other);
}