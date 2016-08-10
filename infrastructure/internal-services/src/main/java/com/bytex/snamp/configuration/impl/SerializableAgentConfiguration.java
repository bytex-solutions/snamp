package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents in-memory agent configuration that can be stored as serialized Java object.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.0
 */
public final class SerializableAgentConfiguration extends AbstractAgentConfiguration implements Externalizable {
    private final static byte FORMAT_VERSION = 1;
    private static final long serialVersionUID = 8461144056430141155L;


    private final ConfigurationEntityRegistry<SerializableManagedResourceConfiguration> resources;
    private final ConfigurationEntityRegistry<SerializableGatewayConfiguration> adapters;
    private final ConfigurationEntityRegistry<SerializableThreadPoolConfiguration> threadPools;

    /**
     * Initializes a new empty agent configuration.
     */
    @SpecialUse
    public SerializableAgentConfiguration(){
        adapters = new AdapterRegistry();
        resources = new ResourceRegistry();
        threadPools = new ThreadPoolRegistry();
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
        threadPools.reset();
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
        //write gateway
        adapters.writeExternal(out);
        //write connector
        resources.writeExternal(out);
        //write thread pools
        threadPools.writeExternal(out);
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
        //read gateway
        adapters.readExternal(in);
        //read connector
        resources.readExternal(in);
        //read thread pools
        threadPools.readExternal(in);
    }

    /**
     * Determines whether this configuration is empty.
     * @return {@literal true}, if this configuration is empty; otherwise, {@literal false}.
     */
    boolean isEmpty(){
        return adapters.isEmpty() && resources.isEmpty() && threadPools.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends EntityConfiguration> ConfigurationEntityRegistry<? extends E> getEntities(final Class<E> entityType) {
        final ConfigurationEntityRegistry result;
        if (entityType.isAssignableFrom(SerializableManagedResourceConfiguration.class))
            result = resources;
        else if (entityType.isAssignableFrom(SerializableGatewayConfiguration.class))
            result = adapters;
        else if(entityType.isAssignableFrom(SerializableThreadPoolConfiguration.class))
            result = threadPools;
        else
            result = null;
        return result;
    }

    ConfigurationEntityRegistry<SerializableThreadPoolConfiguration> getThreadPools(){
        return threadPools;
    }

    ConfigurationEntityRegistry<SerializableGatewayConfiguration> getResourceAdapters() {
        return adapters;
    }


    ConfigurationEntityRegistry<SerializableManagedResourceConfiguration> getManagedResources() {
        return resources;
    }

    /**
     * Creates a new instance of entity configuration.
     *
     * @param entityType Type of entity. Can be {@link ManagedResourceConfiguration},
     *                   {@link GatewayConfiguration}. {@link ManagedResourceConfiguration.AttributeConfiguration}, {@link ManagedResourceConfiguration.EventConfiguration}, {@link ManagedResourceConfiguration.OperationConfiguration}.
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
     *                   {@link GatewayConfiguration}. {@link ManagedResourceConfiguration.AttributeConfiguration}, {@link ManagedResourceConfiguration.EventConfiguration}, {@link ManagedResourceConfiguration.OperationConfiguration}.
     * @return A new instance of entity configuration; or {@literal null}, if entity is not supported.
     */
    public static  <E extends EntityConfiguration> E newEntityConfiguration(final Class<E> entityType) {
        if (entityType.isAssignableFrom(SerializableManagedResourceConfiguration.class))
            return entityType.cast(new SerializableManagedResourceConfiguration());
        else if (entityType.isAssignableFrom(SerializableGatewayConfiguration.class))
            return entityType.cast(new SerializableGatewayConfiguration());
        else if (entityType.isAssignableFrom(SerializableManagedResourceConfiguration.SerializableAttributeConfiguration.class))
            return entityType.cast(new SerializableManagedResourceConfiguration.SerializableAttributeConfiguration());
        else if (entityType.isAssignableFrom(SerializableManagedResourceConfiguration.SerializableEventConfiguration.class))
            return entityType.cast(new SerializableManagedResourceConfiguration.SerializableEventConfiguration());
        else if (entityType.isAssignableFrom(SerializableManagedResourceConfiguration.SerializableOperationConfiguration.class))
            return entityType.cast(new SerializableManagedResourceConfiguration.SerializableOperationConfiguration());
        else if (entityType.isAssignableFrom(SerializableThreadPoolConfiguration.class))
            return entityType.cast(new SerializableThreadPoolConfiguration());
        else return null;
    }

    private boolean equals(final AgentConfiguration other) {
        return adapters.equals(other.getEntities(GatewayConfiguration.class)) &&
                resources.equals(other.getEntities(ManagedResourceConfiguration.class)) &&
                threadPools.equals(other.getEntities(ThreadPoolConfiguration.class));
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof AgentConfiguration && equals((AgentConfiguration)other);
    }

    @Override
    public int hashCode() {
        return adapters.hashCode() ^ (resources.hashCode() << 1) ^ (threadPools.hashCode() << 2);
    }
}
