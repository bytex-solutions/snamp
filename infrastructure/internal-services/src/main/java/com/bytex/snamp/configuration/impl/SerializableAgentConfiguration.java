package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import static com.bytex.snamp.configuration.impl.AbstractManagedResourceTemplate.*;

/**
 * Represents in-memory agent configuration that can be stored as serialized Java object.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.0
 */
public final class SerializableAgentConfiguration extends AbstractEntityConfiguration implements Externalizable, Modifiable, Stateful, AgentConfiguration {
    private static final long serialVersionUID = 8461144056430141155L;

    private final ConfigurationEntityList<SerializableGatewayConfiguration> gateways;
    private final ConfigurationEntityList<SerializableThreadPoolConfiguration> threadPools;
    private final ConfigurationEntityList<SerializableManagedResourceGroupConfiguration> groups;
    private final ConfigurationEntityList<SerializableManagedResourceConfiguration> resources;

    /**
     * Initializes a new empty agent configuration.
     */
    @SpecialUse
    public SerializableAgentConfiguration(){
        gateways = new GatewayList();
        threadPools = new ThreadPoolList();
        groups = new ResourceGroupList();
        resources = new ManagedResourceList();
    }

    @Override
    public SerializableAgentConfiguration asReadOnly() {
        return this;    //TODO: replace with really immutable configuration similar to ImmutableEntityConfiguration
    }

    /**
     * Clears this configuration.
     */
    @Override
    public void clear() {
        super.clear();
        gateways.clear();
        threadPools.clear();
        groups.clear();
        resources.clear();
    }

    /**
     * Clones this instance of agent configuration.
     *
     * @return A new cloned instance of the {@link SerializableAgentConfiguration}.
     */
    @Override
    public SerializableAgentConfiguration clone() {
        final SerializableAgentConfiguration clonedConfig = new SerializableAgentConfiguration();
        clonedConfig.load(this);
        clonedConfig.reset();
        return clonedConfig;
    }

    @Override
    public boolean isModified() {
        return gateways.isModified() || threadPools.isModified() || groups.isModified() || resources.isModified() || super.isModified();
    }

    @Override
    public void reset() {
        super.reset();
        gateways.reset();
        threadPools.reset();
        groups.reset();
        resources.reset();
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
        super.writeExternal(out);
        //write gateway
        gateways.writeExternal(out);
        //write connectors and groups
        resources.writeExternal(out);
        groups.writeExternal(out);
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
        super.readExternal(in);
        //read gateway
        gateways.readExternal(in);
        //read connectors and groups
        resources.readExternal(in);
        groups.readExternal(in);
        //read thread pools
        threadPools.readExternal(in);
    }

    /**
     * Determines whether this configuration is empty.
     * @return {@literal true}, if this configuration is empty; otherwise, {@literal false}.
     */
    boolean hasNoInnerItems(){
        return gateways.isEmpty() && resources.isEmpty() && groups.isEmpty() && threadPools.isEmpty() && super.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends EntityConfiguration> ConfigurationEntityList<? extends E> getEntities(final Class<E> entityType) {
        final ConfigurationEntityList result;
        if (entityType.isAssignableFrom(SerializableManagedResourceConfiguration.class))
            result = resources;
        else if (entityType.isAssignableFrom(SerializableGatewayConfiguration.class))
            result = gateways;
        else if(entityType.isAssignableFrom(SerializableThreadPoolConfiguration.class))
            result = threadPools;
        else if(entityType.isAssignableFrom(SerializableManagedResourceGroupConfiguration.class))
            result = groups;
        else
            result = null;
        return result;
    }

    ConfigurationEntityList<SerializableManagedResourceGroupConfiguration> getManagedResourceGroups(){
        return groups;
    }

    ConfigurationEntityList<SerializableThreadPoolConfiguration> getThreadPools(){
        return threadPools;
    }

    ConfigurationEntityList<SerializableGatewayConfiguration> getGatewayInstances() {
        return gateways;
    }


    ConfigurationEntityList<SerializableManagedResourceConfiguration> getManagedResources() {
        return resources;
    }

    /**
     * Creates a new instance of entity configuration.
     *
     * @param entityType Type of entity. Can be {@link ManagedResourceConfiguration},
     *                   {@link GatewayConfiguration}. {@link AttributeConfiguration}, {@link EventConfiguration}, {@link OperationConfiguration}.
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
     *                   {@link GatewayConfiguration}. {@link AttributeConfiguration}, {@link EventConfiguration}, {@link OperationConfiguration}.
     * @return A new instance of entity configuration; or {@literal null}, if entity is not supported.
     */
    public static <E extends EntityConfiguration> E newEntityConfiguration(final Class<E> entityType) {
        final EntityConfiguration result;
        if (entityType.isAssignableFrom(SerializableManagedResourceConfiguration.class))
            result = new SerializableManagedResourceConfiguration();
        else if (entityType.isAssignableFrom(SerializableGatewayConfiguration.class))
            result = new SerializableGatewayConfiguration();
        else if (entityType.isAssignableFrom(SerializableAttributeConfiguration.class))
            result = new SerializableAttributeConfiguration();
        else if (entityType.isAssignableFrom(SerializableEventConfiguration.class))
            result = new SerializableEventConfiguration();
        else if (entityType.isAssignableFrom(SerializableOperationConfiguration.class))
            result = new SerializableOperationConfiguration();
        else if (entityType.isAssignableFrom(SerializableThreadPoolConfiguration.class))
            result = new SerializableThreadPoolConfiguration();
        else if (entityType.isAssignableFrom(SerializableManagedResourceGroupConfiguration.class))
            result = new SerializableManagedResourceGroupConfiguration();
        else
            return null;
        return entityType.cast(result);
    }

    private boolean equals(final AgentConfiguration other) {
        return gateways.equals(other.getEntities(GatewayConfiguration.class)) &&
                resources.equals(other.getEntities(ManagedResourceConfiguration.class)) &&
                threadPools.equals(other.getEntities(ThreadPoolConfiguration.class)) &&
                groups.equals(other.getEntities(ManagedResourceGroupConfiguration.class)) &&
                super.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof AgentConfiguration && equals((AgentConfiguration)other);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(gateways, groups, resources, threadPools);
    }
}
