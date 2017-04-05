package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.*;

import javax.annotation.Nonnull;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
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

    private final SerializableEntityMap<SerializableGatewayConfiguration> gateways;
    private final SerializableEntityMap<SerializableThreadPoolConfiguration> threadPools;
    private final SerializableEntityMap<SerializableManagedResourceGroupConfiguration> groups;
    private final SerializableEntityMap<SerializableManagedResourceConfiguration> resources;
    private final SerializableEntityMap<SerializableSupervisorConfiguration> supervisors;

    /**
     * Initializes a new empty agent configuration.
     */
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableAgentConfiguration(){
        gateways = new GatewayMap();
        threadPools = new ThreadPoolMap();
        groups = new ResourceGroupMap();
        resources = new ManagedResourceMap();
        supervisors = new SupervisorMap();
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
        supervisors.clear();
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

    private void loadParameters(final Map<String, String> parameters){
        super.clear();
        putAll(parameters);
    }

    private void load(final AgentConfiguration configuration) {
        resources.load(configuration.getResources());
        threadPools.load(configuration.getThreadPools());
        gateways.load(configuration.getGateways());
        groups.load(configuration.getResourceGroups());
        supervisors.load(configuration.getSupervisors());
        loadParameters(configuration);
    }

    @Override
    public void load(final Map<String, String> parameters) {
        if(parameters instanceof AgentConfiguration)
            load((AgentConfiguration) parameters);
        else
            loadParameters(parameters);
    }

    @Override
    public boolean isModified() {
        return gateways.isModified() ||
                threadPools.isModified() ||
                groups.isModified() ||
                resources.isModified() ||
                supervisors.isModified() ||
                super.isModified();
    }

    @Override
    public void reset() {
        super.reset();
        gateways.reset();
        threadPools.reset();
        groups.reset();
        resources.reset();
        supervisors.reset();
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
        //write watchers
        supervisors.writeExternal(out);
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
        //read watchers
        supervisors.readExternal(in);
    }

    /**
     * Determines whether this configuration is empty.
     * @return {@literal true}, if this configuration is empty; otherwise, {@literal false}.
     */
    boolean hasNoInnerItems(){
        return gateways.isEmpty() &&
                resources.isEmpty() &&
                groups.isEmpty() &&
                threadPools.isEmpty() &&
                supervisors.isEmpty() &&
                super.isEmpty();
    }

    @Override
    @Nonnull
    public SerializableEntityMap<SerializableSupervisorConfiguration> getSupervisors(){
        return supervisors;
    }

    @Override
    @Nonnull
    public SerializableEntityMap<SerializableManagedResourceGroupConfiguration> getResourceGroups(){
        return groups;
    }

    @Override
    @Nonnull
    public SerializableEntityMap<SerializableThreadPoolConfiguration> getThreadPools(){
        return threadPools;
    }

    @Override
    @Nonnull
    public SerializableEntityMap<SerializableGatewayConfiguration> getGateways() {
        return gateways;
    }

    @Override
    @Nonnull
    public SerializableEntityMap<SerializableManagedResourceConfiguration> getResources() {
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
        else if (entityType.isAssignableFrom(SerializableSupervisorConfiguration.class))
            result = new SerializableSupervisorConfiguration();
        else
            return null;
        return entityType.cast(result);
    }

    private boolean equals(final AgentConfiguration other) {
        return other.getGateways().equals(gateways) &&
                other.getResources().equals(resources) &&
                other.getThreadPools().equals(threadPools) &&
                other.getResourceGroups().equals(groups) &&
                other.getSupervisors().equals(supervisors) &&
                super.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof AgentConfiguration && equals((AgentConfiguration)other);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(gateways, groups, resources, threadPools, supervisors);
    }
}
