package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.OperationConfiguration;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents configuration of the management information provider. This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.0
 */
final class SerializableManagedResourceConfiguration extends AbstractManagedResourceTemplate implements ManagedResourceConfiguration {
    private static final long serialVersionUID = 5044050385424748355L;

    private String connectionString;

    /**
     * Initializes a new empty configuration of the management information source.
     */
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableManagedResourceConfiguration(){
        connectionString = "";
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
        super.readExternal(in);
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
        connectionString = firstNonNull(value, "");
    }

    /**
     * Sets resource group for this resource.
     *
     * @param value The name of the resource group. Cannot be {@literal null}.
     */
    @Override
    public void setGroupName(final String value) {
        put(GROUP_NAME_PROPERTY, firstNonNull(value, ""));
    }

    /**
     * Gets name of resource group.
     *
     * @return Name of resource group; or empty string, if group is not assigned.
     */
    @Override
    public String getGroupName() {
        return firstNonNull(get(GROUP_NAME_PROPERTY), "");
    }

    private boolean equals(final ManagedResourceConfiguration other){
        return Objects.equals(getConnectionString(),  other.getConnectionString()) &&
                Objects.equals(getType(), other.getType()) &&
                getAttributes().equals(other.getFeatures(AttributeConfiguration.class)) &&
                getEvents().equals(other.getFeatures(EventConfiguration.class)) &&
                getOperations().equals(other.getFeatures(OperationConfiguration.class)) &&
                super.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ManagedResourceConfiguration &&
                equals((ManagedResourceConfiguration)other);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(connectionString, getType(), getAttributes(), getEvents(), getOperations());
    }
}
