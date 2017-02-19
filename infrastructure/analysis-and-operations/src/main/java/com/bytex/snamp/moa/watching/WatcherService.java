package com.bytex.snamp.moa.watching;

import com.bytex.snamp.moa.DataAnalyzer;

/**
 * Represents watcher of attribute values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface WatcherService extends DataAnalyzer {
    ComponentWatchersRepository<?> getComponentsWatchers();
}
