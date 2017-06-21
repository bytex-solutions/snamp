package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents configuration of the management information provider. This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.0
 */
final class SerializableManagedResourceConfiguration extends AbstractManagedResourceTemplate implements ManagedResourceConfiguration {
    private static final long serialVersionUID = 5044050385424748355L;

    private String connectionString;
    private final ModifiableStringSet overriddenProperties;

    /**
     * Initializes a new empty configuration of the management information source.
     */
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableManagedResourceConfiguration(){
        connectionString = "";
        overriddenProperties = new ModifiableStringSet();
    }

    @Override
    public ModifiableHashSet<String> getOverriddenProperties() {
        return overriddenProperties;
    }

    @Override
    public boolean isModified() {
        return super.isModified() || overriddenProperties.isModified();
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
        out.writeUTF(connectionString);
        //save overridden properties
        overriddenProperties.writeExternal(out);
        //save entire map
        super.writeExternal(out);
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
        connectionString = in.readUTF();
        //restore overridden properties
        overriddenProperties.readExternal(in);
        //restore entire map
        super.readExternal(in);
    }

    private void load(final ManagedResourceConfiguration configuration){
        connectionString = configuration.getConnectionString();
        overriddenProperties.clear();
        overriddenProperties.addAll(configuration.getOverriddenProperties());
        super.load(configuration);
    }

    @Override
    public void load(final Map<String, String> parameters) {
        if (parameters instanceof ManagedResourceConfiguration)
            load((ManagedResourceConfiguration) parameters);
        else
            super.load(parameters);
    }

    @Override
    public void overrideProperties(final Collection<String> properties) {
        overriddenProperties.retainAll(properties);
        overriddenProperties.addAll(properties);
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
     * @param value The connection string that is used to connect to the management server.
     */
    @Override
    public void setConnectionString(final String value) {
        markAsModified();
        connectionString = nullToEmpty(value);
    }

    private boolean equals(final ManagedResourceConfiguration other){
        return Objects.equals(getConnectionString(),  other.getConnectionString()) &&
                Objects.equals(getType(), other.getType()) &&
                other.getAttributes().equals(getAttributes()) &&
                other.getEvents().equals(getEvents()) &&
                other.getOperations().equals(getOperations()) &&
                other.getOverriddenProperties().equals(getOverriddenProperties()) &&
                super.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ManagedResourceConfiguration &&
                equals((ManagedResourceConfiguration)other);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(connectionString, getType(), getAttributes(), getEvents(), getOperations(), overriddenProperties);
    }
}
