package com.snamp;

import java.util.logging.Logger;

/**
 * Represents platform service.
 * @author roman
 */
public interface PlatformService extends Aggregator {
    /**
     * Gets a logger associated with this platform service.
     * @return
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public Logger getLogger();
}
