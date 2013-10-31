package com.snamp;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents thread safe object holder.
 * @param <R> Type of the resource to hold.
 * @author roman
 */
public class ConcurrentResourceHolder<R> extends ReentrantReadWriteLock {
    /**
     * Represents coordinated resource.
     */
    protected R resource;

    /**
     * Initializes a new thread safe container for the specified resource.
     * @param resource
     */
    public ConcurrentResourceHolder(final R resource){
        this.resource = resource;
    }

    /**
     * Represents resource reader.
     * @param <R> Type of the resource to read.
     * @param <V> Type of the resource reading operation.
     */
    public static interface Reader<R, V, E extends Throwable>{
        public V read(final R resource) throws E;
    }

    /**
     * Represents resource reader that cannot throws an exception.
     * @param <R>
     * @param <V>
     */
    public static interface ConsistentReader<R, V> extends Reader<R, V, Exception>{
        @Override
        public V read(final R resource);
    }

    public static interface Writer<R, I, O, E extends Throwable>{
        public O write(final R resource, final I value) throws E;
    }

    public static interface ConsistentWriter<R, I, O> extends Writer<R, I, O, Exception>{
        @Override
        public O write(final R resource, final I value);
    }

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

    public final void changeResource(final R newResource){
        changeResource(new Activator<R>() {
            @Override
            public R newInstance() {
                return newResource;
            }
        });
    }

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
