package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents serializable version of managed resource group.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SerializableManagedResourceGroupConfiguration extends AbstractManagedResourceTemplate implements ManagedResourceGroupConfiguration {
    private static final long serialVersionUID = 9050126733283251808L;

    private String supervisor;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableManagedResourceGroupConfiguration(){
        supervisor = "";
    }

    /**
     * Gets supervisor name used for this group.
     *
     * @return Supervisor name.
     */
    @Override
    public String getSupervisor() {
        return supervisor;
    }

    /**
     * Sets supervisor name for this group.
     *
     * @param value Supervisor name.
     */
    @Override
    public void setSupervisor(final String value) {
        supervisor = nullToEmpty(value);
    }

    @Override
    public void merge(final ManagedResourceConfiguration resource) {
        //overwrite all properties in resource but hold user-defined properties
        resource.putAll(this);
        //overwrite all attributes
        resource.getAttributes().putAll(getAttributes());
        //overwrite all events
        resource.getEvents().putAll(getEvents());
        //overwrite all operations
        resource.getOperations().putAll(getOperations());
        //overwrite connector type
        resource.setType(getType());
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(supervisor);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        supervisor = in.readUTF();
        super.readExternal(in);
    }

    private void load(final ManagedResourceGroupConfiguration configuration){
        super.load(configuration);
        supervisor = nullToEmpty(configuration.getSupervisor());
    }

    @Override
    public void load(final Map<String, String> parameters) {
        if (parameters instanceof ManagedResourceGroupConfiguration)
            load((ManagedResourceGroupConfiguration) parameters);
        else
            super.load(parameters);
    }

    private boolean equals(final ManagedResourceGroupConfiguration other) {
        return other.getAttributes().equals(getAttributes()) &&
                other.getEvents().equals(getEvents()) &&
                other.getOperations().equals(getOperations()) &&
                Objects.equals(other.getType(), getType()) &&
                Objects.equals(supervisor, other.getSupervisor()) &&
                super.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ManagedResourceGroupConfiguration && equals((ManagedResourceGroupConfiguration)other);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(getType(), getAttributes(), getEvents(), getOperations(), supervisor);
    }
}
