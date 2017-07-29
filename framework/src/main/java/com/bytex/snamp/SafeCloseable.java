package com.bytex.snamp;

/**
 * A resource that must be closed when it is no longer needed.
 * <p>
 *     Checked exception is suspended.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface SafeCloseable extends AutoCloseable {

    /**
     * Releases all resources associated with this object.
     */
    @Override
    void close();

    static void closeAll(final SafeCloseable... closeables){
        for(final SafeCloseable c: closeables)
            c.close();
    }
}
