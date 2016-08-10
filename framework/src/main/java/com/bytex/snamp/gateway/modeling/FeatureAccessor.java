package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.Internal;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.FeatureModifiedEvent;

import javax.management.Descriptor;
import javax.management.DescriptorRead;
import javax.management.MBeanFeatureInfo;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents an abstract class for all managed resource feature accessor.
 * This class cannot be derived directly from your code.
 * @param <M> The type of the managed resource feature.
 */
public abstract class FeatureAccessor<M extends MBeanFeatureInfo> implements Supplier<M>, DescriptorRead, SafeCloseable {
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
     * Returns a copy of Descriptor.
     *
     * @return Descriptor associated with the component implementing this interface.
     * The return value is never null, but the returned descriptor may be empty.
     */
    @Override
    public final Descriptor getDescriptor() {
        return metadata.getDescriptor();
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
     * Disconnects the feature accessor from the managed resource.
     */
    @Override
    public abstract void close();

    /**
     * Determines whether the feature of the managed resource is accessible
     * through this object.
     * @return {@literal true}, if this feature is accessible; otherwise, {@literal false}.
     */
    public abstract boolean isConnected();

    @Override
    public String toString() {
        return metadata.toString();
    }

    private static int removeAll(final Iterator<? extends FeatureAccessor<?>> features,
                                 final MBeanFeatureInfo metadata){
        int result = 0;
        while (features.hasNext())
            if(metadata.equals(features.next().getMetadata())) {
                features.remove();
                result += 1;
            }
        return result;
    }

    private static <F extends FeatureAccessor<?>> F remove(final Iterator<? extends F> features,
                                 final MBeanFeatureInfo metadata){
        while (features.hasNext()) {
            final F feature = features.next();
            if (metadata.equals(feature.getMetadata())) {
                features.remove();
                return feature;
            }
        }
        return null;
    }

    /**
     * Removes the accessor from the specified collection.
     * @param features A collection of features to be modified.
     * @param metadata The metadata of the feature that should be removed from the collection. Cannot be {@literal null}.
     * @return A number of removed features.
     */
    protected static <M extends MBeanFeatureInfo> int removeAll(final Iterable<? extends FeatureAccessor<M>> features,
                                                                final M metadata){
        return removeAll(features.iterator(), metadata);
    }

    protected static <M extends MBeanFeatureInfo, F extends FeatureAccessor<M>> F remove(final Iterable<? extends F> features,
                                                                                            final M metadata){
        return remove(features.iterator(), metadata);
    }

    /**
     * Processes an event related to this accessor.
     * @param event An event related to this accessor.
     * @return {@literal true}, if event processed successfully; otherwise, {@literal false}.
     */
    @Internal
    public abstract boolean processEvent(final FeatureModifiedEvent<M> event);
}
