package com.itworks.snamp.concurrent;

import com.itworks.snamp.internal.annotations.ThreadSafe;

/**
 * Represents a container for an object with lifecycle management based on
 * reference counting.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ReferenceCountedObject<R> extends ConcurrentResourceAccessor<R> implements AutoCloseable {
    private int refCounter;

    /**
     * Initializes a new reference counted object.
     * <p>
     *     This constructor doesn't initialize underlying resource.
     *     You should call {@link #incref()} before any of resource operations (read or write).
     * </p>
     */
    protected ReferenceCountedObject(){
        super(null);
        this.refCounter = 0;
    }

    /**
     * Initializes a new instance of the resource.
     * <p>
     *     Please avoid any side-effects in this method.
     * </p>
     * @return A new instance of the resource.
     * @throws Exception Unable to instantiate resource.
     */
    protected abstract R createResource() throws Exception;

    /**
     * Cleanup a new instance of the resource.
     * <p>
     *     Please avoid any side-effects in this method.
     * </p>
     * @param resource The resource to clean.
     * @throws Exception Unable to release resource.
     */
    protected abstract void cleanupResource(final R resource) throws Exception;

    /**
     * Increments a reference count to the resource.
     * <p>
     *     This method can cause an initialization of the underlying resource and
     *     invoke {@link #createResource()} method.
     * </p>
     * @throws Exception Unable to instantiate resource.
     */
    @ThreadSafe
    public final void incref() throws Exception{
        incref(1);
    }

    @ThreadSafe
    public synchronized final void incref(final int count) throws Exception{
        if(count > 0)
            switch (refCounter){
                case Integer.MAX_VALUE: throw new IllegalStateException("Maximum count of references reached.");
                case 0: changeResource(createResource());
                default: refCounter += count;
            }
    }

    /**
     * Decrements a reference count to the resource.
     * <p>
     *     This method can cause a cleanup of the underlying resource and
     *     invoke {@link #cleanupResource(Object)} method.
     * </p>
     * @throws Exception Unable to cleanup resource.
     */
    @ThreadSafe
    public final void decref() throws Exception{
        decref(1);
    }

    @ThreadSafe
    public synchronized void decref(final int count) throws Exception{
        if(count > 0){
            refCounter = count > refCounter ? 0 : refCounter - count;
            if(refCounter == 0){
                final R resource = getAndSetResource(null);
                if(resource != null) cleanupResource(resource);
            }
        }
    }

    /**
     * Increments reference to the resource and reads some object from it.
     * <p>
     *     This method increments reference to the underlying resource and
     *     call {@link #read(AbstractConcurrentResourceAccessor.Action)} method.
     *     If this method fails then {@link #decref()} will be called.
     * </p>
     * @param reader The resource reader.
     * @param <V> Type of the object obtained from the resource.
     * @return An object obtained from the resource.
     * @throws Exception An exception occurred in reader or in {@link #createResource()} method.
     */
    @ThreadSafe
    public final <V> V increfAndRead(final Action<R, V, ? extends Exception> reader) throws Exception{
        incref();
        try {
            return read(reader);
        }
        catch (final Exception e){
            decref();
            throw e;
        }
    }

    public final <V> V increfAndWrite(final Action<R, V, ? extends Exception> writer) throws Exception{
        incref();
        try{
           return write(writer);
        }
        catch (final Exception e){
            decref();
            throw e;
        }
    }

    /**
     * Releases underlying resource.
     * <p>
     *     This method calls {@link #cleanupResource(Object)} internally.
     * </p>
     * @throws Exception Failed to release underlying resource.
     */
    @Override
    @ThreadSafe(false)
    public final void close() throws Exception {
        refCounter = 0;
        final R resource = getResource();
        if(resource != null) cleanupResource(resource);
    }
}
