package com.snamp;

import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.SynchronizationType;
import com.snamp.internal.ThreadSafety;

/**
 * Represents an object that aggregates another objects.<br/>
 * <p>
 *     <b>Example:</b><br/>
 *     <pre>{@code
 *         final class SimpleAggregator implements Aggregator{
 *           private final File someFile;
 *
 *           public SimpleAggregator(final String path){
 *             someFile = new File(path);
 *           }
 *
 *           public <T> T queryObject(final Class<T> ot){
 *             if(File.class.equals(ot)) return someFile;
 *             else return null;
 *           }
 *         }
 *     }</pre>
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see AbstractAggregator
 */
public interface Aggregator {
    /**
     * Retrieves the aggregated object.
     * @param objectType Type of the requested object.
     * @param <T> Type of the aggregated object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.READ_LOCK)
    public <T> T queryObject(final Class<T> objectType);
}
