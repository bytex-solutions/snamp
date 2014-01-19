package com.snamp;

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
 *         private final ConcurrentResourceAccess<Map<String, String>> map =
 *           new ConcurrentResourceAccess<>(new HashMap<>());
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
 *           map.write(new ConcurrentResourceAccess.ConsistentAction<Map<String, String>, Void>(){
 *             public Void write(final Map<String, String> m){
 *               m.put(entry.getKey(), entry.getValue());
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
 * @version 1.0
 */
public class ConcurrentResourceAccess<R> extends AbstractConcurrentResourceAccess<R> implements Wrapper<R> {
    /**
     * Represents coordinated resource.
     */
    private volatile R resource;

    /**
     * Initializes a new thread safe container for the specified resource.
     * @param resource The resource to hold. May be {@literal null}.
     */
    public ConcurrentResourceAccess(final R resource){
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

    /**
     * Sets resource in thread unsafe manner.
     * @param resource The resource to set.
     */
    protected final void setResource(final R resource){
        this.resource = resource;
    }

    /**
     * Changes the resource.
     * <p>
     *     This operation acquires write-lock on the resource.
     * </p>
     * @param newResource A new instance of the resource.
     */
    public final void changeResource(final R newResource){
        changeResource(new Activator<R>() {
            @Override
            public R newInstance() {
                return newResource;
            }
        });
    }

    /**
     * Changes the resource.
     * <p>
     *   This operation acquires write-lock on the resource.
     * </p>
     * @param newResource The factory of the new resource. Cannot be {@literal null}.
     * @throws IllegalArgumentException newResource is {@literal null}.
     */
    public final void changeResource(final Activator<? extends R> newResource){
        if(newResource == null) throw new IllegalArgumentException("newResource is null.");
        final WriteLock wl = writeLock();
        wl.lock();
        try{
            setResource(newResource.newInstance());
        }
        finally {
            wl.unlock();
        }
    }
}
