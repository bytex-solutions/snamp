package com.itworks.snamp.adapters;

import com.google.common.base.Supplier;

import javax.management.MBeanFeatureInfo;
import java.util.Objects;

/**
 * Represents an abstract class for all managed resource feature accessor.
 * This class cannot be derived directly from your code.
 * @param <M> The type of the managed resource feature.
 * @param <S> The type of the feature supporter.
 */
public abstract class FeatureAccessor<M extends MBeanFeatureInfo, S> implements Supplier<M> {
    private final M metadata;

    FeatureAccessor(final M metadata){
        this.metadata = Objects.requireNonNull(metadata);
    }

    /**
     * Gets metadata of the feature associated with this accessor.
     * @return The metadata of the feature associated with this accessor.
     */
    public final M getMetadata(){
        return metadata;
    }

    /**
     * Gets metadata of the feature associated with this accessor.
     * @return The metadata of the feature associated with this accessor.
     */
    @Override
    public final M get() {
        return getMetadata();
    }

    /**
     * Connector feature accessor to the managed resource.
     * @param value The managed resource accessor.
     */
    abstract void connect(final S value);

    /**
     * Disconnects the feature accessor from the managed resource.
     */
    public abstract void disconnect();

    /**
     * Determines whether the feature of the managed resource is accessible
     * through this object.
     * @return {@literal true}, if this feature is accessible; otherwise, {@literal false}.
     */
    public abstract boolean isConnected();

    @Override
    public String toString() {
        return getMetadata().toString();
    }
}
