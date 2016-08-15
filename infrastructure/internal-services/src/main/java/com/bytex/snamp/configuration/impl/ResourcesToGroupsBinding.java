package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Represents binding between managed resources and their groups.
 * <p>
 *     When group added then all existing managed resources must be upgraded according with their group names
 *     When group removed then all existing managed resources must be upgraded according with their group names
 *     When group modified then all existing managed resources must be upgraded according with their group names
 *     When group of managed resource updated then properties of this managed resource should be rewritten
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ResourcesToGroupsBinding implements Externalizable, ResourceGroupChangedEventListener, Resettable {
    private static final long serialVersionUID = 1660158740910479875L;
    private final ResourceGroupList groups;
    private final ManagedResourceList resources;

    @SpecialUse
    public ResourcesToGroupsBinding(){
        groups = new ResourceGroupList();
        resources = new ManagedResourceList();
    }

    ConfigurationEntityList<SerializableManagedResourceConfiguration> getManagedResources(){
        return resources;
    }

    ConfigurationEntityList<SerializableManagedResourceGroupConfiguration> getResourceGroups(){
        return groups;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        groups.writeExternal(out);
        resources.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        groups.readExternal(in);
        resources.readExternal(in);
    }

    @Override
    public void reset() {
        groups.reset();
        resources.reset();
    }

    boolean isEmpty(){
        return groups.isEmpty() && resources.isEmpty();
    }

    @Override
    public void groupNameChanged(final ResourceGroupChangedEvent event) {
        //anonymous function that is used  to remove all group parameters from managed resource
        final BiConsumer<ManagedResourceGroupConfiguration, ManagedResourceConfiguration> removeGroupParams = (oldGroup, resource) ->
                oldGroup.getParameters().keySet().forEach(resource.getParameters()::remove);

        final ManagedResourceGroupConfiguration oldGroup = groups.get(event.getOldGroupName());
        final ManagedResourceGroupConfiguration newGroup = groups.get(event.getSource().getGroupName());
        /*
         * Algorithm:
         * 1. oldGroup = null, newGroup = null => nothing to do
         * 2. oldGroup != null, newGroup = null => remove all properties associated with old group
         * 3. oldGroup != null, newGroup != null => remove all properties associated with old group and merge with properties from new group
         * 4. oldGroup = null, newGroup != null => merge with properties from new group
         */
        if (oldGroup != null && newGroup == null) { //2.
            removeGroupParams.accept(oldGroup, event.getSource());
        } else if (newGroup != null) {
            if (oldGroup != null) //3.
                removeGroupParams.accept(oldGroup, event.getSource());
            event.getSource().setParameters(newGroup.getParameters()); //4.
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(groups, resources);
    }

    private boolean equals(final ResourcesToGroupsBinding other) {
        return groups.equals(other.groups) && resources.equals(other.resources);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ResourcesToGroupsBinding && equals((ResourcesToGroupsBinding)other);
    }
}
