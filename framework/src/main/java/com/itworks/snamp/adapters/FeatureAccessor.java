package com.itworks.snamp.adapters;

import javax.management.MBeanFeatureInfo;
import java.util.Objects;

/**
 * Represents an abstract class for all managed resource feature accessor.
 * This class cannot be derived directly from your code.
 * @param <M> The type of the managed resource feature.
 * @param <S> The type of the feature supporter.
 */
public abstract class FeatureAccessor<M extends MBeanFeatureInfo, S> {
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
     * Connector feature accessor to the managed resource.
     * @param value The managed resource accessor.
     */
    abstract void connect(final S value);

    /**
     * Disconnects the feature accessor from the managed resource.
     */
    public abstract void disconnect();

    @Override
    public String toString() {
        return getMetadata().toString();
    }
}
