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
        if(resource == null) throw new IllegalArgumentException("resource is null.");
        this.resource = resource;
    }

    /**
     * Represents resource reader.
     * @param <R> Type of the resource to read.
     * @param <V> Type of the resource reading operation.
     */
    public static interface Reader<R, V>{
        public V read(final R resource);
    }

    public static interface Writer<R, V>{
        public void write(final R resource, final V value);
    }

    public final <V> V read(final Reader<R, V> reader){
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

    public final <V> void write(final Writer<R, V> writer, final V value){
        if(writer == null) return;
        final WriteLock wl = writeLock();
        wl.lock();
        try{
            writer.write(resource, value);
        }
        finally {
            wl.unlock();
        }
    }

    public final <V> void readAndWrite(final Reader<R, V> reader, final Writer<R, V> writer){
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
            final R res = newResource.newInstance();
            if(res == null) throw new IllegalArgumentException("res is null.");
            else resource = res;
        }
        finally {
            wl.unlock();
        }
    }
}
