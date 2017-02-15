package com.bytex.snamp.moa.watching;

import com.bytex.snamp.moa.DataAnalyzer;

import java.util.Collection;

/**
 * Represents watcher of attribute values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface AttributeWatcherService extends DataAnalyzer {
    void addWatcher(final AttributeWatcher listener);
    void removeWatcher(final AttributeWatcher listener);
    Collection<AttributeWatcherSettings> getSettings();
}
