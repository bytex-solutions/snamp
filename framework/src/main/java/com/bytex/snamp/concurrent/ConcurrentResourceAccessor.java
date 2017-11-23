package com.bytex.snamp.concurrent;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
 * @version 2.1
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
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(resource);
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        resource = (R) in.readObject();
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
