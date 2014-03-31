package com.snamp.core;

import com.snamp.Aggregator;
import com.snamp.internal.Internal;
import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;

import java.util.logging.Logger;

/**
 * Represents a root interface for all SNAMP platform service.<br/>
 * <p>
 *     This interface uses as an indicator for architectural component of SNAMP.
 *     You should not implement this interface directly
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public interface PlatformService extends Aggregator {
    /**
     * Gets a logger associated with this platform service.
     * @return A logger associated with this platform service.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public Logger getLogger();
}
