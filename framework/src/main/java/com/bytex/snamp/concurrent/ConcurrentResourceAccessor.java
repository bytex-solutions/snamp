package com.bytex.snamp.concurrent;

import javax.annotation.concurrent.ThreadSafe;

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
 *         private final ConcurrentResourceAccessorImpl&lt;Map&lt;String, String&gt;&gt; map =
 *           new ConcurrentResourceAccessorImpl&lt;&gt;(new HashMap&lt;&gt;());
 *
 *         public String get(final String key){
 *           return map.read(map -&gt; map.get(key));
 *         }
 *
 *         public void put(final String key, final String value){
 *          map.write(map -&gt; {
 *              map.put(key, value);
 *              return null;
 *          });
 *         }
 *     }
 *     }</pre>
 * </p>
 * @param <R> Type of the thread-unsafe resource to hold.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
@ThreadSafe
public class ConcurrentResourceAccessor<R> extends AbstractConcurrentResourceAccessor<R> {
    private static final long serialVersionUID = -5981763196807390411L;

    /**
     * Represents coordinated resource.
     */
    private R resource;

    /**
     * Initializes a new thread safe container for the specified resource.
     * @param resource The resource to hold. May be {@literal null}.
     */
    public ConcurrentResourceAccessor(final R resource){
        this.resource = resource;
    }

    /**
     * Returns the resource.
     *
     * @return The resource to synchronize.
     */
    @Override
    protected final R getResource() {
        return resource;
    }

    private <E extends Throwable> void changeResourceImpl(final Action<R, R, E> newResource) throws E{
        resource = newResource.apply(resource);
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
    public final <E extends Throwable> void changeResource(final Action<R, R, E> newResource) throws E {
        if (newResource == null) throw new IllegalArgumentException("newResource is null.");
        writeLock.accept(newResource, this::changeResourceImpl);
    }

    private void changeResourceImpl(final R newResource){
        resource = newResource;
    }

    public final void changeResource(final R newResource) {
        writeLock.accept(this, newResource, ConcurrentResourceAccessor<R>::changeResourceImpl);
    }
}
