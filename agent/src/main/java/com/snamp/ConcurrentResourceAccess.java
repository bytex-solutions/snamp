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
 *           return map.read(new ConcurrentResourceAccess.ConsistentReader<Map<String, String>, String>(){
 *             public String read(final Map<String, String> map){
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
     * Represents resource reader that can throws an exception during reading operation.
     * @param <R> Type of the resource to read.
     * @param <V> Type of the result of reading operation.
     * @since 1.0
     * @version 1.0
     */
    public static interface Reader<R, V, E extends Throwable>{
        /**
         * Reads the resource.
         * @param resource The resource to read.
         * @return The value obtained from the specified resource.
         * @throws E An exception that can be raised by reading operation.
         */
        public V read(final R resource) throws E;
    }

    /**
     * Represents resource reader that cannot throws an exception during reading operation.
     * @param <R> Type of the resource to read.
     * @param <V> Type of result of reading operation.
     */
    public static interface ConsistentReader<R, V> extends Reader<R, V, Exception>{
        /**
         * Reads the resource.
         * @param resource The resource to read.
         * @return The value obtained from the specified resource.
         */
        @Override
        public V read(final R resource);
    }

    /**
     * Represents resource writer that can throws an exception during writing operation.
     * @param <R> Type of the resource to change.
     * @param <I> Type of the input value to be written into the resource.
     * @param <O> Type of the write result.
     * @param <E> Type of the exception that can be raised by writing operation.
     */
    public static interface Writer<R, I, O, E extends Throwable>{
        /**
         * Writes the specified value into the resource.
         * @param resource The resource to change.
         * @param value The value to be written into the resource.
         * @return The result of the writing operation.
         * @throws E An exception that can be raised by writing operation.
         */
        public O write(final R resource, final I value) throws E;
    }

    /**
     * Represents resource writer that cannot throws an exception.
     * @param <R> Type of the resource to change.
     * @param <I> Type of the value to be written into the resource.
     * @param <O> Type of the writing result.
     */
    public static interface ConsistentWriter<R, I, O> extends Writer<R, I, O, Exception>{
        /**
         * Writes the specified value into the resource.
         * @param resource The resource to change.
         * @param value The value to be written into the resource.
         * @return Writing operation result.
         */
        @Override
        public O write(final R resource, final I value);
    }

    /**
     * Provides consistent read on the resource.
     * <p>
     *     This operation acquires read-lock on the resource.
     * </p>
     * @param reader The resource reader.
     * @param <V> Type of the resource reading value operation.
     * @return The value obtained from the resource.
     */
    public final <V> V read(final ConsistentReader<R, V> reader){
        if(reader == null) return null;
        final ReadLock rl = readLock();
        rl.lock();
        try{
            return reader.read(resource);
        }
        finally {
            rl.unlock();
        }
    }

    /**
     * Provides inconsistent read on the resource.
     * <p>
     *    This operation acquires read-lock on the resource.
     * </p>
     * @param reader The resource reader.
     * @param <V> Type of the resource reading value operation.
     * @param <E> Type of the exception that can be raised by reader.
     * @return The reading operation result.
     * @throws E Type of the exception that can be raised by reader.
     */
    public final <V, E extends Throwable> V read(final Reader<R, V, E> reader) throws E{
        if(reader == null) return null;
        final ReadLock rl = readLock();
        rl.lock();
        try{
            return reader.read(resource);
        }
        finally {
            rl.unlock();
        }
    }

    public final <I, O> O write(final ConsistentWriter<R, I, O> writer, final I value){
        if(writer == null) return null;
        final WriteLock wl = writeLock();
        wl.lock();
        try{
            return writer.write(resource, value);
        }
        finally {
            wl.unlock();
        }
    }

    public final <I, O, E extends Throwable> O write(final Writer<R, I, O, E> writer, final I value) throws E{
        if(writer == null) return null;
        final WriteLock wl = writeLock();
        wl.lock();
        try{
            return writer.write(resource, value);
        }
        finally {
            wl.unlock();
        }
    }

    public final <I, O, E extends Throwable> void readAndWrite(final Reader<R, I, E> reader, final Writer<R, I, O, E> writer) throws E{
        write(writer, read(reader));
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
