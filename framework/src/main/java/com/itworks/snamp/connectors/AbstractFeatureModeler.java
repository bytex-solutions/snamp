package com.itworks.snamp.connectors;

import com.google.common.base.Supplier;
import com.google.common.collect.ObjectArrays;
import com.itworks.snamp.concurrent.ThreadSafeObject;

import javax.management.MBeanFeatureInfo;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Represents an abstract class for all modelers of managed resource features.
 * You cannot derive from this class directly.
 * @param <F> Type of the modeling feature.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractFeatureModeler<F extends MBeanFeatureInfo> extends ThreadSafeObject {
    private static final class WeakResourceEventListener extends WeakReference<ResourceEventListener> implements Supplier<ResourceEventListener> {

        private WeakResourceEventListener(final ResourceEventListener listener) {
            super(Objects.requireNonNull(listener));
        }
    }

    private static final class ResourceEventListenerList extends LinkedList<WeakResourceEventListener> {
        private static final long serialVersionUID = -9139754747382955308L;

        private ResourceEventListenerList(){

        }

        public boolean add(final ResourceEventListener listener) {
            //remove dead references
            final Iterator<WeakResourceEventListener> listeners = iterator();
            while (listeners.hasNext()){
                final WeakResourceEventListener l = listeners.next();
                if(l.get() == null) listeners.remove();
            }
            //add a new weak reference to the listener
            return add(new WeakResourceEventListener(listener));
        }

        public boolean remove(final ResourceEventListener listener){
            final Iterator<WeakResourceEventListener> listeners = iterator();
            while (listeners.hasNext()){
                final WeakResourceEventListener ref = listeners.next();
                final ResourceEventListener l = ref.get();
                if(l == null) listeners.remove(); //remove dead reference
                else if(Objects.equals(listener, l)){
                    ref.clear();    //help GC
                    listeners.remove();
                    return true;
                }
            }
            return false;
        }

        public void fire(final ResourceEvent event){
            final Iterator<WeakResourceEventListener> listeners = iterator();
            while (listeners.hasNext()){
                final WeakResourceEventListener ref = listeners.next();
                final ResourceEventListener l = ref.get();
                if(l == null) listeners.remove(); //remove dead reference
                else l.handle(event);
            }
        }

        @Override
        public void clear() {
            for(final WeakResourceEventListener listener: this)
                listener.clear(); //help GC
            super.clear();
        }
    }

    /**
     * Represents a container for metadata element and its identity.
     * @param <F> Type of the managed resource feature.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class FeatureHolder<F extends MBeanFeatureInfo>{
        private final F metadata;
        /**
         * The identity of this feature.
         */
        protected final BigInteger identity;

        /**
         * Initializes a new container of the managed resource feature.
         * @param metadata The metadata of the managed resource feature.
         * @param identity The identity of the feature.
         */
        protected FeatureHolder(final F metadata,
                              final BigInteger identity){
            this.metadata = Objects.requireNonNull(metadata);
            this.identity = Objects.requireNonNull(identity);
        }

        public final boolean equals(final FeatureHolder<?> other){
            return other != null &&
                    Objects.equals(getClass(), other.getClass()) &&
                    Objects.equals(identity, other.identity);
        }

        /**
         * Gets metadata of the managed resource feature.
         * @return The metadata of the managed resource feature.
         */
        public final F getMetadata(){
            return metadata;
        }

        @Override
        public final boolean equals(final Object other) {
            return other instanceof FeatureHolder<?> && equals((FeatureHolder<?>)other);
        }

        @Override
        public final int hashCode() {
            return identity.hashCode();
        }

        @Override
        public final String toString() {
            return metadata.toString();
        }
    }

    /**
     * Metadata of the managed resource feature.
     */
    protected final Class<F> metadataType;
    private final ResourceEventListenerList resourceEventListeners;
    private final Enum<?> resourceEventListenerSyncGroup;
    private final String resourceName;

    protected <G extends Enum<G>> AbstractFeatureModeler(final String resourceName,
                                                       final Class<F> metadataType,
                                                       final Class<G> resourceGroupDef,
                                                       final G resourceEventListenerSyncGroup) {
        super(resourceGroupDef);
        this.metadataType = Objects.requireNonNull(metadataType);
        this.resourceEventListeners = new ResourceEventListenerList();
        this.resourceEventListenerSyncGroup = Objects.requireNonNull(resourceEventListenerSyncGroup);
        this.resourceName = resourceName;
    }

    /**
     * Gets name of the resource.
     *
     * @return The name of the resource.
     */
    public final String getResourceName() {
        return resourceName;
    }

    /**
     * Adds a new feature modeler event listener.
     *
     * @param listener Feature modeler event listener to add.
     */
    public final void addModelEventListener(final ResourceEventListener listener) {
        try (final LockScope ignored = beginWrite(resourceEventListenerSyncGroup)) {
            resourceEventListeners.add(listener);
        }
    }

    /**
     * Removes the specified modeler event listener.
     *
     * @param listener The listener to remove.
     */
    public final void removeModelEventListener(final ResourceEventListener listener) {
        try (final LockScope ignored = beginWrite(resourceEventListenerSyncGroup)) {
            resourceEventListeners.remove(listener);
        }
    }

    protected final void fireResourceEvent(final FeatureModifiedEvent<?> event) {
        try (final LockScope ignored = beginWrite(resourceEventListenerSyncGroup)) {
            resourceEventListeners.fire(event);
        }
    }

    protected final F[] toArray(final Collection<? extends FeatureHolder<F>> features) {
        final F[] result = ObjectArrays.newArray(metadataType, features.size());
        int index = 0;
        for(final FeatureHolder<F> holder: features)
            result[index++] = holder.getMetadata();
        return result;
    }

    protected final void removeAllResourceEventListeners() {
        try (final LockScope ignored = beginWrite(resourceEventListenerSyncGroup)) {
            resourceEventListeners.clear();
        }
    }
}
