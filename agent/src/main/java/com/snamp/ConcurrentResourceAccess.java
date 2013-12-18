package com.snamp;

import java.util.concurrent.locks.ReentrantReadWriteLock;

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
 *           map.write(new ConcurrentResourceAccess.ConsistentWriter<Map<String, String>, Entry<String, String>, Void>(){
 *             public Void write(final Map<String, String> m, final Entry<String, String> entry){
 *               m.put(entry.getKey(), entry.getValue());
 *               return null;
 *             }
 *           }, entry);
 *         }
 *     }
 *     }</pre>
 * </p>
 * @param <R> Type of the resource to hold.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class ConcurrentResourceAccess<R> extends ReentrantReadWriteLock {
    /**
     * Represents coordinated resource.
     */
    protected R resource;

    /**
     * Initializes a new thread safe container for the specified resource.
     * @param resource The resource to hold. May be {@literal null}.
     */
    public ConcurrentResourceAccess(final R resource){
        this.resource = resource;
    }

    /**
     * Represents resource action that can throws an exception during execution.
     * @param <R> Type of the resource to handle.
     * @param <V> Type of the result of reading operation.
     * @since 1.0
     * @version 1.0
     */
    public static interface Action<R, V, E extends Throwable>{
        /**
         * Handles the resource.
         * @param resource The resource to handle.
         * @return The value obtained from the specified resource.
         * @throws E An exception that can be raised by action.
         */
        public V invoke(final R resource) throws E;
    }

    /**
     * Represents resource action that cannot throws an exception during execution.
     * @param <R> Type of the resource to handle.
     * @param <V> Type of the resource handling.
     */
    public static interface ConsistentAction<R, V> extends Action<R, V, Exception> {
        /**
         * Handles the resource.
         * @param resource The resource to handle.
         * @return The value obtained from the specified resource.
         */
        @Override
        public V invoke(final R resource);
    }

    /**
     * Provides consistent invoke on the resource.
     * <p>
     *     This operation acquires invoke-lock on the resource.
     * </p>
     * @param reader The resource reader.
     * @param <V> Type of the resource reading value operation.
     * @return The value obtained from the resource.
     */
    public final <V> V read(final ConsistentAction<R, V> reader){
        if(reader == null) return null;
        final ReadLock rl = readLock();
        rl.lock();
        try{
            return reader.invoke(resource);
        }
        finally {
            rl.unlock();
        }
    }

    /**
     * Provides inconsistent invoke on the resource.
     * <p>
     *    This operation acquires invoke-lock on the resource.
     * </p>
     * @param reader The resource reader.
     * @param <V> Type of the resource reading value operation.
     * @param <E> Type of the exception that can be raised by reader.
     * @return The reading operation result.
     * @throws E Type of the exception that can be raised by reader.
     */
    public final <V, E extends Throwable> V read(final Action<R, V, E> reader) throws E{
        if(reader == null) return null;
        final ReadLock rl = readLock();
        rl.lock();
        try{
            return reader.invoke(resource);
        }
        finally {
            rl.unlock();
        }
    }

    public final <O> O write(final ConsistentAction<R, O> writer){
        if(writer == null) return null;
        final WriteLock wl = writeLock();
        wl.lock();
        try{
            return writer.invoke(resource);
        }
        finally {
            wl.unlock();
        }
    }

    public final <O, E extends Throwable> O write(final Action<R, O, E> writer) throws E{
        if(writer == null) return null;
        final WriteLock wl = writeLock();
        wl.lock();
        try{
            return writer.invoke(resource);
        }
        finally {
            wl.unlock();
        }
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
            this.resource = newResource.newInstance();
        }
        finally {
            wl.unlock();
        }
    }
}
