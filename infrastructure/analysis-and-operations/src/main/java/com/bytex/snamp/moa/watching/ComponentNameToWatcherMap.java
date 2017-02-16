package com.bytex.snamp.moa.watching;

import java.util.Map;

/**
 * Represents map of component watchers where key represents name of the component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ComponentNameToWatcherMap<W extends ComponentWatcher> extends Map<String, W> {
    /**
     * Creates a new watcher and add it into this map.
     * @param componentName Name of the component to watch.
     * @return Added instance of the watcher.
     */
    W add(final String componentName);
}
