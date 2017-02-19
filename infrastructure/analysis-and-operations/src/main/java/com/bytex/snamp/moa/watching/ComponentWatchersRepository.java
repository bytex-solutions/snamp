package com.bytex.snamp.moa.watching;

import com.bytex.snamp.FactoryMap;

/**
 * Represents map of component watchers where key represents name of the component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ComponentWatchersRepository<W extends ComponentWatcher> extends FactoryMap<String, W> {
    /**
     * Creates a new watcher and add it into this map.
     * @param componentName Name of the component to watch.
     * @return Added instance of the watcher.
     */
    @Override
    W getOrAdd(final String componentName);
}
