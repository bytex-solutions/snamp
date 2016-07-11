package com.bytex.snamp.concurrent;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.Wrapper;

import java.util.function.Supplier;

/**
 * Provides thread-safe access to the thread-unsafe resource.
 * <p>
 *     This class allows to apply the following operations on the stored resource:<br/>
 *     <ul>
 *         <li>Reading from the resource</li>
 *         <li>Writing to the resource</li>
 *         <li>Changing the resource</li>
 *     </ul><br/>
 *     All reading operations will be executed concurrently. All writing operations will be executed sequentially.
 *     <b>Example:</b><br/>
 *     <pre>{@code
 *     final class Container{
 *         private final ConcurrentResourceAccessorImpl<Map<String, String>> map =
 *           new ConcurrentResourceAccessorImpl<>(new HashMap<>());
 *
 *         public String get(final String key){
 *           return map.invoke(new ConcurrentResourceAccess.ConsistentAction<Map<String, String>, String>(){
 *             public String invoke(final Map<String, String> map){
 *               return map.get(key);
 *             }
 *           });
 *         }
 *
 *         public void put(final Entry<String, String> entry){
 *           map.write(new ConcurrentResourceAccessImpl.ConsistentAction<Map<String, String>, Void>(){
 *             public Void write(final Map<String, String> m){
 *               m.put(entry.getKey(), entry.getRawValue());
 *               return null;
 *             }
 *           });
 *         }
 *     }
 *     }</pre>
 * </p>
 * @param <R> Type of the thread-unsafe resource to hold.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public class ConcurrentResourceAccessor<R> extends AbstractConcurrentResourceAccessor<R> implements Wrapper<R> {
    private static final long serialVersionUID = -5981763196807390411L;

    /**
     * Represents coordinated resource.
     */
    private final VolatileBox<R> resource;

    /**
     * Initializes a new thread safe container for the specified resource.
     * @param resource The resource to hold. May be {@literal null}.
     */
    public ConcurrentResourceAccessor(final R resource){
        this.resource = new VolatileBox<>(resource);
    }

    /**
     * Returns the resource.
     *
     * @return The resource to synchronize.
     */
    @Override
    protected final R getResource() {
        return resource.get();
    }

    /**
     * Reads the current resource and set a new resource.
     * @param newResource A new instance of the resource.
     * @return Existed resource.
     */
    protected final R getAndSetResource(final R newResource){
        return resource.getAndSet(newResource);
    }

    /**
     * Sets resource in thread unsafe manner.
     * @param resource The resource to set.
     */
    protected final void setResource(final R resource){
        this.resource.set(resource);
    }

    /**
     * Changes the resource.
     * <p>
     *     This operation acquires write-lock on the resource.
     * </p>
     * @param newResource A new instance of the resource.
     */
    public final void changeResource(final R newResource){
        changeResource(() -> newResource);
    }

    /**
     * Changes the resource.
     * <p>
     *   This operation acquires write-lock on the resource.
     * </p>
     * @param newResource The factory of the new resource. Cannot be {@literal null}.
     * @throws IllegalArgumentException newResource is {@literal null}.
     */
    public final void changeResource(final Supplier<? extends R> newResource){
        if(newResource == null) throw new IllegalArgumentException("newResource is null.");
        final WriteLock wl = writeLock();
        wl.lock();
        try{
            setResource(newResource.get());
        }
        finally {
            wl.unlock();
        }
    }

    /**
     * Changes the resource.
     * <p>
     *   This operation acquires write-lock on the resource.
     * </p>
     * @param newResource The factory of the new resource. Cannot be {@literal null}.
     * @throws IllegalArgumentException newResource is {@literal null}.
     */
    public final void changeResource(final ConsistentAction<R, R> newResource) {
        changeResource((Action<R, R, ExceptionPlaceholder>) newResource);
    }

    /**
     * Changes the resource. This operation may fail.
     * <p>
     *   This operation acquires write-lock on the resource.
     * </p>
     * @param newResource The factory of the new resource. Cannot be {@literal null}.
     * @throws IllegalArgumentException newResource is {@literal null}.
     * @throws E An exception thrown by resource setter. The original resource remain unchanged.
     * @since 1.2
     */
    public final <E extends Throwable> void changeResource(final Action<R, R, E> newResource) throws E{
        if(newResource == null) throw new IllegalArgumentException("newResource is null.");
        final WriteLock wl = writeLock();
        wl.lock();
        try{
            setResource(newResource.apply(getResource()));
        }
        finally {
            wl.unlock();
        }
    }
}
