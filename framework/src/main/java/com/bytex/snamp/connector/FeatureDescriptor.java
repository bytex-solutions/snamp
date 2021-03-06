package com.bytex.snamp.connector;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.jmx.CopyOnWriteDescriptor;
import com.bytex.snamp.jmx.DescriptorUtils;

import javax.annotation.Nonnull;
import javax.management.Descriptor;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents configuration entity descriptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface FeatureDescriptor<F extends FeatureConfiguration> extends CopyOnWriteDescriptor {

    /**
     * The type of the configuration entity.
     * @return The type of the configuration entity.
     */
    Class<F> getEntityType();

    /**
     * Fills the specified configuration entity.
     * @param entity The configuration entity to fill.
     */
    void fill(final F entity);

    /**
     * Returns cloned descriptor with modified fields.
     *
     * @param values A fields to put into the new descriptor.
     * @return A new descriptor with modified fields.
     */
    @Override
    FeatureDescriptor<F> setFields(final Map<String, ?> values);

    /**
     * Returns cloned descriptor with modified fields.
     *
     * @param values A fields to put into the new descriptor.
     * @return A new descriptor with modified fields.
     */
    @Override
    FeatureDescriptor<F> setFields(final Descriptor values);

    /**
     * Gets alternative name of the feature.
     * @return Alternative name of the feature.
     * @see FeatureConfiguration#NAME_KEY
     */
    default Optional<String> getAlternativeName(){
        return getName(this);
    }

    static Optional<String> getName(@Nonnull final Descriptor descriptor) {
        return DescriptorUtils.getField(descriptor, AttributeConfiguration.NAME_KEY, Objects::toString);
    }

    /**
     * Determines whether this descriptor is automatically generated.
     * @return {@literal true}, if this descriptor is automatically generated; otherwise, {@literal false}.
     * @see FeatureConfiguration#AUTOMATICALLY_ADDED_KEY
     */
    default boolean isAutomaticallyAdded() {
        return DescriptorUtils.getField(this, FeatureConfiguration.AUTOMATICALLY_ADDED_KEY, Objects::toString)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }
}
