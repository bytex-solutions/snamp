package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents serializable version of managed resource group.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SerializableManagedResourceGroupConfiguration extends AbstractManagedResourceTemplate implements ManagedResourceGroupConfiguration {
    private static final long serialVersionUID = 9050126733283251808L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableManagedResourceGroupConfiguration(){
    }

    private static <F extends AbstractFeatureConfiguration> F mergeFeature(final F featureConfigInResource,
                                                                           final F featureConfigInGroup){
        return featureConfigInResource.isOverridden() ? featureConfigInResource : featureConfigInGroup;
    }

    private static <F extends AbstractFeatureConfiguration> void mergeFeatures(final SerializableEntityMap<F> groupFeatures,
                                                                       final SerializableEntityMap<F> resourceFeatures) {
        groupFeatures.forEach((featureNameInGroup, featureConfigInGroup) -> resourceFeatures.merge(featureNameInGroup, featureConfigInGroup, SerializableManagedResourceGroupConfiguration::mergeFeature));
    }

    private static void mergeParameters(final Map<String, String> groupParameters,
                                        final Map<String, String> resourceParameters,
                                        final Set<String> overriddenProperties) {
        groupParameters.forEach((paramNameInGroup, paramValueInGroup) -> {
            if (!overriddenProperties.contains(paramNameInGroup))
                resourceParameters.put(paramNameInGroup, paramValueInGroup);
        });
    }

    void fillResourceConfig(final SerializableManagedResourceConfiguration resource) {
        //overwrite all properties in resource but hold user-defined properties
        mergeParameters(this, resource, resource.getOverriddenProperties());
        //overwrite all attributes
        mergeFeatures(getAttributes(), resource.getAttributes());
        //overwrite all events
        mergeFeatures(getEvents(), resource.getEvents());
        //overwrite all operations
        mergeFeatures(getOperations(), resource.getOperations());
        //overwrite connector type
        resource.setType(getType());
    }

    private boolean equals(final ManagedResourceGroupConfiguration other) {
        return other.getAttributes().equals(getAttributes()) &&
                other.getEvents().equals(getEvents()) &&
                other.getOperations().equals(getOperations()) &&
                Objects.equals(other.getType(), getType()) &&
                super.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ManagedResourceGroupConfiguration && equals((ManagedResourceGroupConfiguration)other);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(getType(), getAttributes(), getEvents(), getOperations());
    }
}
