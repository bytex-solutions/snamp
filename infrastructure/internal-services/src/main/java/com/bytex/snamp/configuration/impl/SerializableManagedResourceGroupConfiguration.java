package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents serializable version of managed resource group.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class SerializableManagedResourceGroupConfiguration extends AbstractManagedResourceTemplate implements ManagedResourceGroupConfiguration {
    private static final long serialVersionUID = 9050126733283251808L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableManagedResourceGroupConfiguration(){
    }

    private static <F extends FeatureConfiguration> void mergeFeatures(final EntityMap<? extends F> groupFeatures,
                                                                       final EntityMap<? extends F> resourceFeatures) {
        groupFeatures.forEach((featureNameInGroup, featureConfigInGroup) -> {
            final F featureConfigInResource = resourceFeatures.getOrAdd(featureNameInGroup);
            if (!featureConfigInResource.isOverridden())
                featureConfigInResource.load(featureConfigInGroup);
        });
    }

    private static void mergeParameters(final Map<String, String> groupParameters,
                                        final Map<String, String> resourceParameters,
                                        final Set<String> overriddenProperties) {
        groupParameters.forEach((paramNameInGroup, paramValueInGroup) -> {
            if (!overriddenProperties.contains(paramNameInGroup))
                resourceParameters.put(paramNameInGroup, paramValueInGroup);
        });
    }

    @Override
    public void fillResourceConfig(final ManagedResourceConfiguration resource) {
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
